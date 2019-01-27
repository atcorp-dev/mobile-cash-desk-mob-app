package ua.com.atcorp.mobilecashdesk.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;
import ua.com.atcorp.mobilecashdesk.ui.TransactionListFragment.OnListFragmentInteractionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TransactionListRecyclerViewAdapter extends RecyclerView.Adapter<TransactionListRecyclerViewAdapter.ViewHolder> {

    private final List<TransactionDto> mValues;
    private final OnListFragmentInteractionListener mListener;

    public TransactionListRecyclerViewAdapter(List<TransactionDto> items, OnListFragmentInteractionListener listener) {
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
        TransactionDto item = mValues.get(position);
        holder.mItem = item;
        String numberText = String.format("№: %s", item.documentNumber);
        holder.mIdView.setText(numberText);
        String dateTimeStr = getDateString(item.dateTime);
        String content = String.format("Дата: %s", dateTimeStr);
        holder.mContentView.setText(content);

        View btnPrint = holder.mView.findViewById(R.id.btnDetails);
        View btnUAmadePrint = holder.mView.findViewById(R.id.btnUAmadeDetails);
        String receipt = item.extras == null ? null : item.extras.receipt;
        if (TextUtils.isEmpty(receipt)) {
            btnPrint.setVisibility(View.GONE);
        } else {
            btnPrint.setOnClickListener(v -> {
                if (null != mListener) {
                    mListener.onPrintReceipt(holder.mItem.extras.receipt);
                }
            });
        }

        String UAmadeReceipt = item.extras == null ? null : item.extras.UAmadeReceipt;
        if (TextUtils.isEmpty(UAmadeReceipt)) {
            btnUAmadePrint.setVisibility(View.GONE);
        } else {
            btnUAmadePrint.setOnClickListener(v -> {
                if (null != mListener) {
                    mListener.onPrintReceipt(holder.mItem.extras.UAmadeReceipt);
                }
            });
        }
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
        public TransactionDto mItem;

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
