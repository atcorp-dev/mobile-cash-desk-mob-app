package ua.com.atcorp.mobilecashdesk.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.com.atcorp.mobilecashdesk.rest.dto.CartDto;
import ua.com.atcorp.mobilecashdesk.ui.CartHistoryFragment.OnListFragmentInteractionListener;
import ua.com.atcorp.mobilecashdesk.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CartDto} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CartHistoryRecyclerViewAdapter extends RecyclerView.Adapter<CartHistoryRecyclerViewAdapter.ViewHolder> {

    private final List<CartDto> mValues;
    private final OnListFragmentInteractionListener mListener;

    public CartHistoryRecyclerViewAdapter(List<CartDto> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_carthistory_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CartDto item = mValues.get(position);
        holder.mItem = item;
        holder.mIdView.setText(String.format("%s", position + 1));
        String content = getDateString(item.createdOn);
        holder.mContentView.setText(content);
        String userName = item.createdBy == null ? null :item.createdBy.login;
        holder.mUserNameView.setText(userName);

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private String getDateString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = df.format(date);
        return dateString;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mUserNameView;
        public CartDto mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mContentView = view.findViewById(R.id.content);
            mUserNameView = view.findViewById(R.id.user_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
