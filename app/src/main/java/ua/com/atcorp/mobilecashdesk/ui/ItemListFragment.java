package ua.com.atcorp.mobilecashdesk.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.activeandroid.query.Delete;

import java.util.ArrayList;
import java.util.List;

import ua.com.atcorp.mobilecashdesk.Adapters.ItemAdapter;
import ua.com.atcorp.mobilecashdesk.Models.Company;
import ua.com.atcorp.mobilecashdesk.Models.Item;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.Repositories.ItemRepository;

public class ItemListFragment extends Fragment implements TextWatcher {

    Company mCompany = MainActivity.getCompany();
    ArrayAdapter mItemAdapter;
    EditText mEtName;
    View mBtnSearch;
    ProgressBar mProgressBar;
    int minSearchTextLength = 3;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_list_fragment, container, false);
        ListView listView = view.findViewById(R.id.list_view);
        mItemAdapter = new ItemAdapter(
                getContext(), new ArrayList()
        );
        listView.setAdapter(mItemAdapter);
        mEtName = view.findViewById(R.id.etName);
        mEtName.addTextChangedListener(this);
        mBtnSearch = view.findViewById(R.id.btnSearch);
        mBtnSearch.setOnClickListener(v -> getItemsByName());
        mBtnSearch.setEnabled(false);
        mProgressBar = view.findViewById(R.id.progress);
        return view;
    }

    public static void ResetCache() {
        new Delete().from(Item.class).execute();
    }

    private void getItemsByName() {
        ItemRepository repository = new ItemRepository();
        String name = mEtName.getText().toString();
        if (name == null || name.length() < minSearchTextLength) {
            hideProgress();
            return;
        }
        showProgress();
        repository.getItemsByName(
                mCompany.getRecordId(), name, (items, err) -> onGetItemsResponse(items, err)
        ).execute();
    }

    private void onGetItemsResponse(List<Item> items, Exception err) {
        hideProgress();
        if (err != null) {
            Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_LONG).show();
        } else if (items != null) {
            mEtName.setText("");
            mItemAdapter.clear();
            mItemAdapter.addAll(items);
        }
    }

    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String name = mEtName.getText().toString();
        boolean enabled = false;
        if (name != null && name.length() >= minSearchTextLength)
            enabled = true;
        mBtnSearch.setEnabled(enabled);
    }
}
