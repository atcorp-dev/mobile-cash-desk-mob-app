package ua.com.atcorp.mobilecashdesk;


import android.app.Application;
import android.util.Log;

import com.reactiveandroid.ReActiveAndroid;
import com.reactiveandroid.ReActiveConfig;
import com.reactiveandroid.internal.database.DatabaseConfig;
import com.reactiveandroid.internal.serializer.UUIDSerializer;

import ua.com.atcorp.mobilecashdesk.db.AppDatabase;
import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.Category;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.models.PairedDevice;
import ua.com.atcorp.mobilecashdesk.models.User;
import ua.pbank.dio.minipos.MiniPosManager;

public class MobileCashDeskApp extends Application {

    public static MobileCashDeskApp mInstance;

    public static MobileCashDeskApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initDataBase();
        Log.d("MobileCashDeskApp","Initilizing MMiniPOS");
        initMiniPosManager();

        mInstance = this;
    }

    private void initDataBase() {
        DatabaseConfig appDatabaseConfig = new DatabaseConfig.Builder(AppDatabase.class)
                .addTypeSerializers(
                        UUIDSerializer.class
                )
                .addModelClasses(
                        CartItem.class,
                        Category.class,
                        Company.class,
                        Item.class,
                        PairedDevice.class,
                        User.class,
                        Cart.class
                )
                .addMigrations(
                        AppDatabase.MIGRATION_1_2,
                        AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4
                )
                .build();

        ReActiveAndroid.init(new ReActiveConfig.Builder(this)
                .addDatabaseConfigs(appDatabaseConfig)
                .build());
    }

    private void initMiniPosManager() {
        String token = "a213b10b4706ab98ffee857e0c8182dd"; //указать токен (выдается дополнительно)
        MiniPosManager.builder(this,token)
                .setEnableLogging(true)
                .build();
    }
}
