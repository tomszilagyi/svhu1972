package io.github.tomszilagyi.svhu1972;

import android.content.res.AssetManager;

import io.github.tomszilagyi.svhu1972.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

public class TextData {
    Locale locale;
    Collator collator;
    AssetManager assetmgr;
    ArrayList<ArrayList<String>> text;
    ArrayList<String> index;

    public TextData(AssetManager assetmgr) {
        this.assetmgr = assetmgr;
        locale = new Locale("sv");
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);
        read_serialized();
    }

    /* Accessors used for testing only */
    public ArrayList<String> getIndex() {
        return index;
    }
    public ArrayList<ArrayList<String>> getText() {
        return text;
    }

    /* read all text data from the preprocessed, packed binary
     * created by TextPrepare in assets/txt.bin
     */
    public void read_serialized() {
        try {
            String filename = "txt.bin";
            InputStream is = openAsset(filename);
            BufferedInputStream bis = new BufferedInputStream(is);
            DataInputStream dis = new DataInputStream(bis);
            index = read_page(dis);
            int n_pages = dis.readInt();
            text = new ArrayList<ArrayList<String>>();
            for (int p=0; p < n_pages; p++) {
                text.add(read_page(dis));
            }
            dis.close();
        } catch (Exception e) {
            Log.e("Szotar", e.toString());
        }
    }
    private ArrayList<String> read_page(DataInputStream dis) throws IOException {
        int n_lines = dis.readInt();
        ArrayList<String> list = new ArrayList<String>();
        for (int l=0; l < n_lines; l++) {
            list.add(dis.readUTF());
        }
        list.trimToSize();
        return list;
    }
    private InputStream openAsset(String filename) throws Exception {
        if (assetmgr == null) { /* Test run on build host */
            return new FileInputStream("src/main/assets/" + filename);
        } else {
            return assetmgr.open(filename, AssetManager.ACCESS_STREAMING);
        }
    }

    /* Given a search string, find the longest prefix that produces
     * a non-null index-based search result and return that result.
     * Return a TextPosition (page, line) which will be used to
     * scroll the display to the given place, or null.
     */
    public TextPosition index_search(String str) {
        String entry = null;
        String cstr = str.toLowerCase(locale).replace('w', 'v');
        int p, start_page = letter_start_page(str);
        for (p=index.size()-1; p >= start_page; p--) {
            entry = index.get(p);
            if (collator.compare(entry, cstr) < 0) break;
        }
        /* entry on start_page (loop broke with p decremented once more)
           or we received garbage input (index search with no result): */
        if (p < start_page) p = start_page;
        Log.i("Szotar", "search ("+str+"): index: "+p+":"+entry);
        TextPosition result = null;
        while (str.length() > 0 && result == null) {
            result = fulltext_search(p, str);
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
        str = str.toLowerCase(locale);
        for (int p=p0; p < p0+8 && p < text.size(); p++) {
            ArrayList<String> page = text.get(p);
            for (int l=0; l < page.size(); l++) {
                String line = page.get(l);
                line = line.toLowerCase(locale);
                if (line.contains("@"+str)) {
                    TextPosition tp = new TextPosition(p, l);
                    Log.i("Szotar", "search ("+str+"): "+tp+": "+line);
                    return tp;
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
        case 'v': case 'V': return 885;
        case 'w': case 'W': return 899; /* special: --> watt */
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
