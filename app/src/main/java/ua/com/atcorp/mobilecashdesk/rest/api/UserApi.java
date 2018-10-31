package ua.com.atcorp.mobilecashdesk.rest.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ua.com.atcorp.mobilecashdesk.rest.dto.UserDto;

public interface UserApi {


    @GET("users/{id}")
    Call<UserDto> getUserById(@Path("id") String id);

    @POST("users/{id}/changePassword")
    Call<UserDto> changePassword(@Path("id") String id, @Body Map<String,String> params);
}
