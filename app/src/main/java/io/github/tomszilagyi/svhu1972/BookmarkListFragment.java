package io.github.tomszilagyi.svhu1972;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private BookmarkAdapter mAdapter;
    private BookmarkInventory mBookmarkInventory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);

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
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBookmarkInventory.save();
    }
/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_bookmark_list, menu);
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /* ... */
            default:
                return super.onOptionsItemSelected(item);
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
