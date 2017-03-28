package io.github.tomszilagyi.svhu1972;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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
import android.widget.Toast;

import java.util.List;

public class PageListFragment extends Fragment {

    static final int PICK_BOOKMARK_REQUEST = 1;
    static final String EXTRA_BOOKMARK = "io.github.tomszilagyi.svhu1972.PageListFragment.EXTRA_BOOKMARK";
    private static final String SAVED_SCROLL_POS_PAGE = "scroll_pos_page";
    private static final String SAVED_SCROLL_POS_OFFSET = "scroll_pos_offset";
    private static final String SAVED_SEARCH_TEXT = "search_text";

    private Context ctx;
    private SharedPreferences mPrefs;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private PageAdapter mAdapter;
    private EditText mSearchEditText;
    private ImageButton mSaveBookmark;
    private TextData mTextData;
    private ScrollPosition mScrollPosition;
    private ImageUtils mImageUtils;
    private boolean position_lock;
    private int image_area_height;
    private int image_area_width;
    private BookmarkInventory mBookmarkInventory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ctx = getActivity().getApplicationContext();
        mTextData = new TextData(ctx.getAssets());
        mBookmarkInventory = BookmarkInventory.get(ctx.getFilesDir());
        position_lock = false;

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
                    scrollToPosition(mScrollPosition);
                }
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        mSaveBookmark = (ImageButton) view.findViewById(R.id.save_bookmark);
        mSaveBookmark.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String text = mSearchEditText.getText().toString();
                    if (text.length() == 0) return;

                    saveScrollPosition();
                    Bookmark bk = new Bookmark(text, mScrollPosition);
                    mBookmarkInventory.add(bk);

                    Toast toast = Toast.makeText(getActivity(), R.string.bookmark_saved,
                                                 Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, toast.getXOffset(), toast.getYOffset());
                    toast.show();
                }
            });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
            new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    image_area_width = mRecyclerView.getWidth();
                    image_area_height = mRecyclerView.getHeight();
                    mImageUtils.setViewSize(image_area_width,
                                            image_area_height);
                    Log.i("Szotar",
                          "Updating image display view area:" +
                          " Width:" + image_area_width +
                          " Height:" + image_area_height);

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

        mAdapter = new PageAdapter(PageInventory.get().getPages());
        mRecyclerView.setAdapter(mAdapter);

        mPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        int scroll_pos_page = mPrefs.getInt(SAVED_SCROLL_POS_PAGE, 0);
        int scroll_pos_offset = mPrefs.getInt(SAVED_SCROLL_POS_OFFSET, 0);
        mScrollPosition = new ScrollPosition(scroll_pos_page, scroll_pos_offset);
        String search_text = mPrefs.getString(SAVED_SEARCH_TEXT, "");
        set_gui_state(search_text, mScrollPosition);

        return view;
    }

    private ScrollPosition text_to_scroll_position(TextPosition pos) {
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
        return new ScrollPosition(page, vert.intValue());
    }

    private void scrollToPosition(ScrollPosition pos) {
        position_lock = true;
        mLayoutManager.scrollToPositionWithOffset(pos.page, pos.offset);
    }

    private void saveScrollPosition() {
        int pos = mLayoutManager.findFirstVisibleItemPosition();
        View view = mLayoutManager.getChildAt(0);
        int offset = view.getTop();
        int height = view.getHeight();
        // If offset is bigger than the image size, reduce with image size.
        // I guess there is a bug in an android class we need to work around...
        while (offset < -height) {
            offset += height;
        }
        mScrollPosition.update(pos, offset);
    }

    public class SearchTextWatcher implements TextWatcher {
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (before == 0 && count == 0) return;
            TextPosition result = mTextData.index_search(s.toString());
            if (result != null) {
                mScrollPosition = text_to_scroll_position(result);
                scrollToPosition(mScrollPosition);
            } else {
                Log.i("Szotar", "search returned null!");
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void afterTextChanged(Editable s) {}
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onPause() {
        super.onPause();

        saveScrollPosition();
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt(SAVED_SCROLL_POS_PAGE, mScrollPosition.page);
        ed.putInt(SAVED_SCROLL_POS_OFFSET, mScrollPosition.offset);
        ed.putString(SAVED_SEARCH_TEXT, mSearchEditText.getText().toString());
        ed.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBookmarkInventory.save();
        position_lock = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PICK_BOOKMARK_REQUEST || resultCode != Activity.RESULT_OK)
            return;

        Bookmark bk = (Bookmark)data.getSerializableExtra(EXTRA_BOOKMARK);
        if (bk == null) return;

        set_gui_state(bk.label, bk.position);
    }

    private void set_gui_state(String text_label, ScrollPosition scroll_position) {
        mSearchEditText.setText(text_label);
        /* set cursor to end of text */
        int end_pos = mSearchEditText.length();
        Editable editable = mSearchEditText.getText();
        Selection.setSelection(editable, end_pos);

        /* override scroll position setting triggered by setText() above
           to the actual position desired by the caller */
        mScrollPosition = scroll_position;
        scrollToPosition(mScrollPosition);
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
                Intent intent = new Intent(ctx, BookmarkListActivity.class);
                startActivityForResult(intent, PICK_BOOKMARK_REQUEST);
                return true;
            case R.id.menu_item_dict_notes:
                display_text("notes.html");
                return true;
            case R.id.menu_item_dict_annotations:
                display_text("annotations.html");
                return true;
            case R.id.menu_item_dict_abbreviations:
                display_text("abbreviations.html");
                return true;
            case R.id.menu_item_about_app:
                display_text("about.html");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void display_text(String filename) {
        Intent intent = new Intent(getActivity(), TextDisplayActivity.class);
        intent.putExtra(TextDisplayFragment.EXTRA_ASSET, filename);
        startActivity(intent);
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
                mImageUtils.loadBitmap(ctx, mPage.getIndex(), mImageView);
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
