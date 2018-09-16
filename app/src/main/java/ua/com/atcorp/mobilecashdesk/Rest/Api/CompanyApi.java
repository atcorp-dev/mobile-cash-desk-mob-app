package ua.com.atcorp.mobilecashdesk.Rest.Api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import ua.com.atcorp.mobilecashdesk.Rest.Api.Dto.*;

public interface CompanyApi {
    @GET("companies")
    Call<List<CompanyDto>> getCompanies( );

    /*
    @GET("companies/{companyId}/items")
    Call<List<ItemDto>> getCompanyItems(@Path("companyId") String companyId);
    */
}
