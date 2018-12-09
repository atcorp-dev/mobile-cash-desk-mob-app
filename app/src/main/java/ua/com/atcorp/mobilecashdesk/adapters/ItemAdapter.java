package ua.com.atcorp.mobilecashdesk.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.services.CartService;
import ua.com.atcorp.mobilecashdesk.ui.CartFragment;
import ua.com.atcorp.mobilecashdesk.ui.MainActivity;

public class ItemAdapter extends ArrayAdapter {

    ArrayList<Item> mItems;
    LayoutInflater mLayoutInflater;
    CartService mCartService;

    public ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
        mItems = items;
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
        String size = "";
        for(ItemDto.AdditionalField f : item.getAdditionalFields()) {
            if (f.name.equals("Розмір")) {
                size = "Розмір: " + f.value;
                break;
            }
        }
        setTextToView(view, R.id.item_size, size);
        setTextToView(view, R.id.item_available, String.format("На складі - %s", item.getAvailable() ? "Так" : "Ні"));

        View btn = view.findViewById(R.id.btnToCart);
        if (mCartService.hasItem(item)) {
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

    private void toCart(View view, Item item) {
        try {
            mCartService.addItem(item);
            mCartService.saveState();
            Toast.makeText(getContext(), "Відпарвилено до кошику", Toast.LENGTH_SHORT).show();
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
