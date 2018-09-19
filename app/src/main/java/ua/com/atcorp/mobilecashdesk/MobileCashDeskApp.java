package ua.com.atcorp.mobilecashdesk;


import android.app.Application;

import com.reactiveandroid.ReActiveAndroid;
import com.reactiveandroid.ReActiveConfig;
import com.reactiveandroid.internal.database.DatabaseConfig;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.Category;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.models.PairedDevice;
import ua.pbank.dio.minipos.MiniPosManager;

public class MobileCashDeskApp extends Application {

    public static MobileCashDeskApp instance;

    @Override
    public void onCreate() {
        super.onCreate();

        initDataBase();
        initMiniPosManager();

        instance = this;
    }

    private void initDataBase() {
        DatabaseConfig appDatabaseConfig = new DatabaseConfig.Builder(AppDatabase.class)
                .addModelClasses(
                        CartItem.class,
                        Category.class,
                        Company.class,
                        Item.class,
                        PairedDevice.class
                )
                .build();

        ReActiveAndroid.init(new ReActiveConfig.Builder(this)
                .addDatabaseConfigs(appDatabaseConfig)
                .build());
    }

    private void initMiniPosManager() {
        String token = ""; //указать токен (выдается дополнительно)
        MiniPosManager.builder(this,token)
                .setEnableLogging(true)
                .build();
    }
}
