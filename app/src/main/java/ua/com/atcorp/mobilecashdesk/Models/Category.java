package ua.com.atcorp.mobilecashdesk.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name="Categories")
public class Category extends Model {

    @Column(name = "RecordId", index = true, unique = true)
    private String recordId;

    @Column(name = "Name")
    private String name;

    @Column(name = "Code", index = true, unique = true)
    private String code;

    public Category() {
        super();
    }

    public Category(String recordId, String name, String code) {
        super();
        this.recordId = recordId;
        this.name = name;
        this.code = code;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
