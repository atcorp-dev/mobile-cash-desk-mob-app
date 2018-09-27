package ua.com.atcorp.mobilecashdesk.repositories;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.com.atcorp.mobilecashdesk.rest.converters.NullOnEmptyConverterFactory;
import android.util.Base64;

public abstract class BaseRepository<T> {
    public interface Predicate<T, E> {
        void response(T response, E error);
    }

    // final static String API_URL = "https://mobile-cash-desk.herokuapp.com/api/";
    final static String API_URL = "http://10.0.2.2:3000/api/";

    protected static final Interceptor getRewriteCacheControlInterceptor() {
        return chain -> {
            String username = "admin";
            String password = "admin1234";
            String user = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
            String str = Base64.encodeToString(user.getBytes(), Base64.NO_WRAP);;
            String token = "Bearer " + str;
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", token)
                    .build();
            return chain.proceed(request);
        };
    }

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
                .addInterceptor(getRewriteCacheControlInterceptor())
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        T api = retrofit.create(serviceClass);
        return api;
    }
}
