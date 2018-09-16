package ua.com.atcorp.mobilecashdesk.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;
import java.util.UUID;

@Table(name="CartItems")
public class CartItem extends Model implements Serializable {

    @Column(name = "cartId", index = true)
    private String cartId;

    @Column(name = "qty")
    private int qty;

    @Column(name = "ItemRecordId", index = true)
    private String itemRecordId;

    @Column(name = "ItemCode", index = true)
    private String itemCode;

    @Column(name = "ItemBarCode", index = true)
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
