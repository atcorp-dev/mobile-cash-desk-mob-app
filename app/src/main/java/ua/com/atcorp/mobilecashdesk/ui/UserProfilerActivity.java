package ua.com.atcorp.mobilecashdesk.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.models.User;
import ua.com.atcorp.mobilecashdesk.services.UserService;

public class UserProfilerActivity extends AppCompatActivity {

    @BindView(R.id.tvUserLogin)
    EditText tvUserLogin;
    @BindView(R.id.tvUserEmail)
    EditText tvUserEmail;
    @BindView(R.id.tvCompany)
    EditText tvCompany;

    UserService mUserService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profiler);
        ButterKnife.bind(this);
        mUserService = new UserService(this);
        User user = mUserService.getCurrentUserInfo();
        tvUserLogin.setText(user.getLogin());
        tvUserEmail.setText(user.getEmail());
        tvCompany.setText(user.getCompany().getName());
        // getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
