package ua.com.atcorp.mobilecashdesk.ui;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.User;
import ua.com.atcorp.mobilecashdesk.services.UserService;

public class UserProfilerActivity extends AppCompatActivity {

    @BindView(R.id.tvUserLogin)
    EditText tvUserLogin;
    @BindView(R.id.tvUserEmail)
    EditText tvUserEmail;
    @BindView(R.id.tvCompany)
    EditText tvCompany;
    @BindView(R.id.spinner_payment_method)
    Spinner mPaymentMethodView;

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
        initPaymentMethodSpinner();
        String userLogin = user.getLogin();
        boolean visible = userLogin != null && userLogin.equals("admin");
        mPaymentMethodView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void initPaymentMethodSpinner() {
        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[] {"default", "Default"});
        data.add(new String[] {"private", "Private Bank"});
        data.add(new String[] {"atcorp", "AT CORP"});
        ArrayList<String> list = new ArrayList<>();
        for(String[] item : data)
            list.add(item[1]);
        ArrayAdapter adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                list.toArray()
        );
        mPaymentMethodView.setAdapter(adapter);
        mPaymentMethodView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String method = data.get(i)[0];
                setPaymentMethod(method);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        String savedMethod = getPaymentMethod();
        int index = 0;
        for(String[] item : data) {
            if (item[0].equals(savedMethod))
                break;
            index++;
        }
        mPaymentMethodView.setSelection(index, true);
    }

    private void setPaymentMethod(String method) {
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        sp
                .edit()
                .putString("payment_method", method)
                .commit();
    }

    private String getPaymentMethod() {
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        String method = sp.getString("payment_method", "private");
        return method;
    }
}
