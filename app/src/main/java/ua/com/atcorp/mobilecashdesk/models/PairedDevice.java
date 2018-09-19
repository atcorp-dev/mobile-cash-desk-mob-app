package ua.com.atcorp.mobilecashdesk.models;

import com.reactiveandroid.Model;
import com.reactiveandroid.annotation.Column;
import com.reactiveandroid.annotation.PrimaryKey;
import com.reactiveandroid.annotation.Table;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;


@Table(name = "PairedDevice", database = AppDatabase.class)
public class PairedDevice extends Model {

    @PrimaryKey(name = "_id")
    private Long _id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    //тип устройства терминал pinpad - "t", printer - "p"
    @Column(name = "type")
    private String type;


    @Column(name = "serialnumber")
    private String serialnumber;


    public PairedDevice() {
        super();
    }

    public PairedDevice(String name, String address, String type) {
        super();
        this.name = name; //произвольное имя заданное пользователем
        this.address = address;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSerialNumber() {
        return serialnumber;
    }

    public void setSerialNumber(String serialnumber) {
        this.serialnumber = serialnumber;
    }
}
