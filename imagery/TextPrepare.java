import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

public class TextPrepare {
    Locale locale;
    Collator collator;
    ArrayList<ArrayList<String>> text;
    ArrayList<String> index;

    public TextPrepare() {
        locale = new Locale("sv");
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);
    }

    public static void main(String[] argv) {
        TextPrepare tp = new TextPrepare();

        tp.load_index();
        tp.load_pages();
        tp.normalize_text();
        tp.write_serialized();
    }

    private void load_index() {
        ArrayList<String> lines = load_page("txt/index.txt");
        index = new ArrayList<String>(996);
        for (int i=0; i < lines.size(); i++) {
            String line = lines.get(i); // Ex.: "1013 - Ö - överglänsa"
            int pageno = Integer.parseInt(line.substring(0, 4)) - 25; // display idxs
            String first_word = line.substring(11);
            //System.out.println(pageno + ": " + first_word);
            index.add(first_word);
        }
    }

    // return OCR-ed txt pages 0025..1020 as ArrayList of ArrayList of String
    private void load_pages() {
        text = new ArrayList<ArrayList<String>>(996);
        for (int p=25; p < 1021; p++) {
            String s = String.format(Locale.UK, "txt/%04d.txt", p);
            text.add(load_page(s));
        }
    }

    // write all text data
    public void write_serialized() {
        try {
            String path = "../app/src/main/assets/txt.bin";
            OutputStream os = new FileOutputStream(path);
            DataOutputStream dos = new DataOutputStream(os);
            write_page(dos, index);
            int n_pages = text.size();
            dos.writeInt(n_pages);
            for (int p=0; p < n_pages; p++) {
                write_page(dos, text.get(p));
            }
            dos.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
    private void write_page(DataOutputStream dos, ArrayList<String> page) throws IOException {
        int n_lines = page.size();
        dos.writeInt(n_lines);
        for (int l=0; l < n_lines; l++) {
            dos.writeUTF(page.get(l));
        }
    }

    /* Normalize the text for searching: remove chars \*()
       The pipe character is used to mark compounds, so we try and
       expand them to facilitate searching for the original ones.

       Example: hem|skickad ... -skillnad ... -skrivning ...

       A search for hemskickad (without the |) or for hemskrivning
       should do the right thing.

       How we do this:
       1. in case we read a keyword with | in it, save the stem
       and activate "expand mode";
       2. while in "expand mode", any word written as -suffix (ie.
       starting with a dash) will be expanded with the stem
       except for grammatical suffixes e.g.: -t -n -r -en -et
       3. "expand mode" ends (or is reinstated) when
         - another word containing a | is read;
         - a keyword that is a prefix of the current prefix is
         read (keywords are validated with the current index range)
    */
    private void normalize_text() {
        boolean expand_mode = false;
        String stem = null;
        String suffix = null;
        String index_from = null;
        String index_to = null;

        for (int p=0; p < text.size(); p++) {
            ArrayList<String> page = text.get(p);
            for (int l=0; l < page.size(); l++) {
                String line = page.get(l);

                /* Some basic hygiene to avoid regex problems */
                line = line.replaceAll("–", "-").replaceAll("(\\\\|\\*|\\(|\\))", "");

                int idx_pipe = line.indexOf('|');
                if (idx_pipe > 0) { // pipe exists and not the very first
                    /* Enter or reinitialize expand mode */
                    stem = line.substring(0, idx_pipe);
                    suffix = line.substring(idx_pipe+1);
                    int suffix_end = suffix.indexOf(' ');
                    if (suffix_end > 0) {
                        suffix = suffix.substring(0, suffix_end);
                    }
                    expand_mode = true;
                    index_from = index.get(p);
                    if (p == text.size()-1) {
                        index_to = null;
                    } else {
                        index_to = index.get(p+1);
                    }

                    //System.out.println(line+" => "+stem+"||"+suffix);
                    line = line.replaceFirst("\\|", "");
                } else if (expand_mode) {
                    /* We did not enter expand_mode on this line,
                       see if we need to quit expand_mode */
                    String line_nopipe = line.replaceFirst("\\|", "");
                    if ((collator.compare(index_from, line_nopipe) <= 0) &&
                        ((index_to == null) || collator.compare(line_nopipe, index_to) <= 0)) {
                        /* Yes we do -- line sorts within the current index range */
                        //System.out.println(line+" == index: "+index_from+" >> "+index_to);
                        line = line_nopipe;
                        expand_mode = false;
                        stem = null;
                        suffix = null;
                        index_from = null;
                        index_to = null;
                    }
                }
                if (expand_mode) {
                    /* Identify and iterate through all suffixes in
                     * the line. Filter them according to the current
                     * suffix (taking advantage of the fact that they,
                     * too, are in alphabetical order) and special
                     * exceptions. If accepted, expand with stem and
                     * update current suffix. Expanded keywords are
                     * marked with a preceding '@' so we can avoid
                     * spurious search results where keywords occur in
                     * the text of other keywords.
                     */
                    int idx_dash = line.indexOf('-');
                    while (idx_dash > -1) {
                        String maybe_suffix = line.substring(idx_dash);
                        int idx_end = maybe_suffix.indexOf(' ');
                        if (idx_end > 0) {
                            maybe_suffix = maybe_suffix.substring(0, idx_end);
                        } else {
                            idx_end = 0;
                        }
                        if (!maybe_suffix.matches("^-(n|en|t|et|r|er|ar|or)?[:;,]*$")) {
                            maybe_suffix = maybe_suffix.substring(1);
                            String candidate = stem + maybe_suffix;
                            if ((collator.compare(index_from, candidate) <= 0) &&
                                ((index_to == null) || collator.compare(candidate, index_to) <= 0)) {
                                //System.out.println("== candidate: "+maybe_suffix+ " --> "+candidate);
                                suffix = maybe_suffix;
                                line = line.replaceFirst("-"+suffix, "@" + candidate.replaceAll("\\-", ""));
                            }
                        }
                        idx_dash = line.indexOf('-', idx_dash+idx_end+1);
                    }
                }
                /* Save whatever changes we made to the line */
                page.set(l, line);
            }
        }
    }

    /* This is used to load both the index and OCR-ed text pages,
     * so don't do anything fancy here to change the content.
     */
    private ArrayList<String> load_page(String filename) {
        ArrayList<String> lines = new ArrayList<String>();
        InputStream raw = null;
        InputStreamReader isr = null;
        try {
            raw = new FileInputStream(filename);
            isr = new InputStreamReader(raw, "UTF8");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        BufferedReader is = new BufferedReader(isr);
        String line = null;
        try {
            while((line = is.readLine()) != null) {
                /* skip empty lines -- the book does not have those */
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("IO error while reading "+filename+": "+e);
        }
        return lines;
    }
}
