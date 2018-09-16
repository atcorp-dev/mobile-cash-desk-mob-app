package ua.com.atcorp.mobilecashdesk.Repositories;

import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.Models.Item;
import ua.com.atcorp.mobilecashdesk.Rest.Api.CompanyApi;
import ua.com.atcorp.mobilecashdesk.Rest.Api.Dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.Rest.Api.ItemApi;
import ua.com.atcorp.mobilecashdesk.ui.MainActivity;

public class ItemRepository extends BaseRepository {

    public ItemTask getItemByCode(String companyId, String code, Predicate<Item, Exception> predicate) {
        ItemApi api = createService(ItemApi.class);
        Call<ItemDto> call = api.getItemByCode(companyId, code);
        ItemTask task = new ItemTask(predicate, call);
        return task;
    }

    public ItemTask getItemByBarCode(String companyId, String barCode, Predicate<Item, Exception> predicate) {
        ItemApi api = createService(ItemApi.class);
        Call<ItemDto> call = api.getItemByBarCode(companyId, barCode);
        ItemTask task = new ItemTask(predicate, call);
        return task;
    }

    public ItemListTask getItemsByName(String companyId, String name, Predicate<List<Item>, Exception> predicate) {
        ItemApi api = createService(ItemApi.class);
        Call<List<ItemDto>> call = api.getItemsByName(companyId, name);
        ItemListTask task = new ItemListTask(predicate, call);
        return task;
    }

    public class ItemTask extends AsyncTask<Void,Void,Item> {

        private Predicate<Item, Exception> predicate;
        private Call<ItemDto> call;
        private Exception error;

        public ItemTask(
                Predicate<Item, Exception> predicate,
                Call<ItemDto> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected Item doInBackground(Void... params) {
            try {
                Response response = call.execute();
                Log.d("ITEM SERVICE RESPONSE", response.body().toString());
                ItemDto itemDto = (ItemDto)response.body();
                Item item = dtoToItem(itemDto);
                return item;
            } catch (Exception e) {
                error = e;
                Log.d("GET COMPANY ITEMS ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Item result) {
            super.onPostExecute(result);
            predicate.response(result, error);
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

    public class ItemListTask extends AsyncTask<Void,Void,List<Item>> {

        private Predicate<List<Item>, Exception> predicate;
        private Call<List<ItemDto>> call;
        private Exception error;

        public ItemListTask(
                Predicate<List<Item>, Exception> predicate,
                Call<List<ItemDto>> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected List<Item> doInBackground(Void... params) {
            try {
                Response response = call.execute();
                Log.d("ITEM SERVICE RESPONSE", response.body().toString());
                List<ItemDto> itemDtoList = (List<ItemDto>)response.body();
                List<Item> item = itemDtoList
                        .stream()
                        .map(itemDto -> dtoToItem(itemDto))
                        .collect(Collectors.toList());
                return item;
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
