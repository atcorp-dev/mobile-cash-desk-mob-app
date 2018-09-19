package ua.com.atcorp.mobilecashdesk.models;

import com.reactiveandroid.Model;
import com.reactiveandroid.annotation.Column;
import com.reactiveandroid.annotation.Index;
import com.reactiveandroid.annotation.IndexGroup;
import com.reactiveandroid.annotation.PrimaryKey;
import com.reactiveandroid.annotation.Table;

import java.io.Serializable;
import java.util.List;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;

@Table(name = "Companies", database = AppDatabase.class,
        indexGroups = {
                @IndexGroup(groupNumber = 1, name = "index_RecordId"),
                @IndexGroup(groupNumber = 2, name = "index_Code")
        })
public class Company extends Model implements Serializable {

    @PrimaryKey(name = "_id")
    private Long _id;

    @Index(indexGroups = 1)
    @Column(name = "RecordId")
    public String recordId;

    @Index(indexGroups = 2)
    @Column(name = "Code")
    private String code;

    @Column(name = "Name")
    private String name;

    @Column(name = "Phone")
    private String phone;

    @Column(name = "Email")
    private String email;

    @Column(name = "Address")
    private String address;

    @Column(name = "Parent")
    public Company parent;

    /*public List<Company> children() {
        return getMany(Company.class, "Parent");
    }

    public List<Item> itemList() {
        return getMany(Item.class, "Company");
    }*/

    public Company() {
        super();
    }

    public Company(String recordId, String code, String name, String phone, String email, String address) {
        super();
        this.recordId = recordId;
        this.code = code;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
