package ua.com.atcorp.mobilecashdesk.models;

import com.reactiveandroid.Model;
import com.reactiveandroid.annotation.Column;
import com.reactiveandroid.annotation.Index;
import com.reactiveandroid.annotation.IndexGroup;
import com.reactiveandroid.annotation.PrimaryKey;
import com.reactiveandroid.annotation.Table;

import java.io.Serializable;
import java.util.UUID;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;

@Table(name = "CartItems", database = AppDatabase.class,
        indexGroups = {
                @IndexGroup(groupNumber = 1, name = "index_cartId"),
                @IndexGroup(groupNumber = 2, name = "index_ItemRecordId"),
                @IndexGroup(groupNumber = 3, name = "index_ItemCode"),
                @IndexGroup(groupNumber = 4, name = "index_ItemBarCode")
})
public class CartItem extends Model implements Serializable {

    @PrimaryKey(name = "_id")
    private Long id;

    @Index(indexGroups = 1)
    @Column(name = "cartId")
    private String cartId;

    @Column(name = "qty")
    private int qty;

    @Index(indexGroups = 2)
    @Column(name = "ItemRecordId")
    private String itemRecordId;

    @Index(indexGroups = 3)
    @Column(name = "ItemCode")
    private String itemCode;

    @Index(indexGroups = 4)
    @Column(name = "ItemBarCode")
    private String itemBarCode;

    @Column(name = "ItemName")
    private String itemName;

    @Column(name = "ItemPrice")
    private double itemPrice;

    @Column(name = "ItemCategory")
    private Category itemCategory;

    @Column(name = "ItemCompany")
    public Company itemCompany;

    @Column(name = "ItemImage")
    private String itemImage;

    public  CartItem() {
        super();
    }

    public CartItem(UUID cartId, Item item) {
        this.cartId = cartId.toString();
        this.qty = 1;
        this.itemRecordId = item.getRecordId();
        this.itemBarCode = item.getBarCode();
        this.itemCode = item.getCode();
        this.itemName = item.getName();
        this.itemPrice = item.getPrice();
        this.itemCategory = item.getCategory();
        this.itemCompany = item.getCompany();
        this.itemImage = item.getImage();

    }

    public String getCartId() {
        return cartId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getPrice() {
        return this.itemPrice * this.qty;
    }

    public String getItemRecordId() {
        return itemRecordId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemBarCode() {
        return itemBarCode;
    }

    public String getItemName() {
        return itemName;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public Category getItemCategory() {
        return itemCategory;
    }

    public Company getItemCompany() {
        return itemCompany;
    }

    public String getItemImage() {
        return itemImage;
    }
}