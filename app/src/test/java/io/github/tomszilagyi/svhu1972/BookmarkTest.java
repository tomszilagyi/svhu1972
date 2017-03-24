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

    @Test
    public void date_pretty_print_test() {
        Date now         = new Date(1490302430749L);  // Thu Mar 23 21:53:50 CET 2017
        Date same_day    = new Date(1490301281394L);  // Thu Mar 23 21:34:41 CET 2017
        Date yesterday   = new Date(1490202233129L);  // Wed Mar 22 18:03:53 CET 2017
        Date same_month  = new Date(1489205278234L);  // Sat Mar 11 05:07:58 CET 2017
        Date in_1_year   = new Date(1459508234856L);  // Fri Apr 01 12:57:14 CEST 2016
        Date really_old  = new Date(1459401251341L);  // Thu Mar 31 07:14:11 CEST 2016
        Date newyearsday = new Date(1483250234567L);  // Sun Jan 01 06:57:14 CET 2017
        Date newyearseve = new Date(1483201234567L);  // Sat Dec 31 17:20:34 CET 2016

        assertThat(Bookmark.pretty_print_timestamp(same_day, now), is("21:34"));
        assertThat(Bookmark.pretty_print_timestamp(yesterday, now), is("Tegnap 18:03"));
        assertThat(Bookmark.pretty_print_timestamp(newyearseve, newyearsday), is("Tegnap 17:20"));
        assertThat(Bookmark.pretty_print_timestamp(newyearseve, now), is("december 31, 17:20"));
        assertThat(Bookmark.pretty_print_timestamp(same_month, now), is("március 11, 05:07"));
        assertThat(Bookmark.pretty_print_timestamp(in_1_year, now), is("április 1, 12:57"));
        assertThat(Bookmark.pretty_print_timestamp(really_old, now), is("2016. március 31, 07:14"));
    }
}
