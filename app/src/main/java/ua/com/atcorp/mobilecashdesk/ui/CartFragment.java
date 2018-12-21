package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.repositories.ItemRepository;
import ua.com.atcorp.mobilecashdesk.services.AuthService;
import ua.com.atcorp.mobilecashdesk.services.CartService;

public class CartFragment extends Fragment {

    ListView mListView;
    EditText mItemCodeView;
    ProgressBar mProgressBar;
    CartService mCartService;
    Cart mCart;
    ItemRepository mRepository;
    boolean cartInitialized;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment, container, false);
        mCartService = new CartService(getContext(), getDataSetObserver(view));
        mCartService.restoreState();
        mCart = mCartService.getCurrentCart();
        mListView = view.findViewById(R.id.list_view);
        double price = mCartService.getTotalPrice();
        setTotalPrice(view, price);
        mCartService.bindAdapterToListView(mListView);
        mItemCodeView = view.findViewById(R.id.et_item_code);
        mItemCodeView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        onButtonSearchClick(v);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });
        initCart(view);
        cartInitialized = true;
        Button searchButton = view.findViewById(R.id.item_btn_search);
        searchButton.setOnClickListener(v -> onButtonSearchClick(v));
        Button scanButton = view.findViewById(R.id.item_btn_scan);
        scanButton.setOnClickListener(v -> onButtonScanClick(v));
        Button payButton = view.findViewById(R.id.btn_pay);
        payButton.setOnClickListener(v -> onButtonPayClick(v));
        Button clearButton = view.findViewById(R.id.btn_clear);
        clearButton.setOnClickListener(v -> onButtonClearClick(v));
        view.findViewById(R.id.btn_expand_top_layout)
                .setOnClickListener(v -> onButtonExpandTopLayoutClick(v));
        view.findViewById(R.id.btn_collapse_top_layout)
                .setOnClickListener(v -> onButtonCollapseTopLayoutClick(v));
        view.findViewById(R.id.btn_expand_bottom_layout)
                .setOnClickListener(v -> onButtonExpandBottomLayoutClick(v));
        view.findViewById(R.id.btn_collapse_bottom_layout)
                .setOnClickListener(v -> onButtonCollapseBottomLayoutClick(v));
        mRepository = new ItemRepository(getContext());
        mProgressBar = view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!cartInitialized)
            initCart(getView());
        else
            cartInitialized = false;
    }

    public void addItemByBarCode(String barCode) {
        showProgress();
        Company company = new AuthService(getContext()).getCurrentCompany();
        mRepository.getItemByBarCode(company.getRecordId(), barCode, (item, error) -> {
            hideProgress();
            if (error != null) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            } else if (item != null)
                addItem(item);
            else
                Toast.makeText(getContext(), "Товар не знайдено", Toast.LENGTH_SHORT).show();
            mItemCodeView.setText("");
        });
    }

    private void initCart(View view) {
        mCartService = new CartService(getContext(), getDataSetObserver(view));
        mCartService.restoreState();
        mCart = mCartService.getCurrentCart();
        mListView = view.findViewById(R.id.list_view);
        double price = mCartService.getTotalPrice();
        setTotalPrice(view, price);
        mCartService.bindAdapterToListView(mListView);
        mItemCodeView = view.findViewById(R.id.et_item_code);
    }

    private void onButtonSearchClick(View v) {
        showProgress();
        String code = mItemCodeView.getText().toString();
        Company company = new AuthService(getContext()).getCurrentCompany();
        mRepository.getItemByCode(company.getRecordId(), code, (item, error) -> {
            hideProgress();
            if (error != null) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            } else if (item != null)
                addItem(item);
            else
                Toast.makeText(getContext(), "Товар не знайдено", Toast.LENGTH_SHORT).show();
            mItemCodeView.setText("");
        });

    }

    private void onButtonClearClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setTitle("Ви дійсно бажаєте очистити кошик?")
                .setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> clearCart())
                .setNegativeButton(R.string.no, null);
        builder.create().show();

    }

    private void onButtonScanClick(View view) {
        MainActivity ma = (MainActivity) getContext();
        ma.scanBarCode();
    }

    private void onButtonPayClick(View view) {
        MainActivity ma = (MainActivity) getContext();
        if (mCart.getClientInfo() == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Номер телефону клієнта");
            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
            builder.setView(input);
            builder.setPositiveButton("Так", (dialog, which) -> {
                String clientPhone = input.getText().toString();
                mCart.setClientInfo(clientPhone);
                mCart.save();
                mCartService.saveState();
                ma.makePayment(mCart.getRecordId().toString(), mCartService.getTotalPrice());
            });
            builder.setNegativeButton("Ні", (dialog, which) -> {
                dialog.cancel();
                ma.makePayment(mCart.getRecordId().toString(), mCartService.getTotalPrice());
            });
            builder.show();
            input.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus)
                    v.post(() -> showKeyboard(v));
            });
        } else {
            ma.makePayment(mCart.getRecordId().toString(), mCartService.getTotalPrice());
        }
    }

    private void onButtonExpandTopLayoutClick(View view) {
        getView().findViewById(R.id.search_wrap).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.btn_collapse_top_layout).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        mItemCodeView.requestFocus();
        showKeyboard(mItemCodeView);
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void onButtonCollapseTopLayoutClick(View view) {
        getView().findViewById(R.id.search_wrap).setVisibility(View.GONE);
        getView().findViewById(R.id.btn_expand_top_layout).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
    }

    private void onButtonExpandBottomLayoutClick(View view) {
        getView().findViewById(R.id.bottom_expandable_wrap).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.btn_collapse_bottom_layout).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
    }

    private void onButtonCollapseBottomLayoutClick(View view) {
        getView().findViewById(R.id.bottom_expandable_wrap).setVisibility(View.GONE);
        getView().findViewById(R.id.btn_expand_bottom_layout).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
    }

    private void clearCart() {
        mCartService.clearCart();
        mCart = mCartService.getCurrentCart();
    }

    private void addItem(Item item) {
        mCartService.addItem(item);
    }

    private void setTotalPrice(View view, double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        ((TextView) view.findViewById(R.id.tvTotalPrice))
                .setText(strPrice);
    }

    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    private DataSetObserver getDataSetObserver(View view) {
        return new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                double price = mCartService.getTotalPrice();
                setTotalPrice(view, price);
            }
        };
    }
}
