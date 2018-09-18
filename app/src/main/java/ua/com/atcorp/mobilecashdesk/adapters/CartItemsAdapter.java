package ua.com.atcorp.mobilecashdesk.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.reactiveandroid.ReActiveAndroid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.R;

public class CartItemsAdapter extends ArrayAdapter {

    ArrayList<CartItem> mItems;
    LayoutInflater mLayoutInflater;
    UUID mCartId;

    public CartItemsAdapter(Context context, UUID cartId, ArrayList<CartItem> items) {
        super(context, 0, items);
        mItems = items;
        mCartId = cartId;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public long getItemId(int position) {
        // Item item = (Item)getItem(position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.cart_item, parent, false);
        } else {
            view = convertView;
        }
        CartItem cartItem = (CartItem) getItem(position);
        setTextToView(view, R.id.cart_item_num, position + 1);
        setTextToView(view, R.id.cart_item_name, cartItem.getItemName());
        setTextToView(view, R.id.cart_item_code, cartItem.getItemCode());
        setTextToView(view, R.id.cart_item_price, formatPrice(cartItem.getPrice()));
        setTextToView(view, R.id.cart_item_qty, cartItem.getQty());

        View btnInc = view.findViewById(R.id.cart_btn_inc);
        btnInc.setOnClickListener(v -> {
            increaseQty(cartItem);
            notifyDataSetChanged();
        });
        View btnDec = view.findViewById(R.id.cart_btn_dec);
        btnDec.setOnClickListener(v -> {
            decreaseQty(cartItem);
            notifyDataSetChanged();
        });
        return view;
    }

    private void setTextToView(View view, int textViewId, Object text) {
        TextView textView = view.findViewById(textViewId);
        if (textView != null)
            textView.setText(text.toString());
    }

    private void increaseQty(CartItem item) {
        int qty = item.getQty();
        item.setQty(qty + 1);
    }

    private void decreaseQty(CartItem item) {
        int qty = item.getQty();
        if (qty == 1) {
            remove(item);
            notifyDataSetChanged();
            return;
        }
        item.setQty(qty - 1);
    }

    @Override
    public void add(@Nullable Object object) {
        Item item = (Item) object;
        for(CartItem i : mItems) {
            String cartItemId = i.getItemRecordId();
            String itemId = item.getRecordId();
            if (cartItemId.equals(itemId)) {
                increaseQty(i);
                notifyDataSetChanged();
                return;
            }
        }
        if (item == null)
            super.add(item);
        else {
            CartItem cartItem = new CartItem(mCartId, item);
            mItems.add(cartItem);
            super.add(cartItem);
        }
    }

    @Override
    public void remove(@Nullable Object object) {
        mItems.remove(object);
        super.remove(object);
    }

    @Override
    public void clear() {
        ReActiveAndroid.getDatabase(AppDatabase.class).beginTransaction();
        for(CartItem item : mItems)
            item.delete();
        ReActiveAndroid.getDatabase(AppDatabase.class).endTransaction();
        super.clear();
    }

    public double getTotalPrice() {
        if (mItems == null || mItems.size() == 0)
            return 0;
        return mItems.stream()
                .mapToDouble(i -> i.getPrice())
                .sum();
    }

    public ArrayList<CartItem> getItems() {
        return mItems;
    }

    public Exception saveState() {
        Exception error = null;
        ReActiveAndroid.getDatabase(AppDatabase.class).beginTransaction();
        try {
            for (CartItem item : mItems) {
                item.save();
            }
            ReActiveAndroid.getDatabase(AppDatabase.class).endTransaction();
        } catch (Exception e) {
            error = e;
        }
        return error;
    }

    private String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        return strPrice;
    }
}
