package ua.com.atcorp.mobilecashdesk.models;

import com.reactiveandroid.Model;
import com.reactiveandroid.annotation.Column;
import com.reactiveandroid.annotation.Index;
import com.reactiveandroid.annotation.IndexGroup;
import com.reactiveandroid.annotation.PrimaryKey;
import com.reactiveandroid.annotation.Table;
import com.reactiveandroid.annotation.Unique;
import com.reactiveandroid.annotation.UniqueGroup;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;

@Table(name = "Categories", database = AppDatabase.class,
        indexGroups = {
                @IndexGroup(groupNumber = 1, name = "index_RecordId"),
                @IndexGroup(groupNumber = 2, name = "index_Code")
})
public class Category extends Model {

    @PrimaryKey(name = "_id")
    private Long _id;

    @Unique(unique = true)
    @Index(indexGroups = 1)
    @Column(name = "RecordId")
    private String recordId;

    @Column(name = "Name")
    private String name;

    @Unique(unique = true)
    @Index(indexGroups = 2)
    @Column(name = "Code")
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
