package ua.com.atcorp.mobilecashdesk.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.rest.api.TransactionApi;
import ua.com.atcorp.mobilecashdesk.rest.dto.ItemDto;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;
import ua.com.atcorp.mobilecashdesk.services.AuthService;

public class TransactionRepository extends BaseRepository {

    AuthService mAuthService;

    public TransactionRepository(Context context) {
        super(context);
        mAuthService = new AuthService(context);
    }

    public AsyncTask getAll(Predicate<List<TransactionDto>, Exception> predicate) {
        TransactionApi api = createService(TransactionApi.class, getContext());
        Call<List<TransactionDto>> call = api.getAll();
        TransactionListTask task = new TransactionListTask(predicate, call);
        return task.execute();
    }

    public AsyncTask getPayed(String companyId, Date date, Predicate<List<TransactionDto>, Exception> predicate) {
        TransactionApi api = createService(TransactionApi.class, getContext());
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'00:00'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String dateFrom = df.format(date);
        Call<List<TransactionDto>> call = api.getPayed(
                companyId,
                dateFrom,
                null,
                "dateTime",
                "DESC",
                true
        );
        TransactionListTask task = new TransactionListTask(predicate, call);
        return task.execute();
    }

    public AsyncTask getById(String id, Predicate<TransactionDto, Exception> predicate) {
        TransactionApi api = createService(TransactionApi.class, getContext());
        Call<TransactionDto> call = api.getById(id);
        TransactionTask task = new TransactionTask(predicate, call);
        return task.execute();
    }

    public AsyncTask create(TransactionDto transaction, Predicate<TransactionDto, Exception> predicate) {
        TransactionApi api = createService(TransactionApi.class, getContext());
        String companyId = mAuthService.getCurrentCompany().recordId;
        Call<TransactionDto> call = api.create(companyId, transaction);
        TransactionTask task = new TransactionTask(predicate, call);
        return task.execute();
    }

    public AsyncTask markAsPayed(String id, String receipt, Predicate<TransactionDto, Exception> predicate) {
        TransactionApi api = createService(TransactionApi.class, getContext());
        HashMap<String, String> payload = new HashMap<>();
        payload.put("receipt", receipt);
        Call<TransactionDto> call = api.markAsPayed(id, payload);
        TransactionTask task = new TransactionTask(predicate, call);
        return task.execute();
    }

    public AsyncTask markAsRejected(String id, Predicate<TransactionDto, Exception> predicate) {
        TransactionApi api = createService(TransactionApi.class, getContext());
        Call<TransactionDto> call = api.markAsRejected(id);
        TransactionTask task = new TransactionTask(predicate, call);
        return task.execute();
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
                Log.d("TRANSACTION ERROR", e.getMessage());
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
                Log.d("TRANSACTIONS ERROR", e.getMessage());
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
