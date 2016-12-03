package io.github.tomszilagyi.svhu1972;

import android.support.v4.app.Fragment;

public class PageListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PageListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }
}
