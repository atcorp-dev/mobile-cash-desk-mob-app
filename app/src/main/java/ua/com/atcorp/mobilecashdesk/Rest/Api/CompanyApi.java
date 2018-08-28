package ua.com.atcorp.mobilecashdesk.Rest.Api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import ua.com.atcorp.mobilecashdesk.Models.Company;

public interface CompanyApi {
    @GET("companies")
    Call<List<Company>> getCompanies( );
}
