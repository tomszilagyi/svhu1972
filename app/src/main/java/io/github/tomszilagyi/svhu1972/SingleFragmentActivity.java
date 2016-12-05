package io.github.tomszilagyi.svhu1972;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Activity;
import android.view.View;

public abstract class SingleFragmentActivity extends Activity {

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
