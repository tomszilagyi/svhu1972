package io.github.tomszilagyi.svhu1972;

import android.support.v4.app.Fragment;

public class BookmarkListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new BookmarkListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }
}
