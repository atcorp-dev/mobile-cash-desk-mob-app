package ua.com.atcorp.mobilecashdesk.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.rest.api.CartApi;
import ua.com.atcorp.mobilecashdesk.rest.dto.CartDto;

public class CartRepository extends BaseRepository {
    
    public CartRepository(Context context) {
        super(context);
    }

    public AsyncTask create(Cart cart) {
        CartApi api = createService(CartApi.class, getContext());
        CartDto dto = cartToDto(cart);
        Call<CartDto> call = api.create(dto);
        CartRepository.CartTask task = new CartRepository.CartTask(null, call);
        return task.execute();
    }

    public AsyncTask modify(Cart cart) {
        CartApi api = createService(CartApi.class, getContext());
        CartDto dto = cartToDto(cart);
        Call<CartDto> call = api.modify(cart.getRecordId().toString(), dto);
        CartRepository.CartTask task = new CartRepository.CartTask(null, call);
        return task.execute();
    }

    public AsyncTask getCarts(Predicate<List<CartDto>, Exception> predicate) {
        CartApi api = createService(CartApi.class, getContext());
        Call<List<CartDto>> call = api.getCarts();
        CartListTask task = new CartListTask(predicate, call);
        return task.execute();
    }


    public class CartTask extends AsyncTask<Void,Void,CartDto> {

        private Predicate<CartDto, Exception> predicate;
        private Call<CartDto> call;
        private Exception error;

        public CartTask(
                Predicate<CartDto, Exception> predicate,
                Call<CartDto> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected CartDto doInBackground(Void... params) {
            try {
                Response response = call.execute();
                CartDto CartDto = (CartDto)response.body();
                return CartDto;
            } catch (Exception e) {
                error = e;
                Log.d("Cart ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(CartDto result) {
            super.onPostExecute(result);
            if (predicate != null)
                predicate.response(result, error);
        }
    }

    public class CartListTask extends AsyncTask<Void,Void, List<CartDto>> {

        private Predicate<List<CartDto>, Exception> predicate;
        private Call<List<CartDto>> call;
        private Exception error;

        public CartListTask(
                Predicate<List<CartDto>, Exception> predicate,
                Call<List<CartDto>> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected List<CartDto> doInBackground(Void... params) {
            try {
                Response response = call.execute();
                List<CartDto> CartDtoList = (List<CartDto>)response.body();
                return CartDtoList;
            } catch (Exception e) {
                error = e;
                Log.d("CARTS ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<CartDto> result) {
            super.onPostExecute(result);
            if (predicate != null)
                predicate.response(result, error);
        }
    }

    private CartDto cartToDto(Cart cart) {
        CartDto dto = new CartDto();
        if (cart.getRecordId() != null)
            dto.id = cart.getRecordId().toString();
        dto.clientInfo = cart.getClientInfo();
        dto.type = cart.getType();
        if (cart.getItems() != null && cart.getItems().size() > 0){
            dto.items = new ArrayList<>(cart.getItems().size());
            for(CartItem cartItem : cart.getItems())
                dto.items.add(cartItemToDto(cartItem));
        }
        return dto;
    }

    private CartDto.CartItemDto cartItemToDto(CartItem cartItem) {
        CartDto.CartItemDto dto = new CartDto.CartItemDto();
        dto.cartId = cartItem.getCartId();
        dto.id = cartItem.getItemRecordId();
        dto.barCode = cartItem.getItemBarCode();
        dto.code = cartItem.getItemCode();
        dto.name = cartItem.getItemName();
        if (cartItem.getItemCompany() != null) {
            dto.companyId = cartItem.getItemCompany().getRecordId();
        }
        dto.qty = cartItem.getQty();
        dto.price = cartItem.getPrice();
        dto.discount = cartItem.getDiscount();
        dto.dateTime = cartItem.getDatetime();
        dto.image = cartItem.getItemImage();

        return dto;
    }
}
