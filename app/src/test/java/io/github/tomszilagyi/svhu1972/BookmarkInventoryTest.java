import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import io.github.tomszilagyi.svhu1972.Log;
import io.github.tomszilagyi.svhu1972.Bookmark;
import io.github.tomszilagyi.svhu1972.BookmarkInventory;
import io.github.tomszilagyi.svhu1972.TextPosition;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class BookmarkInventoryTest {

    private BookmarkInventory bi;

    @Test
    public void save_and_load_test() {
        File cwd = new File(".");

        /* Remove bookmark storage file left by previous test run */
        File storage = new File(cwd, BookmarkInventory.FILENAME);
        if (storage.exists()) {
            storage.delete();
        }

        BookmarkInventory bi = new BookmarkInventory(cwd);
        bi.add(new Bookmark("label 1", new TextPosition(1, 2)));
        bi.add(new Bookmark("label 2", new TextPosition(2, 3)));
        bi.add(new Bookmark("label 3", new TextPosition(3, 4)));
        ArrayList<Bookmark> bookmarks = bi.getBookmarks();
        bi.save();

        BookmarkInventory bi2 = new BookmarkInventory(cwd);
        ArrayList<Bookmark> bookmarks2 = bi2.getBookmarks();

        assertThat(bookmarks2.toString(), is(bookmarks.toString()));
    }
}
