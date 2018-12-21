package ua.com.atcorp.mobilecashdesk.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.TransactionListRecyclerViewAdapter;
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
    final Calendar mCalendar = Calendar.getInstance();
    private Date mDateFrom = new Date();
    private List<TransactionDto> mTransactions;

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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactionlist_list, container, false);
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
            loadTransactions(view);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_print_z_report) {
            printReport();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadTransactions(View view) {

        View progressBar =  view.findViewById(R.id.progress);
        View listView = view.findViewById(R.id.list);
        RecyclerView recyclerView = (RecyclerView) listView;
        String companyId = getCompanyId();
        if (companyId != null) {
            TransactionRepository repo = new TransactionRepository(getContext());
            repo.getFinished(companyId, mDateFrom, (transactions, err) -> {
                progressBar.setVisibility(View.GONE);
                ArrayList<DummyItem> items = new ArrayList<>();
                if (err == null || transactions != null) {
                    mTransactions = transactions;
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
            loadTransactions(getView());
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

    private void printReport() {
        double totalAmount = 0;
        DateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append("<body>");
        sb.append("<div style=\"display: flex;justify-content: center; margin-top:24px\">");
        sb.append(String.format("<h4>Звіт за %s</h4>", dateFormat.format(mDateFrom)));
        sb.append("</div>");
        sb.append("<div style=\"display: flex;flex-direction: column;justify-content: center\">");
        for (TransactionDto dto : mTransactions) {
            totalAmount += dto.totalPrice;
            sb.append("<div style=\"margin-top: 12px;\">");
            sb.append(String.format("<div>Платіж №%s</div>", dto.documentNumber));
            sb.append(String.format("<div>Дата %s</div>", dateTimeFormat.format(dto.dateTime)));
            sb.append(String.format("<div>Статус: %s</div>", dto.status == 1 ? "Успішно" : "Відхилено"));
            sb.append(String.format("<div>Сума %s грн.</div>", decimalFormat.format(dto.totalPrice)));
            sb.append("</div>");
        }
        sb.append("<div style=\"margin-top: 12px;margin-bottom: 24px\">");
        sb.append(String.format("<h4>Всього %s грн.</h4>", decimalFormat.format(totalAmount)));
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</body?");
        sb.append("</html>");
        String report = sb.toString();
        if (mListener != null)
            mListener.onPrintReceipt(report);
    }

    public interface OnListFragmentInteractionListener {
        void onPrintReceipt(String receipt);
    }
}
