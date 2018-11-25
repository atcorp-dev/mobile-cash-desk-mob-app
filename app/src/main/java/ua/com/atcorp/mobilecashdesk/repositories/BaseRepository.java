package ua.com.atcorp.mobilecashdesk.repositories;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.com.atcorp.mobilecashdesk.rest.converters.NullOnEmptyConverterFactory;
import ua.com.atcorp.mobilecashdesk.services.AuthService;

import android.util.Base64;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public abstract class BaseRepository<T> {
    public interface Predicate<T, E> {
        void response(T response, E error);
    }

    // final static String API_URL = "https://mobile-cash-desk.herokuapp.com/api/";
    final static String API_URL = "https://mobile-cash-desk-test.herokuapp.com/api/";
    // final static String API_URL = "http://10.0.2.2:3000/api/";

    protected static String mUsername, mPassword;
    protected static HashSet<String> mCookies;

    protected static final Interceptor getAuthTokenInterceptor() {
        return chain -> {
            String user = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", mUsername, mPassword);
            String str = Base64.encodeToString(user.getBytes(), Base64.NO_WRAP);;
            String token = "Bearer " + str;
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", token)
                    .build();
            return chain.proceed(request);
        };
    }

    public static final Interceptor getReceivedCookiesInterceptor() {
        return chain -> {
            Response originalResponse = chain.proceed(chain.request());
            if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                HashSet<String> cookies = new HashSet<>();
                for (String header : originalResponse.headers("Set-Cookie")) {
                    cookies.add(header);
                }
                mCookies = cookies;
            }
            return originalResponse;
        };
    }

    public static final Interceptor getAddCookiesInterceptor() {
         return chain -> {
            Request.Builder builder = chain.request().newBuilder();
            HashSet<String> preferences = mCookies;
            if (preferences != null) {
                for (String cookie : preferences) {
                    builder.addHeader("Cookie", cookie);
                }
            }
            return chain.proceed(builder.build());
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
                .readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(getAuthTokenInterceptor())
                .addInterceptor(getAddCookiesInterceptor())
                .addInterceptor(getReceivedCookiesInterceptor())
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

    protected Context mContext;

    public BaseRepository(Context context) {
        mContext = context;
        if (mUsername == null)
            initCredentials();
    }


    private void initCredentials() {
        mUsername = AuthService.getPrefLogin(mContext);
        mPassword = AuthService.getPrefPassword(mContext);
    }

    protected Context getContext() {
        return mContext;
    }
}
