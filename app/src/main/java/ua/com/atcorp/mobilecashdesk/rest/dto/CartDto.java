package ua.com.atcorp.mobilecashdesk.rest.dto;

import java.util.Date;
import java.util.ArrayList;

public class CartDto {

    public static class CartItemDto {
        public String id;
        public String cartId;
        public String name;
        public String code;
        public String barCode;
        public Double price;
        public Double discount;
        public int qty;
        public Date dateTime;
        public CompanyDto company;
        public String companyId;
        public String image;
    }

    public String id;
    public int type;
    public String clientInfo;
    public ArrayList<CartItemDto> items;
    public ArrayList<CartDto> history;
}
