package ua.com.atcorp.mobilecashdesk.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.reactiveandroid.query.Select;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.User;
import ua.com.atcorp.mobilecashdesk.repositories.BaseRepository;
import ua.com.atcorp.mobilecashdesk.rest.api.AuthApi;
import ua.com.atcorp.mobilecashdesk.rest.dto.UserDto;

public class AuthService extends BaseRepository {

    private User mCurrentUser;
    private Company mCurrentCompany;
    private final static String mPreferencesFileName = "__auth__";

    public AuthService(Context context) {
        super(context);
    }

    public AsyncTask login(String username, String password, Predicate<User, Exception> predicate) {
        mUsername = username;
        mPassword = password;
        AuthApi api = createService(AuthApi.class, getContext());
        Map<String,String> params = new HashMap<>();
        params.put("username", mUsername);
        params.put("password", mPassword);
        Call<UserDto> call = api.login(params);
        LoginTask task = new LoginTask(predicate, call);
        return task.execute();
    }

    public AsyncTask ping(Predicate<User, Exception> predicate) {
        AuthApi api = createService(AuthApi.class, getContext());
        Call<UserDto> call = api.ping();
        LoginTask task = new LoginTask(predicate, call);
        return task.execute();
    }

    public AsyncTask logout(Predicate<Void, Exception> predicate) {
        mUsername = null;
        mPassword = null;
        AuthApi api = createService(AuthApi.class, getContext());
        Call<Void> call = api.logout();
        LogoutTask task = new LogoutTask(predicate, call);
        return task.execute();
    }

    public Company getCurrentCompany() {
        if (mCurrentCompany == null) {
            String companyId = getPrefCompanyId(mContext);
            Company company = Select
                    .from(Company.class)
                    .where("RecordId = ?", companyId)
                    .fetchSingle();
            List<Company> cc = Select.from(Company.class).fetch();
            for(Company c : cc)
                Log.d("Companies", c.getRecordId());
            mCurrentCompany = company;
        }
        return mCurrentCompany;
    }

    public void setCurrentCompany(Company company) {
        mCurrentCompany = company;
        if (company == null)
            setPrefCompanyId(null, mContext);
        else
            setPrefCompanyId(company.getRecordId(), mContext);
    }

    public User getCurrentUser() {
        if (mCurrentUser == null) {
            String userLogin = getPrefLogin(mContext);
            User user = Select.from(User.class)
                    .where("Login = ?", userLogin)
                    .fetchSingle();
            mCurrentUser = user;
        }
        return mCurrentUser;
    }

    public void setCurrentUser(User currentUser) {
        currentUser = mCurrentUser;
        if (currentUser == null) {
            setPrefLogin(null, mContext);
            setPrefPassword(null, mContext);
            setCurrentCompany(null);
        }
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        SharedPreferences sharedPref = context
                .getSharedPreferences(mPreferencesFileName, Context.MODE_PRIVATE);
        return  sharedPref;
    }

    public static String getPrefLogin(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getString("login", null);
    }

    public static void setPrefLogin(String login, Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("login" , login);
        editor.commit();
    }

    public static String getPrefPassword(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getString("password", null);
    }

    public static void setPrefPassword(String password, Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("password" , password);
        editor.commit();
    }

    public static String getPrefCompanyId(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getString("companyId", null);
    }

    public static void setPrefCompanyId(String password, Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("companyId" , password);
        editor.commit();
    }

    //TODO: meve to UserService
    private User dtoToUser(UserDto dto) {
        if (dto == null)
            return null;

        User user = Select.from(User.class).where("RecordId = ?", dto.id).fetchSingle();

        if(user == null) {
            user = new User();
            user.setRecordId(dto.id);
            user.setCompanyId(dto.companyId);
        }

        user.setEmail(dto.email);
        user.setLogin(dto.login);

        user.save();

        return user;

    }

    public class LoginTask extends AsyncTask<Void,Void,User> {

        private Predicate<User, Exception> predicate;
        private Call<UserDto> call;
        private Exception error;

        public LoginTask(
                Predicate<User, Exception> predicate,
                Call<UserDto> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected User doInBackground(Void... params) {
            try {

                Response response = call.execute();
                UserDto userDto = (UserDto)response.body();
                mCurrentUser = dtoToUser(userDto);
                setPrefLogin(mUsername, mContext);
                setPrefPassword(mPassword, mContext);
                return mCurrentUser;
            } catch (Exception e) {
                error = e;
                Log.d("LOGIN ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(User result) {
            super.onPostExecute(result);
            try {
                predicate.response(result, error);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            try {
                predicate.response(result, error);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
