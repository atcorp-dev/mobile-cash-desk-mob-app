package ua.com.atcorp.mobilecashdesk.rest.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;

public interface ItemApi {

    @GET("items/{id}")
    Call<ItemDto> getItemById(@Path("id") String id);

    @GET("items/byCode/{companyId}/{code}")
    Call<ItemDto> getItemByCode(
            @Path("companyId") String companyId,
            @Path("code") String code);

    @GET("items/byBarCode/{companyId}/{barCode}")
    Call<ItemDto> getItemByBarCode(
                    @Path("companyId") String companyId,
                    @Path("barCode") String barCode);

    @GET("items/byName/{companyId}/{name}")
    Call<List<ItemDto>> getItemsByName(
            @Path("companyId") String companyId,
            @Path("name") String name);
}
