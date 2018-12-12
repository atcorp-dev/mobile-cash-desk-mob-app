package ua.com.atcorp.mobilecashdesk.rest.api;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;

public interface TransactionApi {

    @GET("transactions")
    Call<List<TransactionDto>> getAll();

    @GET("transactions/{id}")
    Call<TransactionDto> getById(@Path("id") String id);

    @POST("transactions/{companyId}")
    Call<TransactionDto> create(
            @Path("companyId") String companyId, @Body() TransactionDto transaction);

    @PATCH("transactions/{id}/markAsPayed")
    Call<TransactionDto> markAsPayed(@Path("id") String id, @Body() HashMap<String, String> payload);

    @PATCH("transactions/{id}/markAsRejected")
    Call<TransactionDto> markAsRejected(@Path("id") String id);
}
