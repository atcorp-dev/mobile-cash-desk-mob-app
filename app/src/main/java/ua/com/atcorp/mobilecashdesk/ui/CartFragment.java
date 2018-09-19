package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.reactiveandroid.query.Select;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ua.com.atcorp.mobilecashdesk.adapters.CartItemsAdapter;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.repositories.ItemRepository;

public class CartFragment extends Fragment {

    ListView mListView;
    EditText mItemCodeView;
    ProgressBar mProgressBar;
    CartItemsAdapter mAdapter;
    ArrayList<CartItem> mItems = new ArrayList<>();
    ItemRepository mRepository;
    private static String mPreferencesFileName = "__cart__";
    UUID mCartId;

    public static UUID getActiveCartId(Context ctx) {
        SharedPreferences sharedPref = getSharedPreferences(ctx);
        String cartId = sharedPref.getString("cartId", "");
        if (cartId.isEmpty()) {
            UUID cartUId =  UUID.randomUUID();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("cartId" , cartUId.toString());
            editor.commit();
            return cartUId;
        } else {
            return UUID.fromString(cartId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment, container, false);
        restoreState();
        mListView = view.findViewById(R.id.list_view);
        mAdapter = new CartItemsAdapter(getContext(), mCartId, mItems);
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                double price = mAdapter.getTotalPrice();
                setTotalPrice(view, price);
                saveState();
            }
        });
        double price = mAdapter.getTotalPrice();
        setTotalPrice(view, price);
        mListView.setAdapter(mAdapter);
        mItemCodeView = view.findViewById(R.id.et_item_code);
        Button searchButton = view.findViewById(R.id.item_btn_search);
        searchButton.setOnClickListener(v -> onButtonSearchClick(v));
        Button scanButton = view.findViewById(R.id.item_btn_scan);
        scanButton.setOnClickListener(v -> onButtonScanClick(v));
        Button payButton = view.findViewById(R.id.btn_pay);
        payButton.setOnClickListener(v -> onButtonPayClick(v));
        Button clearButton = view.findViewById(R.id.btn_clear);
        clearButton.setOnClickListener(v -> onButtonClearClick(v));
        mRepository = new ItemRepository();
        mProgressBar = view.findViewById(R.id.progress);
        return view;
    }

    public void onButtonSearchClick(View v) {
        showProgress();
        String code = mItemCodeView.getText().toString();
        Company company = MainActivity.getCompany();
        mRepository.getItemByCode(company.getRecordId(), code, (item, error) -> {
            hideProgress();
            if (error != null) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            } else if (item != null)
                addItem(item);
            else
                Toast.makeText(getContext(), "Товар не знайдено", Toast.LENGTH_SHORT).show();
            mItemCodeView.setText("");
        }).execute();

    }

    public void onButtonClearClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Ви дійсно бажаєте очистити кошик")
                .setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> clearCart())
                .setNegativeButton(R.string.no, null);
        builder.create().show();

    }

    public void onButtonScanClick(View view) {
        MainActivity ma = (MainActivity) getContext();
        ma.scanBarCode();
    }

    public void onButtonPayClick(View view) {
        MainActivity ma = (MainActivity) getContext();
        ma.makePayment(mCartId.toString(), mAdapter.getTotalPrice());
    }

    public void clearCart() {
        mAdapter.clear();
        SharedPreferences sharedPref = getSharedPreferences(getContext());
        sharedPref.edit().remove("cartId").commit();
    }

    public void addItem(Item item) {
        mAdapter.add(item);
    }

    public void addItemByBarCode(String barCode) {
        showProgress();
        Company company = MainActivity.getCompany();
        mRepository.getItemByBarCode(company.getRecordId(), barCode, (item, error) -> {
            hideProgress();
            if (error != null) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            } else if (item != null)
                addItem(item);
            else
                Toast.makeText(getContext(), "Товар не знайдено", Toast.LENGTH_SHORT).show();
            mItemCodeView.setText("");
        }).execute();
    }

    private void setTotalPrice(View view, double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        ((TextView) view.findViewById(R.id.tvTotalPrice))
                .setText(strPrice);
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(mPreferencesFileName, Context.MODE_PRIVATE);
        return  sharedPref;
    }

    private void saveState() {
        SharedPreferences sharedPref = getSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cartId" , mCartId.toString());
        Exception error = mAdapter.saveState();
        if (error == null)
            editor.commit();
        else {
            editor.clear();
            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void restoreState() {
        if (mCartId == null) {
            mCartId = getActiveCartId(getContext());
        }
        List<CartItem> cartItems = Select
                .from(CartItem.class)
                .where("cartId = ?", mCartId.toString())
                .fetch();
        if (cartItems != null & cartItems.size() > 0)
            mItems = new ArrayList<>(cartItems);
        else
            mItems = new ArrayList<>();
    }

    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }
}
