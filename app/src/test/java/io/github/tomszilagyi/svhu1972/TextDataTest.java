import org.junit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

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
        assertEquals(null, td_ixs(""));
        assertEquals(null, td_ixs("$@&/{#!£€*"));

        /* capital letters are converted to lower-case */
        assertEquals(td_ixs_str("STORBOKSTÄVER"), td_ixs_str("storbokstäver"));
        assertEquals(td_ixs_str("Älvsjö"), td_ixs_str("älvsjö"));
        assertEquals(td_ixs_str("Östersjön"), td_ixs_str("östersjön"));

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
