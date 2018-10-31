package ua.com.atcorp.mobilecashdesk.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.reactiveandroid.ReActiveAndroid;
import com.reactiveandroid.query.Delete;
import com.reactiveandroid.query.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.db.AppDatabase;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.rest.api.CompanyApi;
import ua.com.atcorp.mobilecashdesk.rest.dto.CompanyDto;

public class CompanyRepository extends BaseRepository {

    public CompanyRepository(Context context) {
        super(context);
    }

    public AsyncTask getCompanies(Predicate<List<Company>, Exception> predicate) {
        CompanyApi api = createService(CompanyApi.class);
        Call<List<CompanyDto>> call = api.getCompanies();
        CompaniesTask task = new CompaniesTask(predicate, call);
        return task.execute();
    }

    public class CompaniesTask extends AsyncTask<Void,Void,List<Company>> {

        private Predicate<List<Company>, Exception> predicate;
        private Call<List<CompanyDto>> call;
        private Exception error;

        public CompaniesTask(
                Predicate<List<Company>, Exception> predicate,
                Call<List<CompanyDto>> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected List<Company> doInBackground(Void... params) {
            try {
                /*List<Company> cachedItems = getCachedCompanies();
                if (cachedItems != null && cachedItems.size() > 0)
                    return cachedItems;*/

                Response response = call.execute();
                if (response.code() == 401)
                    throw new Exception(response.message());
                List<CompanyDto> companies = (List<CompanyDto>)response.body();

                List<Company> companyList = saveToCache(companies);

                return companyList;
            } catch (Exception e) {
                error = e;
                Log.d("GET COMPANIES ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Company> result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }

        private List<Company> getCachedCompanies() {
            return Select
                    .from(Company.class)
                    .fetch();
        }

        private List<Company> saveToCache(List<CompanyDto> companyDtoList) {
            if (companyDtoList == null || companyDtoList.size() == 0)
                return null;
            List<Company> companies = new ArrayList<>();
            for (CompanyDto c : companyDtoList) {
                Company dtoToCompany = dtoToCompany(c);
                companies.add(dtoToCompany);
            }
            // ReActiveAndroid.getDatabase(AppDatabase.class).beginTransaction();
            Delete.from(Company.class).execute();
            for(Company company : companies)
                company.save();
            // ReActiveAndroid.getDatabase(AppDatabase.class).endTransaction();
            return companies;
        }

        private Company dtoToCompany(CompanyDto dto) {
            return new Company(
                    dto.id,
                    dto.code,
                    dto.name,
                    dto.phone,
                    dto.email,
                    dto.address
            );
        }
    }
}
