package ua.com.atcorp.mobilecashdesk.Repositories;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class BaseRepository<T> {
    public interface Predicate<T, E> {
        void response(T response, E error);
    }

    // final static String API_URL = "https://mobile-cash-desk.herokuapp.com/api/";
    final static String API_URL = "http://10.0.2.2:3000/api/";

    protected static  boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected static <T> T createService(Class<T> serviceClass) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        OkHttpClient client = httpClientBuilder
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        T api = retrofit.create(serviceClass);
        return api;
    }
}
