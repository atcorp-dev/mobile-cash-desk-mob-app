package ua.com.atcorp.mobilecashdesk.rest.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ua.com.atcorp.mobilecashdesk.rest.dto.UserDto;

public interface AuthApi {

    @POST("auth/login")
    Call<UserDto> login(@Body Map<String,String> params);

    @POST("auth/ping")
    Call<UserDto> ping();

    @POST("auth/logout")
    Call<Void> logout();
}
