package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.Company;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.R;

public class MainActivity extends AppCompatActivity
        implements MainFragment.MainFragmentEventListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static Company mSelectedCompany;
    private int mSelectedMenuId;
    private List<Item> mCompanyItems;
    private NavigationView mNavigationView;
    CartFragment mCartFragment;
    private int PAYMENT_REQ_CODE = 100;

    public static Company getCompany() {
        return mSelectedCompany;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        mSelectedCompany = (Company) intent.getSerializableExtra("Company");
        openMainFragment();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_reset) {
            ItemListFragment.ResetCache();
        }

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
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
    }

    private void openCatalogueFragment() {
        mSelectedMenuId = R.id.nav_cart;
        ItemListFragment catalogueFragment = new ItemListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, catalogueFragment)
                .commit();
        mNavigationView.setCheckedItem(R.id.nav_catalogue);
        setTitle("Каталог");
    }

    public CartFragment getCartFragment() {
        return mCartFragment;
    }

    public void makePayment(CartItem item) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("cartId", item.getCartId());
        intent.putExtra("amount", item.getPrice());
        startActivityForResult(intent, PAYMENT_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        if (requestCode == PAYMENT_REQ_CODE) {
            // onPaymentActivity(resultCode, data);
        }
    }
}
