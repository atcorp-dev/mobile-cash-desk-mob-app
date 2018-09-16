package ua.com.atcorp.mobilecashdesk.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

@Table(name="Items")
public class Item extends Model implements Serializable {
    @Column(name = "RecordId", index = true, unique = true)
    private String recordId;

    @Column(name = "Code", index = true)
    private String code;

    @Column(name = "BarCode", index = true)
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

    @Column(name = "Image")
    private String image;

    public Item() {
        super();
    }

    public Item(String recordId, String code, String barCode, String name, String description, double price, Category category, Company company, String image) {
        super();
        this.recordId = recordId;
        this.code = code;
        this.barCode = barCode;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.company = company;
        this.image = image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
