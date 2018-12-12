package ua.com.atcorp.mobilecashdesk.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.reactiveandroid.query.Select;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.User;
import ua.com.atcorp.mobilecashdesk.repositories.BaseRepository;
import ua.com.atcorp.mobilecashdesk.rest.api.UserApi;
import ua.com.atcorp.mobilecashdesk.rest.dto.UserDto;

public class UserService extends BaseRepository {

    User mCurrentUserInfo;
    AuthService mAuthService;

    public UserService(Context context) {
        super(context);
        mAuthService = new AuthService(getContext());
    }

    public User getCurrentUserInfo() {
        if (mCurrentUserInfo == null) {
            mCurrentUserInfo = mAuthService.getCurrentUser();
            Company company = mAuthService.getCurrentCompany();
            mCurrentUserInfo.setCompany(company);
        }
        return mCurrentUserInfo;
    }

    public void changePasword(String id, String password, String newPassword, Predicate<User, Exception> predicate) {
        UserApi api = createService(UserApi.class, getContext());
        Map<String,String> params = new HashMap<>();
        params.put("password", password);
        params.put("newPassword", newPassword);
        Call<UserDto> call = api.changePassword(id, params);
        UserTask task = new UserTask(predicate, call);
        task.execute(password, newPassword);
    }

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
        user.setName(dto.name);

        user.save();

        return user;

    }

    public class UserTask extends AsyncTask<String,Void,User> {

        private Predicate<User, Exception> predicate;
        private Call<UserDto> call;
        private Exception error;

        public UserTask(
                Predicate<User, Exception> predicate,
                Call<UserDto> call
        ) {
            this.predicate = predicate;
            this.call = call;
        }

        @Override
        protected User doInBackground(String... params) {
            try {
                Response response = call.execute();
                UserDto userDto = (UserDto)response.body();
                User user = dtoToUser(userDto);
                String newPassword = params[1];
                AuthService.setPrefPassword(newPassword, getContext());
                return user;
            } catch (Exception e) {
                error = e;
                Log.d("LOGIN ERROR", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(User result) {
            super.onPostExecute(result);
            predicate.response(result, error);
        }
    }
}
