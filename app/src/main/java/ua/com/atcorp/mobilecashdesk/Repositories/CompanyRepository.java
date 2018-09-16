package ua.com.atcorp.mobilecashdesk.Repositories;

import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.ui.MainActivity;
import ua.com.atcorp.mobilecashdesk.Models.Company;
import ua.com.atcorp.mobilecashdesk.Models.Item;
import ua.com.atcorp.mobilecashdesk.Rest.Api.CompanyApi;
import ua.com.atcorp.mobilecashdesk.Rest.Api.Dto.CompanyDto;
import ua.com.atcorp.mobilecashdesk.Rest.Api.Dto.ItemDto;

public class CompanyRepository extends BaseRepository {

    public CompaniesTask getCompanies(Predicate<List<Company>, Exception> predicate) {
        CompanyApi api = createService(CompanyApi.class);
        Call<List<CompanyDto>> call = api.getCompanies();
        CompaniesTask task = new CompaniesTask(predicate, call);
        return task;
    }

    /*
    public CompanyItemsTask getCompanyItems(String companyId, Predicate<List<Item>, Exception> predicate) {
        CompanyApi api = createService(CompanyApi.class);
        Call<List<ItemDto>> call = api.getCompanyItems(companyId);
        CompanyItemsTask task = new CompanyItemsTask(predicate, call);
        return task;
    }
    */

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
                List<Company> cachedItems = getCachedCompanies();
                if (cachedItems != null && cachedItems.size() > 0)
                    return cachedItems;

                Response response = call.execute();
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
            return new Select()
                    .from(Company.class)
                    .execute();
        }

        private List<Company> saveToCache(List<CompanyDto> companyDtoList) {
            if (companyDtoList == null || companyDtoList.size() == 0)
                return null;
            List<Company> companies = companyDtoList.stream()
                    .map(c -> dtoToCompany(c))
                    .collect(Collectors.toList());
            ActiveAndroid.beginTransaction();
            try {
                for(Company company : companies)
                    company.save();
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
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

    public class CompanyItemsTask extends AsyncTask<Void,Void,List<Item>> {

        private Predicate<List<Item>, Exception> predicate;
        private Call<List<ItemDto>> call;
        private Exception error;

        public CompanyItemsTask(
                Predicate<List<Item>, Exception> predicate,
                Call<List<ItemDto>> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected List<Item> doInBackground(Void... params) {
            try {
                String companyId = MainActivity.getCompany().getRecordId();
                List<Item> cachedItems = getCachedItems(companyId);
                if (cachedItems != null && cachedItems.size() > 0)
                    return cachedItems;
                Response response = call.execute();
                Log.d("RESPONSE", response.headers().toString());
                List<ItemDto> items = (List<ItemDto>)response.body();
                List<Item> itemList = saveToCache(items);
                return itemList;
            } catch (Exception e) {
                error = e;
                Log.d("GET COMPANY ITEMS ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Item> result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }

        private List<Item> getCachedItems(String companyId) {
            return new Select()
                    .from(Item.class)
                    .where("company=?", companyId)
                    .execute();
        }

        private List<Item> saveToCache(List<ItemDto> itemDtoList) {
            if (itemDtoList == null || itemDtoList.size() == 0)
                return null;
            List<Item> items = itemDtoList.stream()
                    .map(i -> dtoToItem(i))
                    .collect(Collectors.toList());
            /*ActiveAndroid.beginTransaction();
            try {
                for(Item item : items)
                    item.save();
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }*/
            return items;
        }

        private Item dtoToItem(ItemDto dto) {
            return new Item(
                    dto.id,
                    dto.code,
                    dto.barCode,
                    dto.name,
                    dto.description,
                    dto.price,
                    null, //dto.categoryId,
                    null, //dto.companyId,
                    dto.image
            );
        }
    }
}
