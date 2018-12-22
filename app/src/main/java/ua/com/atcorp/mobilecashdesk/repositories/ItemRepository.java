package ua.com.atcorp.mobilecashdesk.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.rest.api.ItemApi;

public class ItemRepository extends BaseRepository {

    public ItemRepository(Context context) {
        super(context);
    }

    public AsyncTask getItemById(String id, Predicate<Item, Exception> predicate) {
        ItemApi api = createService(ItemApi.class, getContext());
        Call<ItemDto> call = api.getItemById(id);
        ItemTask task = new ItemTask(predicate, call);
        return task.execute();
    }

    public AsyncTask getItemByCode(String companyId, String code, Predicate<Item, Exception> predicate) {
        ItemApi api = createService(ItemApi.class, getContext());
        Call<ItemDto> call = api.getItemByCode(companyId, code);
        ItemTask task = new ItemTask(predicate, call);
        return task.execute();
    }

    public AsyncTask getItemByBarCode(String companyId, String barCode, Predicate<Item, Exception> predicate) {
        ItemApi api = createService(ItemApi.class, getContext());
        Call<ItemDto> call = api.getItemByBarCode(companyId, barCode);
        ItemTask task = new ItemTask(predicate, call);
        return task.execute();
    }

    public AsyncTask getItemsByName(String companyId, String name, Predicate<List<Item>, Exception> predicate) {
        ItemApi api = createService(ItemApi.class, getContext());
        Call<List<ItemDto>> call = api.getItemsByName(companyId, name);
        ItemListTask task = new ItemListTask(predicate, call);
        return task.execute();
    }

    public AsyncTask getAvailable(String companyId, String code, Predicate<List<Item>, Exception> predicate) {
        ItemApi api = createService(ItemApi.class, getContext());
        Call<List<ItemDto>> call = api.getAvailable(companyId, code);
        ItemListTask task = new ItemListTask(predicate, call);
        return task.execute();
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
                ItemDto itemDto = (ItemDto)response.body();
                if (itemDto == null)
                    return null;
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
            try {
                predicate.response(result, error);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Item dtoToItem(ItemDto dto) {
            if (dto == null)
                return null;
            Item item =  new Item(
                    dto.id,
                    dto.code,
                    dto.barCode,
                    dto.name,
                    dto.description,
                    dto.price,
                    null, //dto.categoryId,
                    null, //dto.companyId,
                    dto.available
            );
            item.setImage(dto.image);
            item.setAdditionalFields(dto.additionalFields);
            return item;
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
                List<ItemDto> itemDtoList = (List<ItemDto>)response.body();
                if (itemDtoList == null)
                    itemDtoList = new ArrayList<>();
                List<Item> item = new ArrayList<>();
                for (ItemDto itemDto : itemDtoList) {
                    Item dtoToItem = dtoToItem(itemDto);
                    item.add(dtoToItem);
                }
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
            try {
                predicate.response(result, error);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Item dtoToItem(ItemDto dto) {
            if (dto == null)
                return null;
            Company company = null;
            if (dto.company != null)
                company = new Company(
                        dto.company.id,
                        dto.company.code,
                        dto.company.name,
                        dto.company.phone,
                        dto.company.email,
                        dto.company.address
                );
            Item item = new Item(
                    dto.id,
                    dto.code,
                    dto.barCode,
                    dto.name,
                    dto.description,
                    dto.price,
                    null, //dto.categoryId,
                    company,
                    dto.available
            );
            item.setImage(dto.image);
            item.setAdditionalFields(dto.additionalFields);
            return item;
        }
    }
}
