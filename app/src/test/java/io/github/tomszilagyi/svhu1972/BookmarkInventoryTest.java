import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import io.github.tomszilagyi.svhu1972.Log;
import io.github.tomszilagyi.svhu1972.Bookmark;
import io.github.tomszilagyi.svhu1972.BookmarkInventory;
import io.github.tomszilagyi.svhu1972.ScrollPosition;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class BookmarkInventoryTest {

    File cwd;

    @Before
    public void setup() {
        BookmarkInventory.reset_for_test();

        /* Remove bookmark storage file left by previous test run */
        cwd = new File(".");
        File storage = new File(cwd, BookmarkInventory.FILENAME);
        if (storage.exists()) {
            storage.delete();
        }
    }

    @Test
    public void save_and_load_test() {

        BookmarkInventory bi = BookmarkInventory.get(cwd);
        bi.add(new Bookmark("label 1", new ScrollPosition(1, 2)));
        bi.add(new Bookmark("label 2", new ScrollPosition(2, 3)));
        bi.add(new Bookmark("label 3", new ScrollPosition(3, 4)));
        ArrayList<Bookmark> bookmarks = bi.getBookmarks();
        bi.save();

        BookmarkInventory.reset_for_test();

        BookmarkInventory bi2 = BookmarkInventory.get(cwd);
        ArrayList<Bookmark> bookmarks2 = bi2.getBookmarks();

        assertThat(bookmarks2.toString(), is(bookmarks.toString()));
    }

    @Test
    public void deduplication_test() {

        BookmarkInventory bi = BookmarkInventory.get(cwd);
        bi.add(new Bookmark("samelabel", new ScrollPosition(1, 2)));
        bi.add(new Bookmark("samelabel", new ScrollPosition(2, 3)));
        bi.add(new Bookmark("samelabel", new ScrollPosition(3, 4)));
        ArrayList<Bookmark> bookmarks = bi.getBookmarks();

        /* check that only the last one is present */
        assertThat(bookmarks.size(), is(1));
        assertThat(bookmarks.get(0).position.toString(), is("sp[3:4]"));
    }
}
