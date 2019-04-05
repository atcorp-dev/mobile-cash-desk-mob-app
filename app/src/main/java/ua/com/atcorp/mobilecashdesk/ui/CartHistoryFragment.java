package ua.com.atcorp.mobilecashdesk.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.CartHistoryRecyclerViewAdapter;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.repositories.CartRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.CartDto;
import ua.com.atcorp.mobilecashdesk.services.AuthService;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnCartHistoryListFragmentInteractionListener}
 * interface.
 */
public class CartHistoryFragment extends Fragment {

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private CartRepository mCartRepository;
    private OnCartHistoryListFragmentInteractionListener mListener;
    final Calendar mCalendar = Calendar.getInstance();
    private Date mDateFrom = new Date();
    private List<CartDto> mCartHistoryList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CartHistoryFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CartHistoryFragment newInstance() {
        CartHistoryFragment fragment = new CartHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCartHistoryListFragmentInteractionListener) {
            mListener = (OnCartHistoryListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCartRepository = new CartRepository(getContext());
        View view = inflater.inflate(R.layout.fragment_carthistory_list, container, false);
        View progressBar = view.findViewById(R.id.progress);
        View listView = view.findViewById(R.id.list);
        progressBar.setVisibility(View.VISIBLE);

        initDateInput(view);

        // Set the adapter
        if (listView instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) listView;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            loadCartHistory(view);
        }
        return view;
    }

    private void loadCartHistory(View view) {

        View progressBar =  view.findViewById(R.id.progress);
        View listView = view.findViewById(R.id.list);
        RecyclerView recyclerView = (RecyclerView) listView;
        String companyId = getCompanyId();

        if (companyId != null) {
            mCartRepository.getCarts(companyId, mDateFrom, (historyData, err) -> {
                progressBar.setVisibility(View.GONE);
                if (err == null || historyData != null) {
                    mCartHistoryList = historyData;
                }
                recyclerView.setAdapter(new CartHistoryRecyclerViewAdapter(historyData, mListener));
            });
        }
    }

    private String getCompanyId() {
        AuthService auth = new AuthService(getContext());
        Company company = auth.getCurrentCompany();
        if (company != null)
            return company.getRecordId();
        return  null;
    }

    private void initDateInput(View view) {
        EditText edittext= view.findViewById(R.id.dateFrom);
        View datePicker = view.findViewById(R.id.date_picker);
        updateLabel(edittext);

        DatePickerDialog.OnDateSetListener date = (datePickerView, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(edittext);
            mDateFrom = getDateFromDatePicker(datePickerView);
            loadCartHistory(getView());
        };

        datePicker.setOnClickListener(v -> {

            new DatePickerDialog(
                    getContext(),
                    date,
                    mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    private void updateLabel(EditText edittext) {
        String format = "dd.MM.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        edittext.setText(sdf.format(mCalendar.getTime()));
    }

    private String getDateString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = df.format(date);
        return dateString;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCartHistoryListFragmentInteractionListener {
        // TODO: Update argument type and name
        void openCartHistoryDetail(CartDto item);
    }
}
