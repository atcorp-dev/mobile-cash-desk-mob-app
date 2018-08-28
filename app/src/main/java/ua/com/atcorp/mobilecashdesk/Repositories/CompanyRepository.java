package ua.com.atcorp.mobilecashdesk.Repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.Models.Company;
import ua.com.atcorp.mobilecashdesk.Rest.Api.CompanyApi;

public class CompanyRepository extends BaseRepository {

    public CompaniesTask getCompanies(
            Predicate<List<Company>, Exception> predicate, Context ctx, boolean force
    ) {
        CompanyApi api = createService(CompanyApi.class,ctx, force);
        Call<List<Company>> call = api.getCompanies();
        CompaniesTask task = new CompaniesTask(predicate, call);
        return task;
    }

    public class CompaniesTask extends AsyncTask<Void,Void,List<Company>> {

        private Predicate<List<Company>, Exception> predicate;
        private Call<List<Company>> call;
        private Exception error;

        public CompaniesTask(
                Predicate<List<Company>, Exception> predicate,
                Call<List<Company>> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected List<Company> doInBackground(Void... params) {
            try {
                Response response = call.execute();
                Log.d("RESPONSE", response.headers().toString());
                List<Company> items = (List<Company>)response.body();
                return items;
            } catch (Exception e) {
                error = e;
                Log.d("ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Company> result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }
    }
}
