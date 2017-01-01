/* This is a standalone program to use as part of the build.
   It reads the raw OCR files under txt/, preprocesses them
   and packs them up into a single binary "txt.bin" under
   the app's assets.

   The main logic revolves around trying to identify keywords
   in whole or stemmed form, to facilitate searching for them.

   There are some command line options to support using this
   program to look for text errors:

   -i or --index: output a listing in index format
           (using this option does not replace txt/index.txt
            as the index used by the app)

   -w or --words: output identified keywords
*/

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

    boolean opt_index = false;
    boolean opt_words = false;

    public TextPrepare() {
        locale = new Locale("sv");
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);
    }

    public static void main(String[] argv) {
        TextPrepare tp = new TextPrepare();
        tp.set_opts(argv);
        tp.load_index();
        tp.load_pages();
        tp.normalize_text();
        tp.write_serialized();
    }
    private void set_opts(String[] argv) {
        for (int i=0; i < argv.length; i++) {
            switch (argv[i]) {
            case "-i": case "--index": opt_index = true; break;
            case "-w": case "--words": opt_words = true; break;
            }
        }
    }

    private void load_index() {
        ArrayList<String> lines = load_page("txt/index.txt");
        index = new ArrayList<String>(996);
        for (int i=0; i < lines.size(); i++) {
            String line = lines.get(i); // Ex.: "1013 - Ö - överglänsa"
            int pageno = Integer.parseInt(line.substring(0, 4)) - 25; // display idxs
            String first_word = line.substring(11);
            if (i != pageno) {
                System.out.println("fatal: index mismatch at item "+i+
                                   ": pageno=" + pageno + " entry=" + first_word);
                System.exit(1);
            }
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

    /* Normalize the text for searching

       The pipe character is used to mark compounds, so we try and
       expand them to facilitate searching for the original ones.

       Example: hem|skickad ... -skillnad ... -skrivning ...

       A search for hemskickad (without the |) or for hemskrivning
       should do the right thing.

       How we do this:
       1. Try to identify keywords by looking at the current page's
          index range. If the keyword contains a | separate it into
          the stem and the suffix, else take the whole as stem.
       2. Expand suffixes of the form "-suffix" with current stem,
          except for grammatical suffixes e.g.: -t -n -r -en -et

       All identified keywords are preceded with @ (does not occur
       elsewhere) to help avoid spurious search results.
    */
    String stem = null;
    String suffix = null;
    String index_from = null;
    String index_to = null;
    boolean index_printed = false;

    private void normalize_text() {
        int n_pages = text.size();
        for (int p=0; p < n_pages; p++) {
            ArrayList<String> page = text.get(p);
            index_printed = false;

            /* Maintain the current index range for page */
            index_from = index.get(p);
            if (p == n_pages-1) {
                index_to = null;
            } else {
                index_to = index.get(p+1);
            }

            /* Go through page and normalize the lines */
            for (int l=0; l < page.size(); l++) {
                String line = page.get(l);
                line = normalize_line(p, line);
                page.set(l, line);
            }
        }
    }

    private String normalize_line(int p, String line) {

        /* Some basic hygiene to clean raw text and avoid regex problems */
        line = line.replaceAll("–", "-")
                   .replaceAll("\\[.*\\]", "")
                   .replaceAll("[:;,\\.]", " ")
                   .replaceAll("[\\(\\)\\[\\]\\{\\}\\?\\*\\\\0-9]", "");

        String keyword = first_word(line);
        line = line.replaceFirst("\\|", "");
        if (in_range(index_from, index_to, first_word(line))) {
            /* line sorts within the current index range,
             * so we assume it starts with a keyword */

            int idx_pipe = keyword.indexOf('|');
            if (idx_pipe > 0) { // pipe exists and is not the very first
                stem = keyword.substring(0, idx_pipe);
                suffix = keyword.substring(idx_pipe+1);
                keyword = stem + suffix;
            } else {
                stem = keyword;
                suffix = null;
            }
            line = line.replaceFirst(keyword, "@"+keyword);
            pr(opt_words, (p+25)+".txt: "+keyword+" => "+stem+" | "+suffix);
            index_printed = print_index(index_printed, p, stem, suffix);
        }

        /* Identify and iterate through all suffixes in the line.
         * Filter them according to the current index range and
         * special exceptions. If accepted, expand with stem.
         * Expanded keywords are marked with a preceding '@' so
         * we can avoid spurious search results where keywords
         * occur in the text of other keywords.
         */
        int idx_dash = line.indexOf('-');
        while (idx_dash > -1) {
            /* the dash must be at the beginning, or preceded by a space */
            if ((idx_dash > 0) && (line.charAt(idx_dash-1) != ' ')) {
                idx_dash = line.indexOf('-', idx_dash+1);
                continue;
            }
            String maybe_suffix = first_word(line.substring(idx_dash));
            if (!maybe_suffix.matches("^-(n|en|t|et|r|er|ar|or)?$")) {
                maybe_suffix = maybe_suffix.substring(1); idx_dash += 1;
                String candidate = stem + maybe_suffix;
                if (in_range(index_from, index_to, candidate)) {
                    suffix = maybe_suffix;
                    candidate = candidate.replaceAll("\\-", "");
                    line = line.replaceFirst("-"+suffix, "@"+candidate);
                    pr(opt_words, (p+25)+".txt:      "+suffix+ " => "+candidate);
                    index_printed = print_index(index_printed, p, candidate, null);
                }
            }
            idx_dash = line.indexOf('-', idx_dash+maybe_suffix.length()+1);
        }
        /* normalize case for searches */
        line = line.toLowerCase(locale);
        return line;
    }

    private boolean print_index(boolean index_printed, int page, String stem, String suffix) {
        if (!opt_index) return true;
        if (index_printed) return true;
        String index_entry = stem;
        if (suffix != null) index_entry += suffix;
        System.out.format("%04d - " + index_entry.toUpperCase().charAt(0) +
                          " - " + index_entry + "\n", page+25);
        return true;
    }
    private boolean in_range(String index_from, String index_to, String str) {
        return ((collator.compare(index_from, str) <= 0) &&
                ((index_to == null) || collator.compare(str, index_to) <= 0));
    }
    private String first_word(String str) {
        int idx_end = str.indexOf(' ');
        if (idx_end > 0) {
            return str.substring(0, idx_end);
        } else {
            return str;
        }
    }
    private void pr(boolean enabled, String msg) {
        if (enabled) {
            System.out.println(msg);
        }
    }
}
