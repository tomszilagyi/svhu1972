import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import io.github.tomszilagyi.svhu1972.Bookmark;

import java.util.Date;

public class BookmarkTest {

    @Test
    public void date_format_and_parse_test() {
        Date date = new Date();
        String str_date = Bookmark.format_timestamp(date);
        Date parsed_date = Bookmark.parse_timestamp(str_date);
        assertThat(parsed_date, is(date));
    }
}
