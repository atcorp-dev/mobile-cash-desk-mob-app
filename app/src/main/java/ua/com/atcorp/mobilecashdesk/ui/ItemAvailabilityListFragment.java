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
import android.widget.Toast;

import java.util.ArrayList;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.ItemAvailabilityListRecyclerViewAdapter;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.repositories.ItemRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.services.AuthService;
import ua.com.atcorp.mobilecashdesk.ui.dummy.DummyContent;
import ua.com.atcorp.mobilecashdesk.ui.dummy.DummyContent.DummyItem;

public class ItemAvailabilityListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private static Item mItem;
    private ArrayList<DummyItem> mItems = new ArrayList<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemAvailabilityListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemAvailabilityListFragment newInstance(int columnCount, Item item) {
        ItemAvailabilityListFragment fragment = new ItemAvailabilityListFragment();
        mItem = item;
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itemavailabilitylist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            ItemRepository repository = new ItemRepository(getContext());
            AuthService authService = new AuthService(getContext());
            String companyId = authService.getCurrentCompany().getRecordId();
            repository.getAvailable(companyId, mItem.getCode(), (items, err) -> {
                if (err != null) {
                    Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_LONG);
                    err.printStackTrace();
                    return;
                }
                int n = 0;
                for(Item field : items) {
                    Company company = field.getCompany();
                    String details = "";
                    if (company.getAddress() != null)
                        details += "Адреса: " + company.getAddress() + "\n";
                    if (company.getPhone() != null)
                        details += "Тел.: " + company.getPhone() + "\n";
                    if (company.getEmail() != null)
                        details += "E-Mail: " + company.getEmail() + "\n";
                    mItems.add(new DummyItem(++n + "", company.getName(), details.trim()));
                }
                recyclerView.getAdapter().notifyDataSetChanged();
            });
            recyclerView.setAdapter(new ItemAvailabilityListRecyclerViewAdapter(mItems));
        }
        return view;
    }
}
