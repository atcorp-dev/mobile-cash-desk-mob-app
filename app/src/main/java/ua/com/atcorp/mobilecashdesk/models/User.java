package ua.com.atcorp.mobilecashdesk.models;

import com.reactiveandroid.Model;
import com.reactiveandroid.annotation.Column;
import com.reactiveandroid.annotation.Index;
import com.reactiveandroid.annotation.IndexGroup;
import com.reactiveandroid.annotation.PrimaryKey;
import com.reactiveandroid.annotation.Table;

import java.io.Serializable;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;

@Table(name = "Users", database = AppDatabase.class,
        indexGroups = {
                @IndexGroup(groupNumber = 1, name = "index_RecordId"),
                @IndexGroup(groupNumber = 2, name = "index_login")
        })
public class User extends Model implements Serializable {

    @PrimaryKey(name = "_id")
    private Long _id;

    @Index(indexGroups = 1)
    @Column(name = "RecordId")
    private String recordId;

    @Index(indexGroups = 2)
    @Column(name = "Login")
    private String login;

    @Column(name = "Name")
    private String name;

    @Column(name = "CompanyId")
    private String companyId;

    private Company company;

    @Column(name = "Email")
    private String email;

    public Long get_id() {
        return _id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
