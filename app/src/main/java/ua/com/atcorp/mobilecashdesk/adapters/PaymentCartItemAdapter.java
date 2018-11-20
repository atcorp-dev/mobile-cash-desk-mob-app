package ua.com.atcorp.mobilecashdesk.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.models.CartItem;

public class PaymentCartItemAdapter extends ArrayAdapter {

    ArrayList<CartItem> mItems;
    LayoutInflater mLayoutInflater;

    public PaymentCartItemAdapter(Context context, ArrayList<CartItem> items) {
        super(context, R.layout.payment_cart_item, items);
        mItems = items;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.payment_cart_item, parent, false);
        } else {
            view = convertView;
        }
        CartItem cartItem = (CartItem) getItem(position);
        setTextToView(view, R.id.cart_item_name, cartItem.getItemName());
        setTextToView(view, R.id.cart_item_qty, "x" + cartItem.getQty());
        setTextToView(view, R.id.cart_item_price, formatPrice(cartItem.getPrice()));
        return view;
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
}
