package io.github.tomszilagyi.svhu1972;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

public class TextData {
    Locale locale;
    Collator collator;
    Activity activity;
    AssetManager assetmgr;
    ArrayList text;
    ArrayList index;

    public TextData(Activity activity) {
        this.activity = activity;
        this.assetmgr = activity.getApplicationContext().getAssets();
        locale = new Locale("swe");
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);

        // read index
        index = load_index();

        // read txt resources 0025..1020 into ArrayList of ArrayList of String
        text = new ArrayList(1020-24);
        for (int p=25; p < 1021; p++) {
            String s = String.format("txt/%04d.txt", p);
            text.add(load_page(s));
        }
        /* some basic validation: how many lines have we read on each page?
         * NB. this is not needed once the OCR files are cleaned up
         * and is commented out to save load time
         */
        /*
        Log.i("Szotar", "*** n_pages: " + text.size());
        String s = "*** n_lines (those != measured): ";
        for (int p=0; p < text.size(); p++) {
            ArrayList page = (ArrayList)text.get(p);
            if (page.size() != column_rows(p, 0) + column_rows(p, 1))
                s = s+" "+(p+25)+":"+page.size();
        }
        Log.i("Szotar", s);
        */
    }

    private class IndexEntry {
        int pageno;
        String first_word;
        private IndexEntry() {
            this.pageno = 0;
            this.first_word = "a";
        }
        private IndexEntry(int pageno, String first_word) {
            this.pageno = pageno;
            this.first_word = first_word;
        }
    }

    private ArrayList load_index() {
        ArrayList lines = load_page("txt/index.txt");
        ArrayList index = new ArrayList();
        for (int i=0; i < lines.size(); i++) {
            String line = (String)lines.get(i); // Ex.: "1013 - Ö - överglänsa"
            int pageno = Integer.parseInt(line.substring(0, 4)) - 25; // display idxs
            String first_word = line.substring(11);
            //Log.i("Szotar", pageno + ": " + first_word);
            index.add(new IndexEntry(pageno, first_word));
        }
        return index;
    }

    /* This is used to load both the index and OCR-ed text pages,
     * so don't do anything fancy here to change the content.
     */
    private ArrayList load_page(String filename) {
        ArrayList lines = new ArrayList();
        InputStream raw = null;
        InputStreamReader isr = null;
        try {
            raw = assetmgr.open(filename);
            isr = new InputStreamReader(raw, "UTF8");
        } catch (UnsupportedEncodingException e) {
            Log.e("Szotar", "Unsupported encoding for "+filename+": "+e);
        } catch (IOException e) {
            Log.e("Szotar", "IO Exception opening "+filename+": "+e);
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
            Log.e("Szotar", "IO error while reading "+filename+": "+e);
        }
        return lines;
    }

    /* Given a search string, find the longest prefix that produces
     * a non-null index-based search result and return that result.
     * Return a TextPosition (page, line) which will be used to
     * scroll the display to the given place, or null.
     */
    public TextPosition index_search(String str) {
        String cstr = str.toLowerCase(locale).replace('w', 'v');
        IndexEntry entry = new IndexEntry();
        int start_page = letter_start_page(str);
        for (int p=index.size()-1; p >= start_page; p--) {
            entry = (IndexEntry)index.get(p);
            if (collator.compare(cstr, entry.first_word) >= 0) break;
        }
        Log.i("Szotar", "search ("+str+"): index: "+
              entry.pageno+":"+entry.first_word);
        TextPosition result = null;
        while (str.length() > 0 && result == null) {
            result = fulltext_search(entry.pageno, str);
            str = str.substring(0, str.length()-1);
        }
        return result;
    }

    /* Given a search string, return a TextPosition (page, line)
     * which will be used to scroll the display to the given place.
     * Start search from the top of page p and throughout a maximum
     * of 8 pages.
     */
    public TextPosition fulltext_search(int p0, String str) {
        for (int p=p0; p < p0+8 && p < text.size(); p++) {
            ArrayList page = (ArrayList)text.get(p);
            for (int l=0; l < page.size(); l++) {
                String line = (String)page.get(l);
                if (line.toLowerCase(locale).startsWith(str.toLowerCase(locale))) {
                    Log.i("Szotar", "search ("+str+"): "+p+":"+l+" :"+line);
                    return new TextPosition(p, l);
                }
            }
        }
        return null;
    }

    /* Return page number where the first letter of str starts */
    public static int letter_start_page(String str) {
        if (str == null || str.length() == 0) return 0;
        switch (str.charAt(0)) {
        case 'a': case 'A': return 0;
        case 'b': case 'B': return 45;
        case 'c': case 'C': return 103;
        case 'd': case 'D': return 106;
        case 'e': case 'E': return 129;
        case 'f': case 'F': return 145;
        case 'g': case 'G': return 226;
        case 'h': case 'H': return 262;
        case 'i': case 'I': return 307;
        case 'j': case 'J': return 325;
        case 'k': case 'K': return 332;
        case 'l': case 'L': return 387;
        case 'm': case 'M': return 422;
        case 'n': case 'N': return 464;
        case 'o': case 'O': return 480;
        case 'p': case 'P': return 504;
        case 'r': case 'R': return 537;
        case 's': case 'S': return 576;
        case 't': case 'T': return 770;
        case 'u': case 'U': return 840;
        case 'v': case 'V':
        case 'w': case 'W': return 885;
        case 'x': case 'X': return 946;
        case 'y': case 'Y': return 947;
        case 'z': case 'Z': return 952;
        case 'å': case 'Å': return 953;
        case 'ä': case 'Ä': return 966;
        case 'ö': case 'Ö': return 977;
        default: return 0;
        }
    }

    /* Return the number of lines of text in a given column.
     * p: displayed page index (0 for 0025, 1 for 0026, ...)
     * c: column index (0 for left, 1 for right)
     */
    public static int column_rows(int p, int c) {
        switch (c) {
        case 0:
            switch (p) {
            /* chapter header & ending pages: */
            case   0: return 48; // A
            case  44: return 39;
            case  45: return 48; // B
            case 102: return 17;
            case 103: return 48; // C
            case 105: return 43;
            case 106: return 48; // D
            case 128: return 31;
            case 129: return 48; // E
            case 144: return 37;
            case 145: return 48; // F
            case 225: return 53;
            case 226: return 48; // G
            case 261: return 13;
            case 262: return 48; // H
            case 306: return 54;
            case 307: return 47; // I
            case 324: return 38;
            case 325: return 47; // J
            case 331: return  8;
            case 332: return 47; // K
            case 386: return  8;
            case 387: return 47; // L
            case 421: return 49;
            case 422: return 47; // M
            case 463: return 13;
            case 464: return 47; // N
            case 479: return 48;
            case 480: return 47; // O
            case 503: return 49;
            case 504: return 47; // P
            case 536: return  6;
            case 537: return 47; // R
            case 575: return 48;
            case 576: return 47; // S
            case 769: return 19;
            case 770: return 47; // T
            case 839: return 28;
            case 840: return 47; // U
            case 884: return 19;
            case 885: return 47; // V, W
            case 945: return  7;
            case 946: return 17; // X
            case 947: return 47; // Y
            case 951: return 34;
            case 952: return 37; // Z
            case 953: return 47; // Å
            case 965: return 31;
            case 966: return 47; // Ä
            case 976: return 17;
            case 977: return 47; // Ö
            case 995: return 46;
            default:  return 54;
            }
        case 1:
            switch (p) {
            /* unbalanced pages: */
            case 207: return 53;
            default: return column_rows(p, 0);
            }
        default: return 0;
        }
    }
}
