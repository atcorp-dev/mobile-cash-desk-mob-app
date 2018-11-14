package ua.com.atcorp.mobilecashdesk.rest.dto;

import java.util.ArrayList;
import java.util.Date;

public class TransactionDto {

    enum PaymentType {
        Card,
        Cash
    }

    enum TransactionStatus {
        Pending,
        Payed,
        Rejected
    }

    public String id;
    public String companyId;
    public PaymentType type;
    public Date dateTime;
    public TransactionStatus sattus;
    public String ownerId;
    public ArrayList<ItemDto> itemList;
    public double totalPrice;
}
