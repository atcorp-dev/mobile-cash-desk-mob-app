package ua.com.atcorp.mobilecashdesk;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.app.Application;

public class MobileCashDeskApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }
}
