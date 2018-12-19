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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.TransactionListRecyclerViewAdapter;
import ua.com.atcorp.mobilecashdesk.dummy.DummyContent;
import ua.com.atcorp.mobilecashdesk.dummy.DummyContent.DummyItem;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.repositories.TransactionRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;
import ua.com.atcorp.mobilecashdesk.services.AuthService;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TransactionListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransactionListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TransactionListFragment newInstance(int columnCount) {
        TransactionListFragment fragment = new TransactionListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    private String getCompanyId() {
        AuthService auth = new AuthService(getContext());
        Company company = auth.getCurrentCompany();
        if (company != null)
            return company.getRecordId();
        return  null;
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
        View view = inflater.inflate(R.layout.fragment_transactionlist_list, container, false);
        View progressBar = view.findViewById(R.id.progress);
        View listView = view.findViewById(R.id.list);
        progressBar.setVisibility(View.VISIBLE);

        // Set the adapter
        if (listView instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) listView;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            String companyId = getCompanyId();
            if (companyId != null) {
                TransactionRepository repo = new TransactionRepository(getContext());
                repo.getPayed(companyId, new Date(), (transactions, err) -> {
                    progressBar.setVisibility(View.GONE);
                    ArrayList<DummyItem> items = new ArrayList<>();
                    if (err == null || transactions != null) {
                        int index = 0;
                        for(TransactionDto dto : transactions) {
                            String details = dto.extras == null ? null : dto.extras.receipt;
                            String content = getDateString(dto.dateTime);
                            items.add(new DummyItem(dto.documentNumber, content, details));
                        }
                    }
                    recyclerView.setAdapter(new TransactionListRecyclerViewAdapter(items, mListener));
                });
            }
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onPrintReceipt(String receipt);
    }

    private String getDateString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = df.format(date);
        return dateString;
    }
}
