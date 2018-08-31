package ua.com.atcorp.mobilecashdesk.Rest.Api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import ua.com.atcorp.mobilecashdesk.Models.Company;
import ua.com.atcorp.mobilecashdesk.Models.Item;

public interface CompanyApi {
    @GET("companies")
    Call<List<Company>> getCompanies( );

    @GET("companies/:companyId/items")
    Call<List<Item>> getCompanyItems(@Path("id") String companyId);
}
