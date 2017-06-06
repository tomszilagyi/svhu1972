package io.github.tomszilagyi.svhu1972;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BookmarkListFragment extends Fragment {

    private static final String SAVED_SORT_MODE = "sort_mode";
    private static final int SORT_MODE_ALPHABETIC   = 0;
    private static final int SORT_MODE_NEWEST_FIRST = 1;
    private static final int SORT_MODE_OLDEST_FIRST = 2;

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private LinearLayoutManager mLayoutManager;
    private BookmarkAdapter mAdapter;
    private BookmarkInventory mBookmarkInventory;
    private MenuItem mSortModeMenuItem;
    private SharedPreferences mPrefs;
    private int sort_mode = SORT_MODE_ALPHABETIC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Context ctx = getActivity().getApplicationContext();
        mBookmarkInventory = BookmarkInventory.get(ctx.getFilesDir());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bookmark_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mEmptyView = (TextView) view.findViewById(R.id.empty_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        set_view_visibility();

        ItemTouchHelper mIth = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                public boolean onMove(RecyclerView recyclerView,
                                      RecyclerView.ViewHolder viewHolder,
                                      RecyclerView.ViewHolder target) {
                    return false;
                }
                public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                     int direction) {
                    final int pos = viewHolder.getAdapterPosition();
                    mAdapter.onItemDelete(pos);
                    set_view_visibility();
                }
            });
        mIth.attachToRecyclerView(mRecyclerView);

        mPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        sort_mode = mPrefs.getInt(SAVED_SORT_MODE, SORT_MODE_ALPHABETIC);
        refresh_adapter();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh_adapter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt(SAVED_SORT_MODE, sort_mode);
        ed.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBookmarkInventory.save();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_bookmark_list, menu);
        mSortModeMenuItem = menu.findItem(R.id.menu_item_sort_mode);
        setup_sort_mode();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_item_sort_mode:
            if (sort_mode == SORT_MODE_OLDEST_FIRST) {
                sort_mode = SORT_MODE_ALPHABETIC;
            } else {
                sort_mode += 1;
            }
            setup_sort_mode();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void setup_sort_mode() {
        switch (sort_mode) {
        case SORT_MODE_ALPHABETIC:
            mSortModeMenuItem.setTitle(R.string.sort_mode_alphabetic);
            break;
        case SORT_MODE_NEWEST_FIRST:
            mSortModeMenuItem.setTitle(R.string.sort_mode_newest_first);
            break;
        case SORT_MODE_OLDEST_FIRST:
            mSortModeMenuItem.setTitle(R.string.sort_mode_oldest_first);
            break;
        }
        refresh_adapter();
    }

    private void refresh_adapter() {
        if (mAdapter == null) {
            mAdapter = new BookmarkAdapter(sort_mode);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.sort(sort_mode);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void set_view_visibility() {
        if (mBookmarkInventory.size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class BookmarkSorter {

        Collator collator;

        public BookmarkSorter() {
            collator = Collator.getInstance(new Locale("sv"));
            collator.setStrength(Collator.SECONDARY);
        }

        public Comparator<Bookmark> getComparator(int sort_mode) {
            switch (sort_mode) {
            case SORT_MODE_ALPHABETIC:
                return new Comparator<Bookmark>() {
                    @Override
                    public int compare(Bookmark left, Bookmark right) {
                        return collator.compare(left.label, right.label);
                    }
                };
            case SORT_MODE_NEWEST_FIRST:
                return new Comparator<Bookmark>() {
                    @Override
                    public int compare(Bookmark left, Bookmark right) {
                        long diff = right.timestamp.getTime() - left.timestamp.getTime();
                        if (diff < 0) return -1;
                        if (diff > 0) return 1;
                        return 0;
                    }
                };
            case SORT_MODE_OLDEST_FIRST:
                return new Comparator<Bookmark>() {
                    @Override
                    public int compare(Bookmark left, Bookmark right) {
                        long diff = left.timestamp.getTime() - right.timestamp.getTime();
                        if (diff < 0) return -1;
                        if (diff > 0) return 1;
                        return 0;
                    }
                };
            }
            return null;
        }
    }

    private class BookmarkHolder extends RecyclerView.ViewHolder {

        private TextView mLabel;
        private TextView mTimestamp;

        public BookmarkHolder(View itemView) {
            super(itemView);
            mLabel = (TextView) itemView.findViewById(R.id.list_item_bookmark_label);
            mTimestamp = (TextView) itemView.findViewById(R.id.list_item_bookmark_timestamp);
        }

        public void bindBookmark(Bookmark bookmark) {
            mLabel.setText(bookmark.label);
            mTimestamp.setText(bookmark.pretty_print_timestamp());
        }
    }

    private class BookmarkAdapter extends RecyclerView.Adapter<BookmarkHolder> {

        private List<Bookmark> mBookmarks;
        private BookmarkSorter sorter;

        public BookmarkAdapter(int sort_mode) {
            mBookmarks = new ArrayList<Bookmark>(mBookmarkInventory.getBookmarks());
            sorter = new BookmarkSorter();
            sort(sort_mode);
        }

        public void sort(int sort_mode) {
            Collections.sort(mBookmarks, sorter.getComparator(sort_mode));
        }

        @Override
        public BookmarkHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_bookmark, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    int index = mRecyclerView.getChildLayoutPosition(v);
                    Bookmark bk = mBookmarks.get(index);
                    Intent intent = new Intent(getActivity(), PageListActivity.class);
                    intent.putExtra(PageListFragment.EXTRA_BOOKMARK, bk);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            });

            return new BookmarkHolder(view);
        }

        @Override
        public void onBindViewHolder(BookmarkHolder holder, int position) {
            Bookmark bookmark = mBookmarks.get(position);
            holder.bindBookmark(bookmark);
        }

        @Override
        public int getItemCount() {
            return mBookmarks.size();
        }

        public void onItemDelete(int position) {
            Bookmark bk = mBookmarks.get(position);
            mBookmarkInventory.removeByLabel(bk.label);
            mBookmarks.remove(position);
            notifyItemRemoved(position);
        }
    }
}
