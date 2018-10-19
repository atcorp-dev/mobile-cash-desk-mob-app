package ua.com.atcorp.mobilecashdesk.models;


import com.reactiveandroid.Model;
import com.reactiveandroid.annotation.Column;
import com.reactiveandroid.annotation.Index;
import com.reactiveandroid.annotation.IndexGroup;
import com.reactiveandroid.annotation.PrimaryKey;
import com.reactiveandroid.annotation.Table;
import com.reactiveandroid.annotation.Unique;

import java.io.Serializable;
import java.util.ArrayList;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;

@Table(name = "Items", database = AppDatabase.class,
        indexGroups = {
                @IndexGroup(groupNumber = 1, name = "index_RecordId"),
                @IndexGroup(groupNumber = 2, name = "index_Code"),
                @IndexGroup(groupNumber = 3, name = "index_BarCode")
        })
public class Item extends Model implements Serializable {

    @PrimaryKey(name = "_id")
    private Long _id;

    @Unique(unique = true)
    @Index(indexGroups = 1)
    @Column(name = "RecordId")
    private String recordId;

    @Index(indexGroups = 2)
    @Column(name = "Code")
    private String code;

    @Index(indexGroups = 3)
    @Column(name = "BarCode")
    private String barCode;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Price")
    private double price;

    @Column(name = "Category")
    private Category category;

    @Column(name = "Company")
    public Company company;

    @Column(name = "Available")
    boolean available;

    @Column(name = "Image")
    private String image;

    private ArrayList<ItemDto.AdditionalField> additionalFields;

    public Item() {
        super();
    }

    public Item(String recordId, String code, String barCode, String name, String description, double price, Category category, Company company, boolean available) {
        super();
        this.recordId = recordId;
        this.code = code;
        this.barCode = barCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.company = company;
        this.available = available;
    }

    @Override
    public String toString() {
        return name + " / " + code + " / " + barCode + " (" + price + ")";
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<ItemDto.AdditionalField> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(ArrayList<ItemDto.AdditionalField> additionalFields) {
        this.additionalFields = additionalFields;
    }
}
