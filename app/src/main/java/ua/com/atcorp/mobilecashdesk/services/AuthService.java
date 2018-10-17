package ua.com.atcorp.mobilecashdesk.services;

import android.os.AsyncTask;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.repositories.BaseRepository;
import ua.com.atcorp.mobilecashdesk.rest.api.AuthApi;
import ua.com.atcorp.mobilecashdesk.rest.dto.UserDto;

public class AuthService extends BaseRepository {

    public LoginTask login(String username, String password, Predicate<UserDto, Exception> predicate) {
        mUsername = username;
        mPassword = password;
        AuthApi api = createService(AuthApi.class);
        Call<UserDto> call = api.login();
        LoginTask task = new LoginTask(predicate, call);
        return task;
    }

    public LoginTask ping(Predicate<UserDto, Exception> predicate) {
        AuthApi api = createService(AuthApi.class);
        Call<UserDto> call = api.login();
        LoginTask task = new LoginTask(predicate, call);
        return task;
    }

    public LogoutTask logout(Predicate<Void, Exception> predicate) {
        mUsername = null;
        mPassword = null;
        AuthApi api = createService(AuthApi.class);
        Call<Void> call = api.logout();
        LogoutTask task = new LogoutTask(predicate, call);
        return task;
    }

    private static UserDto currentUser;

    public static UserDto getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserDto currentUser) {
        currentUser = currentUser;
    }

    public class LoginTask extends AsyncTask<Void,Void,UserDto> {

        private Predicate<UserDto, Exception> predicate;
        private Call<UserDto> call;
        private Exception error;

        public LoginTask(
                Predicate<UserDto, Exception> predicate,
                Call<UserDto> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected UserDto doInBackground(Void... params) {
            try {

                Response response = call.execute();
                UserDto user = (UserDto)response.body();
                currentUser = user;
                return user;
            } catch (Exception e) {
                error = e;
                Log.d("LOGIN ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserDto result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }
    }

    public class LogoutTask extends AsyncTask<Void,Void,Void> {

        private Predicate<Void, Exception> predicate;
        private Call<Void> call;
        private Exception error;

        public LogoutTask(
                Predicate<Void, Exception> predicate,
                Call<Void> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                call.execute();
                return null;
            } catch (Exception e) {
                error = e;
                Log.d("LOGIN ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }
    }
}
