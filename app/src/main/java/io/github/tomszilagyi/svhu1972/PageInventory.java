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
        int i;
        for (i = 25; i <= 286; i++) {
                Page page = new Page();
                page.setLabel(String.format("s%04d_1", i));
                pages.add(page);

                page = new Page();
                page.setLabel(String.format("s%04d_2", i));
                pages.add(page);
        }
        return pages;
    }
}
