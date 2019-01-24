package ua.com.atcorp.mobilecashdesk.rest.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ua.com.atcorp.mobilecashdesk.rest.dto.CartDto;

public interface CartApi {

    @GET("carts")
    Call<List<CartDto>> getCarts();

    @GET("carts/{id}")
    Call<List<CartDto>> getCartById(@Path("id") String cartId);

    @POST("carts")
    Call<CartDto> create(@Body() CartDto cartDto);

    @PATCH("carts/{id}")
    Call<CartDto> modify(@Path("id") String cartId, @Body() CartDto cartDto);
}
