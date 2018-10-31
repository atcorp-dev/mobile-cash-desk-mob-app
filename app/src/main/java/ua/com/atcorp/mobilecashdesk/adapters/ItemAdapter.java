package ua.com.atcorp.mobilecashdesk.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.reactiveandroid.query.Select;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.services.CartService;
import ua.com.atcorp.mobilecashdesk.ui.CartFragment;
import ua.com.atcorp.mobilecashdesk.ui.MainActivity;

public class ItemAdapter extends ArrayAdapter {

    ArrayList<Item> mItems;
    LayoutInflater mLayoutInflater;
    ArrayList<String> mItemsInCart;
    CartService mCartService;

    public ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
        mItems = items;
        mItemsInCart = new ArrayList<>();
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCartService = new CartService(getContext());
        mCartService.restoreState();
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.item, parent, false);
        } else {
            view = convertView;
        }
        Item item = (Item) getItem(position);
        setTextToView(view, R.id.item_num, position + 1);
        setTextToView(view, R.id.item_name, item.getName());
        setTextToView(view, R.id.item_code, item.getCode());
        setTextToView(view, R.id.item_price, formatPrice(item.getPrice()));

        View btn = view.findViewById(R.id.btnToCart);
        if (mItemsInCart.contains(item.getRecordId())) {
            btn.setEnabled(false);
        } else {
            btn.setEnabled(true);
            btn.setOnClickListener(v -> {
                toCart(v, item);
                notifyDataSetChanged();
            });
        }
        view.findViewById(R.id.btnDetail)
                .setOnClickListener(v -> openCartItemDetailView(item));
        return view;
    }

    @Override
    public void clear() {
        mItemsInCart.clear();
        mItemsInCart = new ArrayList<>();
        super.clear();
    }

    private void toCart(View view, Item item) {
        try {
            mCartService.addItem(item);
            mCartService.saveState();
            Toast.makeText(getContext(), "Відпарвилено до кошику", Toast.LENGTH_SHORT).show();
            mItemsInCart.add(item.getRecordId());
        } catch (Exception err) {
            Toast.makeText(getContext(), err.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("EXCEPTION", err.getMessage());
        }
    }

    private void setTextToView(View view, int textViewId, Object text) {
        TextView textView = view.findViewById(textViewId);
        if (textView != null)
            textView.setText(text.toString());
    }

    private String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        return strPrice;
    }

    private void openCartItemDetailView(Item item) {
        MainActivity ma = (MainActivity) getContext();
        ma.openCatalogueItemActivity(item);
    }
}
