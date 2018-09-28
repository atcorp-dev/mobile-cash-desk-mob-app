package ua.com.atcorp.mobilecashdesk.rest.api;

import retrofit2.Call;
import retrofit2.http.POST;
import ua.com.atcorp.mobilecashdesk.rest.dto.UserDto;

public interface AuthApi {

    @POST("auth/login")
    Call<UserDto> login();

    @POST("auth/logout")
    Call<Void> logout();
}
