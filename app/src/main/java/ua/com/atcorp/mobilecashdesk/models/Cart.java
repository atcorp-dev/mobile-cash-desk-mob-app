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

    public ArrayList<CartItem> getmItems() {
        return mItems;
    }

    public void setmItems(ArrayList<CartItem> mItems) {
        this.mItems = mItems;
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
}
