package io.github.tomszilagyi.svhu1972;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PageListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PageAdapter mAdapter;
    private EditText mSearchEditText;
    private ImageButton mSaveBookmark;
    private TextData mTextData;
    private TextPosition mTextPosition;
    private ImageUtils mImageUtils;
    private boolean position_lock;
    private int image_area_height;
    private int image_area_width;
    private boolean global_layout_listener_ran = false;

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
        mTextData = new TextData(getActivity().getApplicationContext().getAssets());

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
        image_area_width = hwWidth;
        image_area_height = hwHeight;
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
        mSearchEditText.setOnEditorActionListener(
            new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_PREVIOUS) {
                        v.setText("");
                        return true;
                    }
                    return false;
                }
            });
        mTextPosition = new TextPosition();

        mSaveBookmark = (ImageButton) view.findViewById(R.id.save_bookmark);
        mSaveBookmark.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = mLayoutManager.findFirstVisibleItemPosition();
                    int offset = mLayoutManager.getChildAt(0).getTop();
                    String text = mSearchEditText.getText().toString();
                    if (text.length() == 0) return;
                    Log.i("Szotar", "Save bookmark: '"+text+"' at pos="+pos+":"+offset);
                    /* TODO */
                }
            });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
            new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    /* This is anal, but the API deprecation of
                       removeGlobalOnFocusChangeListener in favour of
                       removeOnGlobalFocusChangeListener is even more so. */
                    if (global_layout_listener_ran) return;
                    global_layout_listener_ran = true;

                    image_area_width = mRecyclerView.getWidth();
                    image_area_height = mRecyclerView.getHeight();
                    mImageUtils.setViewSize(image_area_width,
                                            image_area_height);
                    Log.i("Szotar",
                          "Updating image display view area:" +
                          " Width:" + image_area_width +
                          " Height:" + image_area_height);
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
                    /* TODO save this position and revert to the last saved one
                     * when an image load throws it off
                     */
                    /*
                    int pos = mLayoutManager.findFirstVisibleItemPosition();
                    int offset = mLayoutManager.getChildAt(0).getTop();
                    Log.i("Szotar", "pos="+pos+":"+offset);
                    */
                }
            });

        mLayoutManager = new LinearLayoutManager(getActivity()) {
            /* make forward scrolling a bit smoother by laying out extra area */
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return image_area_height;
            }
        };
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

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
        int n_rows = TextData.column_rows(page / 2, page % 2);
        int image_x = ImageSize.x(page);
        int image_y = ImageSize.y(page);
        Double pixels_per_row = 1.0 * image_y * image_area_width / image_x / n_rows;
        Double vert = -pixels_per_row * line;
        /*
        Log.i("Szotar", "scrollToPosition: "+page+":"+line);
        Log.i("Szotar", "size of page "+page+" is "+ image_x + "x" + image_y);
        Log.i("Szotar", "image_area_width: "+image_area_width);
        Log.i("Szotar", "pixels_per_row: "+pixels_per_row);
        Log.i("Szotar", "vertical offset: "+vert.intValue());
        */
        mLayoutManager.scrollToPositionWithOffset(page, vert.intValue());
    }

    public class SearchTextWatcher implements TextWatcher {
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (before == 0 && count == 0) return;
            TextPosition result = mTextData.index_search(s.toString());
            if (result != null) {
                mTextPosition = result;
                position_lock = true;
                scrollToPosition(mTextPosition);
            } else {
                Log.i("Szotar", "search returned null!");
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {}
        public void afterTextChanged(Editable s) {}
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_page_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_bookmarks:
                Log.i("Szotar", "menu -> bookmarks");
                /* TODO */
                return true;
            case R.id.menu_item_about_app:
                Log.i("Szotar", "menu -> about");
                /* TODO */
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                mImageUtils.loadBitmap(getActivity().getApplicationContext(),
                                       mPage.getIndex(), mImageView);
            }
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
