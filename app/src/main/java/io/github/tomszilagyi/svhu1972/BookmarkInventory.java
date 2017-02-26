package io.github.tomszilagyi.svhu1972;

import io.github.tomszilagyi.svhu1972.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.ArrayList;

public class BookmarkInventory {

    public static final String FILENAME = "bookmarks.bin";
    private ArrayList<Bookmark> bookmarks;
    private File storage_path;
    private boolean dirty;

    public BookmarkInventory(File storage_path) {
        this.storage_path = storage_path;
        load();
    }

    public ArrayList<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void add(Bookmark bk) {
        /* TODO FIXME when adding the same label a second time, the
         * first instance should be replaced!
         */
        bookmarks.add(bk);
        dirty = true;
    }

    public void load() {
        load(FILENAME);
    }

    public void save() {
        save(FILENAME);
    }

    private void load(String filename) {
        bookmarks = new ArrayList<Bookmark>();
        dirty = false;
        DataInputStream dis = null;
        try {
            File bookmarksFile = new File(storage_path, filename);
            Log.i("Szotar", "Loading bookmarks from "+bookmarksFile);
            FileInputStream fis = new FileInputStream(bookmarksFile);
            dis = new DataInputStream(fis);
            int n = dis.readInt();
            for (int k = 0; k < n; k++) {
                bookmarks.add(new Bookmark(dis));
            }
            dbg_print();
        } catch (FileNotFoundException e) {
            Log.i("Szotar", "Bookmarks file does not exist, starting empty");
        } catch (IOException e) {
            Log.e("Szotar", "Cannot read bookmarks file: "+e);
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {}
            }
        }
    }

    private void save(String filename) {
        if (dirty == false) {
            Log.i("Szotar", "Bookmarks are unchanged, not saving.");
            return;
        }
        DataOutputStream dos = null;
        try {
            File bookmarksFile = new File(storage_path, filename);
            Log.i("Szotar", "Saving bookmarks to "+bookmarksFile);
            FileOutputStream fos = new FileOutputStream(bookmarksFile);
            dos = new DataOutputStream(fos);
            int n = bookmarks.size();
            dos.writeInt(n);
            for (int k=0; k < n; k++) {
                bookmarks.get(k).write(dos);
            }
            dos.flush();
            dirty = false;
        } catch (IOException e) {
            Log.e("Szotar", "Cannot write bookmarks file: "+e);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {}
            }
        }
    }

    public void dbg_print() {
        int n = bookmarks.size();
        Log.i("Szotar", "--- bookmarks (n="+n+") ---");
        for (int k=0; k < n; k++) {
            Log.i("Szotar", bookmarks.get(k).toString());
        }
    }
}