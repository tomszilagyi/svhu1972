import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.IOException;

import io.github.tomszilagyi.svhu1972.Log;
import io.github.tomszilagyi.svhu1972.TextData;
import io.github.tomszilagyi.svhu1972.TextPosition;

public class TextDataTest {

    @Test
    public void index_search_test() {
        TextData td = new TextData(null /* no asset manager */);

        /* simple cases including w and garbage input */
        assertThat(td.index_search("alma").toString(), is("tp[10:37]"));
        assertThat(td.index_search("gravid").toString(), is("tp[244:58]"));
        assertThat(td.index_search("hämta").toString(), is("tp[294:105]"));
        assertThat(td.index_search("jollragzhsa823").toString(), is("tp[326:50]"));
        assertThat(td.index_search("rabatt").toString(), is("tp[537:3]"));
        assertThat(td.index_search("watt").toString(), is("tp[899:15]"));
        assertThat(td.index_search("ömma").toString(), is("tp[980:86]"));
        assertEquals(null, td.index_search(""));
        assertEquals(null, td.index_search("$@&/{#!£€*"));

        /* compound words */
        assertThat(td.index_search("havsbad").toString(), is("tp[271:4]"));
        assertThat(td.index_search("rengöra").toString(), is("tp[546:70]"));
    }
}
