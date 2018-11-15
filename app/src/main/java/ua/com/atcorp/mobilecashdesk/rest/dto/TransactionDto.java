package ua.com.atcorp.mobilecashdesk.rest.dto;

import java.util.ArrayList;
import java.util.Date;

import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.CartItem;

public class TransactionDto {

    public class TransactionItemDto {

        public String itemId;
        public String name;
        public String code;
        public String barCode;
        public int qty;
        public double price;

        public TransactionItemDto() {}

        public TransactionItemDto(CartItem cartItem) {
            itemId = cartItem.getItemRecordId();
            name  = cartItem.getItemName();
            code  = cartItem.getItemCode();
            barCode = cartItem.getItemBarCode();
            qty = cartItem.getQty();
            price = cartItem.getItemPrice();
        }
    }

    public String id;
    public String cartId;
    public String companyId;
    public int type;
    public Date dateTime;
    public int status;
    public String ownerId;
    public ArrayList<TransactionItemDto> itemList;
    public double totalPrice;
    public String orderNum;

    public TransactionDto() {}

    public TransactionDto(Cart cart) {
        if (cart == null) {
            return;
        }
        cartId = cart.getRecordId().toString();
        type = cart.getType();
        dateTime = new Date();
        itemList = new ArrayList<>();
        for (CartItem cartItem: cart.getmItems()) {
            TransactionItemDto dto = new TransactionItemDto(cartItem);
            itemList.add(dto);
        }
        totalPrice = cart.getTotalPrice();
    }

    public String getOrderNumPrint() {
      return "<html>"
      + "<body>"
              + "<div style='width: 380px; height: 240px; display: flex; flex-direction: column; justify-content: center; align-items: center'>"
              + "<div style='width: 100%; display: flex; justify-content: center; align-items: center'>"
              + "<h2>Номер замовлення</h2>"
              + "</div>"
              + "<div div style='width: 100%; display: flex; justify-content: center; align-items: center'>"
              + "<h1>" + this.orderNum + "</h1>"
              + "</div>"
              + "</div>"
              + "</body>"
              + "</html>";
    }
}
