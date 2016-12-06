package io.github.tomszilagyi.svhu1972;

import java.util.ArrayList;
import java.util.List;

public class PageInventory {
    private static PageInventory sPageInventory;

    public static PageInventory get() {
        if (sPageInventory == null) {
            sPageInventory = new PageInventory();
        }
        return sPageInventory;
    }

    public List<Page> getPages() {
        List<Page> pages = new ArrayList<>();
        for (int i = 25; i <= 1020; i++) {
                Page page = new Page();
                page.setIndex(2*i);
                pages.add(page);

                page = new Page();
                page.setIndex(2*i + 1);
                pages.add(page);
        }
        return pages;
    }
}
