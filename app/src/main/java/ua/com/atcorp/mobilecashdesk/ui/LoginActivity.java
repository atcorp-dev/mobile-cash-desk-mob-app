package ua.com.atcorp.mobilecashdesk.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.models.User;
import ua.com.atcorp.mobilecashdesk.repositories.CompanyRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.UserDto;
import ua.com.atcorp.mobilecashdesk.services.AuthService;

public class LoginActivity extends AppCompatActivity {
 
    private boolean loginInProgress = false;
    private AuthService mAuthService;
    // UI references.
    private EditText mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mEntranceFormView;
    private Spinner mCompanyView;
    private Button mEntranceButton;

    public static final String PreferencesFileName = "__login__";

    private SharedPreferences getSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(PreferencesFileName, Context.MODE_PRIVATE);
        return  sharedPref;
    }

    private String getPrefLogin() {
        SharedPreferences sharedPref = getSharedPreferences();
        return sharedPref.getString("login", null);
    }

    private void setPrefLogin(String login) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("login" , login);
        editor.commit();
    }

    private String getPrefPassword() {
        SharedPreferences sharedPref = getSharedPreferences();
        return sharedPref.getString("password", null);
    }

    private void setPrefPassword(String password) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("password" , password);
        editor.commit();
    }

    private String getPrefCompanyId() {
        SharedPreferences sharedPref = getSharedPreferences();
        return sharedPref.getString("companyId", null);
    }

    private void setPrefCompanyId(String password) {
        SharedPreferences sharedPref = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("companyId" , password);
        editor.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuthService = new AuthService(this);
        // Set up the login form.
        mLoginView = findViewById(R.id.login);
        String login = getPrefLogin();
        mLoginView.setText(login);
        mEntranceFormView = findViewById(R.id.entrance_form);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });
        String password = getPrefPassword();
        mPasswordView.setText(password);

        findViewById(R.id.sign_in_button).setOnClickListener(v -> attemptLogin());

        mCompanyView = findViewById(R.id.spinner_company);
        mEntranceButton = findViewById(R.id.entrance_button);
        mEntranceButton.setOnClickListener(v -> onEntranceButtonClick(v));
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }

    private void loadCompanies(User user) {
        CompanyRepository repository = new CompanyRepository(this);
        repository.getCompanies((companies, err) -> {

            if (err != null || companies == null) {
                Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            List<String> list = new ArrayList<>();
            for (Company c : companies) {
                String name = c.getName();
                list.add(name);
            }
            ArrayAdapter adapter = new ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    list.toArray()
            );
            mCompanyView.setAdapter(adapter);
            mCompanyView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Company company = companies.get(i);
                    mAuthService.setCurrentCompany(company);
                    mEntranceButton.setEnabled(true);
                    setPrefCompanyId(company.getRecordId());
                    mAuthService.setCurrentCompany(company);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    mEntranceButton.setEnabled(false);
                }
            });
            String companyId = getPrefCompanyId();
            if (companyId == null) {
                Company currentCompany = mAuthService.getCurrentCompany();
                if (currentCompany != null)
                    companyId = currentCompany.getRecordId();
                else if (user != null)
                    companyId = user.getCompanyId();
            }
            if (companyId != null) {
                for (Company company : companies) {
                    if (companyId.equals(company.getRecordId())) {

                        mAuthService.setCurrentCompany(company);
                        if (companies.size() == 1) {
                            onEntranceButtonClick(mEntranceButton);
                            mCompanyView.setVisibility(View.GONE);
                            mEntranceButton.setVisibility(View.GONE);
                        } else {
                            mCompanyView.setSelection(companies.indexOf(company));
                            mEntranceButton.setEnabled(true);
                        }
                        break;
                    }
                }
            }
        });
    }
    
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (loginInProgress) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String login = mLoginView.getText().toString();
        setPrefLogin(login);
        String password = mPasswordView.getText().toString();
        setPrefPassword(password);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_invalid_email));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mLoginFormView.setVisibility(View.GONE);
            mAuthService.login(login, password, (user, err) -> onLoginExecute(user, err));
        }
    }

    private boolean isLoginValid(String login) {
        return login.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void onLoginExecute(User user, Exception err) {
        loginInProgress = false;
        showProgress(false);
        if (err != null) {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
            mLoginFormView.setVisibility(View.VISIBLE);
            return;
        }

        if (user != null) {
            loadCompanies(user);
            mEntranceFormView.setVisibility(View.VISIBLE);
            mLoginFormView.setVisibility(View.GONE);
        } else {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
            mLoginFormView.setVisibility(View.VISIBLE);
        }
    }

    private void onEntranceButtonClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        // onBackPressed();
        finish();
    }

}

