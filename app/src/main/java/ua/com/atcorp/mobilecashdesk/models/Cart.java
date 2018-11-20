package ua.com.atcorp.mobilecashdesk.models;

import com.reactiveandroid.Model;
import com.reactiveandroid.annotation.Column;
import com.reactiveandroid.annotation.PrimaryKey;
import com.reactiveandroid.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;

@Table(name = "Carts", database = AppDatabase.class)
public class Cart extends Model implements Serializable {

    @PrimaryKey(name = "_id")
    private Long _id;

    @Column(name = "RecordId")
    private UUID mRecordId;

    @Column(name = "Type")
    private int mType;

    @Column(name = "ClientInfo")
    private String mClientInfo;

    private ArrayList<CartItem> mItems;

    public Cart() {
        mRecordId = UUID.randomUUID();
        mItems = new ArrayList<>();
    }

    public Cart(UUID recordId) {
        mRecordId = recordId;
        mItems = new ArrayList<>();
    }

    public UUID getRecordId() {
        return  mRecordId;
    }

    public void setRecordId(UUID recordId) {
        mRecordId = recordId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public ArrayList<CartItem> getItems() {
        return mItems;
    }

    public void setItems(ArrayList<CartItem> mItems) {
        this.mItems = mItems;
    }

    public String getClientInfo() {
        return this.mClientInfo;
    }

    public void setClientInfo(String cleintInfo) {
        this.mClientInfo = cleintInfo;
    }

    public Cart addItem(CartItem item) {
        mItems.add(item);
        return this;
    }

    public boolean hasItemId(String itemId) {
        for (CartItem item : mItems)
            if (item.getItemRecordId().equals(itemId))
                return true;
        return false;
    }

    public double getTotalPrice() {
        double res = 0;
        for (CartItem item : mItems)
            res += item.getPrice();
        return  res;
    }

    public double getDiscount() {
        double res = 0;
        for (CartItem item : mItems)
            res += item.getDiscount();
        return  res;
    }
}
