package ua.com.atcorp.mobilecashdesk.rest.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import ua.com.atcorp.mobilecashdesk.rest.dto.CompanyDto;

public interface CompanyApi {
    @GET("companies")
    Call<List<CompanyDto>> getCompanies( );

    /*
    @GET("companies/{companyId}/items")
    Call<List<ItemDto>> getCompanyItems(@Path("companyId") String companyId);
    */
}
