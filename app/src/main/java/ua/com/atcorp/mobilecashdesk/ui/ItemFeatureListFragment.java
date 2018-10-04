package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.ItemFeatureListRecyclerViewAdapter;
import ua.com.atcorp.mobilecashdesk.repositories.ItemRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.ui.dummy.DummyContent.DummyItem;


public class ItemFeatureListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_ITEM_ID = "item-id";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private String mItemId;

    private ArrayList<DummyItem> mItems = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFeatureListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFeatureListFragment newInstance(int columnCount, String itemId) {
        ItemFeatureListFragment fragment = new ItemFeatureListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_ITEM_ID, itemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mItemId = getArguments().getString(ARG_ITEM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itemfeaturelist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            ItemRepository repository = new ItemRepository();
            repository.getItemById(mItemId, (item, err) -> {
                //TODO: Error handling
                int n = 0;
                for(ItemDto.AdditionalField field : item.getAdditionalFields()) {
                    mItems.add(new DummyItem(++n + "", field.key, field.value));
                }
                //mItems.add(new DummyItem("1", "Колір", "Жовтий"));
                recyclerView.setAdapter(new ItemFeatureListRecyclerViewAdapter(mItems));
            }).execute();
        }
        return view;
    }
}
