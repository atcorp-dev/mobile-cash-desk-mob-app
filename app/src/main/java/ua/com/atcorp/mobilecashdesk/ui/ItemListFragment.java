package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.reactiveandroid.query.Delete;

import java.util.ArrayList;
import java.util.List;

import ua.com.atcorp.mobilecashdesk.adapters.ItemAdapter;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.repositories.CompanyRepository;
import ua.com.atcorp.mobilecashdesk.repositories.ItemRepository;
import ua.com.atcorp.mobilecashdesk.services.AuthService;

public class ItemListFragment extends Fragment implements TextWatcher {

    Company mCompany;
    ArrayAdapter mItemAdapter;
    EditText mEtName;
    View mBtnSearch;
    ProgressBar mProgressBar;
    int minSearchTextLength = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCompany = new AuthService(getContext()).getCurrentCompany();
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
        mEtName.post(() -> showKeyboard(mEtName));
        mEtName.requestFocus();
        mEtName.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        getItemsByName();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });
        return view;
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

    private void getItemsByName() {
        ItemRepository repository = new ItemRepository(getContext());
        String name = mEtName.getText().toString();
        if (name == null || name.length() < minSearchTextLength) {
            hideProgress();
            return;
        }
        showProgress();
        repository.getItemsByName(
                mCompany.getRecordId(), name, (items, err) -> onGetItemsResponse(items, err)
        );
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

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

}
