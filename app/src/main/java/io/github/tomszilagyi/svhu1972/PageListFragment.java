package io.github.tomszilagyi.svhu1972;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PageListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PageAdapter mAdapter;
    private boolean mSubtitleVisible;
    private EditText mSearchEditText;
    private TextData mTextData;
    private TextPosition mTextPosition;
    private ImageUtils mImageUtils;
    private boolean position_lock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        position_lock = false;
        mTextData = new TextData(getActivity(), getResources());

        int hwWidth = 480;
        int hwHeight = 800;
        try {
            Point realSize = new Point();
            Display d = getActivity().getWindowManager().getDefaultDisplay();
            Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
            hwWidth = realSize.x;
            hwHeight = realSize.y;
        } catch (Exception e) {
            Log.w("Szotar", "Could not determine hardware display size, "+
                  "using default values. Reason: "+e);
        }
        Log.i("Szotar", "Hardware display size:" +
              " Width:" + hwWidth +
              " Height:" + hwHeight);
        mImageUtils = new ImageUtils(hwWidth, hwHeight) {
            @Override
            public void onImageLoaded() {
                if (position_lock) {
                    scrollToPosition(mTextPosition);
                }
            }
        };

        View view = inflater.inflate(R.layout.fragment_page_list, container, false);

        mSearchEditText = (EditText) view.findViewById(R.id.search_text);
        mSearchEditText.addTextChangedListener(new SearchTextWatcher());
        mTextPosition = new TextPosition();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
            new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.i("Szotar",
                          "Updating image display view area:" +
                          " Width:" + mRecyclerView.getWidth() +
                          " Height:" + mRecyclerView.getHeight());
                    mImageUtils.setViewSize(mRecyclerView.getWidth(),
                                            mRecyclerView.getHeight());
                    mRecyclerView.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
                }
            });
        mRecyclerView.addOnScrollListener(
            new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        position_lock = false;
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    /*
                    position = mLayoutManager.findFirstVisibleItemPosition();
                    Log.i("Szotar",
                          "pos="+mLayoutManager.findFirstVisibleItemPosition());
                    */
                }
            });

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    private void scrollToPosition(TextPosition pos) {
        int page = 2 * pos.page;
        int line = pos.line;
        if (pos.line >= TextData.column_rows(pos.page, 0)) {
            page += 1;
            line -= TextData.column_rows(pos.page, 0);
        }
        Log.i("Szotar", "scrollToPosition: "+page+":"+line);
        // TODO compute the vertical pixel position for actual image:
        // Store the size of each bitmap, use that to compute the
        // vertical position with the image resized to display width
        View view = mLayoutManager.findViewByPosition(page);
        if (view == null) {
            Log.i("Szotar", "view for page "+page+" is null!");
        } else {
            Log.i("Szotar", "w="+view.getWidth()+" h="+view.getHeight()+
                  " mw="+view.getMeasuredWidth()+" mh="+view.getMeasuredHeight());
        }
        Double vert = -57.6 * line;
        mLayoutManager.scrollToPositionWithOffset(page, vert.intValue());
    }

    public class SearchTextWatcher implements TextWatcher {
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (before == 0 && count == 0) return;
            TextPosition result = mTextData.index_search_prefix(s.toString());
            if (result != null) {
                mTextPosition = result;
                position_lock = true;
                scrollToPosition(mTextPosition);
            } else {
                Log.i("Szotar", "search returned null!");
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }
        public void afterTextChanged(Editable s) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_page_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new:
                /* TODO debug */
                scrollToPosition(mTextPosition);
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        String subtitle = getString(R.string.subtitle_format, 0);
        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI() {
        PageInventory pi = PageInventory.get();
        List<Page> pages = pi.getPages();

        if (mAdapter == null) {
            mAdapter = new PageAdapter(pages);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setPages(pages);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    private class PageHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private Page mPage;

        public PageHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_page_image);
        }

        public void bindPage(Page page) {
            mPage = page;
            if (mImageView != null) {
                mImageUtils.loadBitmap(getResources(),
                                       title_to_resId(mPage.getLabel()),
                                       mImageView);
            }
        }

        private int title_to_resId(String str) {
            int i = getActivity()
                    .getResources()
                    .getIdentifier(str, "drawable",
                                   getActivity().getPackageName());
            return i;
        }
    }

    private class PageAdapter extends RecyclerView.Adapter<PageHolder> {

        private List<Page> mPages;

        public PageAdapter(List<Page> pages) {
            mPages = pages;
        }

        @Override
        public PageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_page, parent, false);
            return new PageHolder(view);
        }

        @Override
        public void onBindViewHolder(PageHolder holder, int position) {
            Page page = mPages.get(position);
            holder.bindPage(page);
        }

        @Override
        public int getItemCount() {
            return mPages.size();
        }

        public void setPages(List<Page> pages) {
            mPages = pages;
        }
    }
}
