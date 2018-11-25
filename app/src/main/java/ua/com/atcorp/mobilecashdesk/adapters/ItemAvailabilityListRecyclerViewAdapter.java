package ua.com.atcorp.mobilecashdesk.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.ui.dummy.DummyContent.DummyItem;

import java.util.List;

public class ItemAvailabilityListRecyclerViewAdapter extends RecyclerView.Adapter<ItemAvailabilityListRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;

    public ItemAvailabilityListRecyclerViewAdapter(List<DummyItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_itemavailabilitylist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DummyItem item = mValues.get(position);
        holder.mItem = item;
        holder.mIdView.setText(item.id);
        holder.mContentView.setText(item.content);
        holder.mDetailsView.setText(item.details);

        holder.mView.setOnClickListener(v -> {});
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mDetailsView;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            mDetailsView = (TextView) view.findViewById(R.id.details);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
