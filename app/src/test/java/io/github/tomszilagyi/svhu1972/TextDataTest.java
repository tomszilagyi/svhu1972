import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

import io.github.tomszilagyi.svhu1972.Log;
import io.github.tomszilagyi.svhu1972.TextData;
import io.github.tomszilagyi.svhu1972.TextPosition;

public class TextDataTest {

    private TextData td;

    @Before
    public void setup() {
        td = new TextData(null /* no asset manager */);
    }

    /* Validate that on each page, we have the number of lines of raw
     * OCR text that we think we have.
     */
    @Test
    public void ocr_page_lines_test() {
        for (int p=0; p < td.getText().size(); p++) {
            ArrayList<String> page = td.getText().get(p);
            assertThat(page.size(), is(TextData.column_rows(p, 0) +
                                       TextData.column_rows(p, 1)));
        }
    }

    /* Validate that the index looks sane */
    @Test
    public void index_test() {
        Locale locale = new Locale("sv");
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);

        String prev = null;
        for (int p=0; p < td.getIndex().size(); p++) {
            String entry = td.getIndex().get(p);
            if ((prev == null) ||
            // legit exceptions:
                (p == 192 && collator.compare(entry, "få") == 0) ||
                ((p == 725 || p == 726) && collator.compare(entry, "stå") == 0) ||
                (p == 773 && collator.compare(entry, "taga") == 0))
            { prev = entry; continue; }
            // normally, the entries must be different and sort correctly
            assertThat("'"+entry+"' (p."+(p+25)+") sorts greater than '"+prev+"'",
                       collator.compare(entry, prev), greaterThan(0));
            prev = entry;
        }
    }

    /* Validate the search functionality */
    @Test
    public void search_test() {
        /* simple cases including w and garbage input */
        assertThat(td_ixs_str("alma"), is("tp[10:37]"));
        assertThat(td_ixs_str("gravid"), is("tp[244:58]"));
        assertThat(td_ixs_str("hämta"), is("tp[294:105]"));
        assertThat(td_ixs_str("jollragzhsa823"), is("tp[326:50]"));
        assertThat(td_ixs_str("rabatt"), is("tp[537:3]"));
        assertThat(td_ixs_str("watt"), is("tp[899:15]"));
        assertThat(td_ixs_str("ömma"), is("tp[980:86]"));
        assertThat(td_ixs(""), is(nullValue()));
        assertThat(td_ixs("$@&/{#!£€*"), is(nullValue()));

        /* capital letters are converted to lower-case */
        assertThat(td_ixs_str("STORBOKSTÄVER"), is(td_ixs_str("storbokstäver")));
        assertThat(td_ixs_str("Älvsjö"), is(td_ixs_str("älvsjö")));
        assertThat(td_ixs_str("Östersjön"), is(td_ixs_str("östersjön")));

        /* compound words with '|' at the start of the entry (explicit stem) */
        assertThat(td_ixs_str("avhämta"), is("tp[37:84]"));
        assertThat(td_ixs_str("havsbad"), is("tp[271:4]"));
        assertThat(td_ixs_str("rengöra"), is("tp[546:70]"));
        assertThat(td_ixs_str("omljud"), is("tp[491:12]"));
        assertThat(td_ixs_str("påverkan"), is("tp[535:40]"));
        assertThat(td_ixs_str("starköl"), is("tp[695:73]"));
        assertThat(td_ixs_str("sysslolöshet"), is("tp[754:83]"));
        assertThat(td_ixs_str("tvättvatten"), is("tp[830:96]"));
        assertThat(td_ixs_str("varmrätt"), is("tp[896:88]"));
        assertThat(td_ixs_str("översättningslitteratur"), is("tp[993:81]"));
        assertThat(td_ixs_str("övervunnen"), is("tp[994:98]"));

        /* compound words with no '|' at the start of the entry (implicit stem) */
        assertThat(td_ixs_str("bagagevagn"), is("tp[46:29]"));
        assertThat(td_ixs_str("kundtjänst"), is("tp[375:11]"));
        assertThat(td_ixs_str("syrehaltig"), is("tp[754:9]"));
        assertThat(td_ixs_str("syskonbarn"), is("tp[754:33]"));
        assertThat(td_ixs_str("åkerfält"), is("tp[954:72]"));
    }

    /* helpers to make test code more succinct */
    private TextPosition td_ixs(String src) {
        return td.index_search(src);
    }
    private String td_ixs_str(String src) {
        return td_ixs(src).toString();
    }
}
