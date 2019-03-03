package ua.com.atcorp.mobilecashdesk.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.iid.FirebaseInstanceId;

import java.net.URL;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.services.AuthService;
import ua.com.atcorp.mobilecashdesk.services.CartService;

public class MainActivity extends AppCompatActivity
        implements MainFragment.MainFragmentEventListener,
        TransactionListFragment.OnListFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private int mSelectedMenuId;
    private NavigationView mNavigationView;
    CartFragment mCartFragment;
    private int PAYMENT_REQ_CODE = 100;
    private int SCAN_REQ_CODE = 101;
    private String mAppDownloadUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        openMainFragment();

        pingSession();

        refreshFmcToken();
    }

    private void refreshFmcToken() {
        SharedPreferences sp = getSharedPreferences("fcm", MODE_PRIVATE);
        String token = sp.getString("token", null);
        if (token == null)
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
                String newToken = instanceIdResult.getToken();
                sp.edit().putString("token", newToken).commit();
            });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mSelectedMenuId > 0 && mSelectedMenuId != R.id.nav_main) {
            openMainFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean actionPrintZReportVisible = mSelectedMenuId == R.id.nav_transactions;
        menu.findItem(R.id.action_print_z_report).setVisible(actionPrintZReportVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            openMainFragment();
        } else if (id == R.id.nav_cart) {
            openCartFragment();
        } else if (id == R.id.nav_catalogue) {
            openCatalogueFragment();
        } else if (id == R.id.nav_transactions) {
            openTransactionsFragment();
        } else if (id == R.id.nav_cart_history) {
            openCartHistoryFragment();
        } else if (id == R.id.nav_upgrade) {
            upgradeApp();
        } else if (id == R.id.nav_profile) {
            openProfileActivity();
        } else if (id == R.id.nav_logout) {
            logOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCartMenuItemClick() {
        openCartFragment();
    }

    @Override
    public void onCatalogueMenuItemClick() {
        openCatalogueFragment();
    }

    private void openMainFragment() {
        mSelectedMenuId = R.id.nav_main;
        MainFragment mainFragment = new MainFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mainFragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_main);
        setTitle("Головна");
        invalidateOptionsMenu();
    }

    private void openCartFragment() {
        mSelectedMenuId = R.id.nav_cart;
        mCartFragment = new CartFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mCartFragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_cart);
        setTitle("Кошик");
        invalidateOptionsMenu();
    }

    private void openCatalogueFragment() {
        mSelectedMenuId = R.id.nav_catalogue;
        ItemListFragment catalogueFragment = new ItemListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, catalogueFragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_catalogue);
        setTitle("Каталог");
        invalidateOptionsMenu();
    }

    private void openTransactionsFragment() {
        mSelectedMenuId = R.id.nav_transactions;
        TransactionListFragment transactionListFragment = TransactionListFragment
                .newInstance(1);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, transactionListFragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_transactions);
        setTitle("Архів");
        invalidateOptionsMenu();
    }

    private void openCartHistoryFragment() {
        mSelectedMenuId = R.id.nav_cart_history;
        CartHistoryFragment cartHistoryFragment = CartHistoryFragment
                .newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, cartHistoryFragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_cart_history);
        setTitle("Історія");
    }


    public void openCatalogueItemActivity(Item item) {
        openCatalogueItemActivity(item.getRecordId());
    }

    public void openCatalogueItemActivity(String itemId) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("itemId", itemId);
        startActivity(intent);
    }

    public void openProfileActivity() {
        Intent intent = new Intent(this, UserProfilerActivity.class);
        startActivity(intent);
    }

    public void makePayment(String cartId, double price) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("cartId", cartId);
        intent.putExtra("amount", price);
        startActivityForResult(intent, PAYMENT_REQ_CODE);
    }

    public void scanBarCode() {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, SCAN_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        if (requestCode == PAYMENT_REQ_CODE) {
            onPaymentActivity(resultCode, data);
        } else if (requestCode == SCAN_REQ_CODE) {
            onBarCodeDetected(resultCode, data);
        }
    }

    private void onPaymentActivity(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            new CartService(this).clearCart();
        }
    }

    private void onBarCodeDetected(int resultCode, Intent data) {
        if (resultCode != CommonStatusCodes.SUCCESS)
            return;
        try {
            String barCode = data.getStringExtra("barCode");
            if (barCode != null && mCartFragment != null) {
                mCartFragment.addItemByBarCode(barCode);
            }
        } catch (Exception err) {
            String message = "ERROR IN DETECTING BAR CODE: " + err.getMessage();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Ви дійсно бажаєте вийти?")
                .setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> {
                    AuthService authService = new AuthService(this);
                    authService.logout((Void, err) -> {
                        if (err != null) {
                            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
                            String msg = err.getMessage() + "\n" + err.getStackTrace().toString();
                            Log.e("LOG_OUT_ERROR", msg);
                        }
                    });
                    authService.setCurrentUser(null);
                    //TODO: Move to CartService
                    new CartService(this).clearCart();
                    finish();
                    openLoginActivity();
                })
                .setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    private void pingSession() {
        AuthService auth = new AuthService(this);
        if (auth.getCurrentUser() == null)
            openLoginActivity();
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void upgradeApp() {
        if (TextUtils.isEmpty(mAppDownloadUrl))
            return;
        Uri uri = Uri.parse(mAppDownloadUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onPrintReceipt(String receipt) {
        Intent intent = new Intent(this, PrintReceiptActivity.class);
        intent.putExtra("receipt", receipt);
        startActivity(intent);
    }
}
