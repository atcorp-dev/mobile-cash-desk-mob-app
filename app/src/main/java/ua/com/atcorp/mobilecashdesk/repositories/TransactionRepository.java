package ua.com.atcorp.mobilecashdesk.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;

public class TransactionRepository extends BaseRepository {
    public TransactionRepository(Context context) {
        super(context);
    }

    public class TransactionTask extends AsyncTask<Void,Void,TransactionDto> {

        private Predicate<TransactionDto, Exception> predicate;
        private Call<TransactionDto> call;
        private Exception error;

        public TransactionTask(
                Predicate<TransactionDto, Exception> predicate,
                Call<TransactionDto> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected TransactionDto doInBackground(Void... params) {
            try {
                Response response = call.execute();
                TransactionDto transactionDto = (TransactionDto)response.body();
                return transactionDto;
            } catch (Exception e) {
                error = e;
                Log.d("GET COMPANY ITEMS ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(TransactionDto result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }
    }

    public class TransactionListTask extends AsyncTask<Void,Void,List<TransactionDto>> {

        private Predicate<List<TransactionDto>, Exception> predicate;
        private Call<List<TransactionDto>> call;
        private Exception error;

        public TransactionListTask(
                Predicate<List<TransactionDto>, Exception> predicate,
                Call<List<TransactionDto>> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected List<TransactionDto> doInBackground(Void... params) {
            try {
                Response response = call.execute();
                List<TransactionDto> transactionDtoList = (List<TransactionDto>)response.body();
                return transactionDtoList;
            } catch (Exception e) {
                error = e;
                Log.d("GET COMPANY ITEMS ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<TransactionDto> result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }
    }
}
