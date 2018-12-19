package ua.com.atcorp.mobilecashdesk.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.ui.TransactionListFragment.OnListFragmentInteractionListener;
import ua.com.atcorp.mobilecashdesk.dummy.DummyContent.DummyItem;

import java.util.List;

public class TransactionListRecyclerViewAdapter extends RecyclerView.Adapter<TransactionListRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public TransactionListRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_transactionlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DummyItem item =mValues.get(position);
        holder.mItem = item;
        String numberText = String.format("№: %s", item.id);
        holder.mIdView.setText(numberText);
        String content = String.format("Дата: %s", item.content);
        holder.mContentView.setText(content);

        View btnPrint = holder.mView.findViewById(R.id.btnDetails);
        if (TextUtils.isEmpty(item.details))
            btnPrint.setVisibility(View.GONE);

        btnPrint.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onPrintReceipt(holder.mItem.details);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
