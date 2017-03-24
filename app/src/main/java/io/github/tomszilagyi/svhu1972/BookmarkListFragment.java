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
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class BookmarkListFragment extends Fragment {

    private static final String SAVED_SORT_MODE = "sort_mode";
    private static final short SORT_MODE_ALPHABETIC   = 0;
    private static final short SORT_MODE_NEWEST_FIRST = 1;
    private static final short SORT_MODE_OLDEST_FIRST = 2;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private BookmarkAdapter mAdapter;
    private BookmarkInventory mBookmarkInventory;
    private MenuItem mSortModeMenuItem;
    private SharedPreferences mPrefs;
    private short sort_mode = SORT_MODE_ALPHABETIC;

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
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

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
                }
            });
        mIth.attachToRecyclerView(mRecyclerView);

        mPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (savedInstanceState != null) {
            sort_mode = savedInstanceState.getShort(SAVED_SORT_MODE);
        } else {
            sort_mode = (short)mPrefs.getInt(SAVED_SORT_MODE, SORT_MODE_ALPHABETIC);
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putShort(SAVED_SORT_MODE, sort_mode);
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
        ed.commit();
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
            Log.i("Szotar", "new sort_mode = "+sort_mode);
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
    }

    public void updateUI() {
        if (mAdapter == null) {
            mAdapter = new BookmarkAdapter();
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
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

        @Override
        public BookmarkHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_bookmark, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    int index = mRecyclerView.getChildLayoutPosition(v);
                    Bookmark bk = mBookmarkInventory.get(index);
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
            Bookmark bookmark = mBookmarkInventory.get(position);
            holder.bindBookmark(bookmark);
        }

        @Override
        public int getItemCount() {
            return mBookmarkInventory.size();
        }

        public void onItemDelete(int position) {
            mBookmarkInventory.remove(position);
            notifyItemRemoved(position);
        }
    }
}
