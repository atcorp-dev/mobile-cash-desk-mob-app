package ua.com.atcorp.mobilecashdesk.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.widget.ListView;
import android.widget.Toast;

import com.reactiveandroid.query.Delete;
import com.reactiveandroid.query.Select;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ua.com.atcorp.mobilecashdesk.adapters.CartItemsAdapter;
import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.repositories.BaseRepository;
import ua.com.atcorp.mobilecashdesk.repositories.CartRepository;

public class CartService extends BaseRepository {

    private String mPreferencesFileName = "__cart__";
    private Cart mCart;
    private CartItemsAdapter mAdapter;
    private DataSetObserver mDataSetObserver;
    private CartRepository mCartRepository;
    private AuthService mAuthService;
    private boolean mCartClearing;

    // region Constructors

    public CartService(Context context) {
        super(context);
        mCartRepository = new CartRepository(context);
        mAuthService = new AuthService(context);
    }

    public CartService(Context context, DataSetObserver dataSetObserver) {
        super(context);
        mDataSetObserver = dataSetObserver;
        mCartRepository = new CartRepository(context);
        mAuthService = new AuthService(context);
    }

    // endregion

    // region Methods: Public

    public void bindAdapterToListView(ListView listView) {
        listView.setAdapter(mAdapter);
    }

    public Cart getCurrentCart() {
        if (mCart == null)
            restoreState();
        return mCart;
    }

    public double getTotalPrice() {
        return mAdapter.getTotalPrice();
    }

    public void addItem(Item item) {
        mAdapter.add(item);
    }

    public void clearCart() {
        mCartClearing = true;
        if(mAdapter != null)
            mAdapter.clear();
        SharedPreferences sharedPref = getSharedPreferences();
        sharedPref.edit().remove("modifiedOn").apply();
        sharedPref.edit().remove("cartId").commit();
        Delete.from(Cart.class).execute();
        Delete.from((CartItem.class)).execute();
        restoreState();
        mCartClearing = false;
    }

    public void saveState() {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cartId" , mCart.getRecordId().toString());
        editor.putString("modifiedOn" , getDateTimeNow());
        Exception error = mAdapter.saveState();
        mCartRepository.modify(mCart);
        if (error == null)
            editor.commit();
        else {
            editor.clear();
            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public CartService restoreState() {
        UUID cartId = mCart == null ? getActiveCartId() : mCart.getRecordId();
        if (mAuthService.getCurrentCompany() == null)
            return this;
        String companyId = mAuthService.getCurrentCompany().getRecordId();
        if(mCart == null) {
            mCart = getCartByRecordId(cartId);
            mCartRepository.create(companyId, mCart);
        }
        List<CartItem> cartItems = Select
                .from(CartItem.class)
                .where("cartId = ?", cartId.toString())
                .fetch();
        Collections.sort(cartItems, (c1, c2) -> c2.getDatetime().compareTo(c1.getDatetime()));
        if (cartItems != null & cartItems.size() > 0)
            mCart.setItems(new ArrayList<>(cartItems));
        mAdapter = new CartItemsAdapter(getContext(), mCart.getRecordId(), mCart.getItems());
        mAdapter.registerDataSetObserver(getDataSetObserver());
        if (mCartClearing)
            mCartRepository.create(companyId, mCart);
        return this;
    }

    public boolean hasItem(Item item) {
        if (item == null || mCart == null || mCart.getItems() == null)
            return false;
        for (CartItem cartItem : mCart.getItems())
            if (cartItem.getItemRecordId().equals(item.getRecordId()))
                return  true;
        return false;
    }

    public boolean isChanged(String timestamp) {
        String cartModifiedOn = getCartModifiedOn();
        if (timestamp == null || cartModifiedOn == null)
            return true;
        int compare = timestamp.compareTo(cartModifiedOn);
        return compare < 0;
    }

    public String getCartModifiedOn() {
        SharedPreferences sp = getSharedPreferences();
        String res = sp.getString("modifiedOn", null);
        return res;
    }

    // endregion

    // region Methods: Private

    private Cart getCartByRecordId(UUID recordId) {
        return getCartByRecordId(recordId, false);
    }

    private Cart getCartByRecordId(UUID recordId, boolean notCreate) {
        Cart cart = Select
                .from(Cart.class)
                .where("RecordId = ?", recordId.toString())
                .fetchSingle();
        if (cart == null && !notCreate)
            cart = new Cart(recordId);
        return  cart;
    }

    private UUID getActiveCartId() {
        SharedPreferences sharedPref = getSharedPreferences();
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

    private DataSetObserver getDataSetObserver() {
        return new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mCart.setItems(mAdapter.getItems());
                if (mDataSetObserver != null)
                    mDataSetObserver.onChanged();
                saveState();
            }
        };
    }

    private SharedPreferences getSharedPreferences() {
        SharedPreferences sharedPref = getContext()
                .getSharedPreferences(mPreferencesFileName, Context.MODE_PRIVATE);
        return  sharedPref;
    }

    private String getDateTimeNow() {
        Date now = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmss");
        String res = formater.format(now);
        return res;
    }

    // endregion
}
