package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.ItemDetailTabAdapter;
import ua.com.atcorp.mobilecashdesk.models.Item;
import ua.com.atcorp.mobilecashdesk.repositories.ItemRepository;


public class ItemDetailActivity extends AppCompatActivity {

    private Item mItem;
    private ItemDetailTabAdapter mAdapter;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Intent intent = getIntent();
        // mItem = (Item) intent.getSerializableExtra("item");
        String itemId = intent.getStringExtra("itemId");

        new ItemRepository().getItemById(itemId, (item, err) -> {
            if (err != null)
                Toast.makeText(this, "Помилка при отримані датальгної інфомації", Toast.LENGTH_SHORT).show();
            else if (item != null)
                onItem(item);
            else
                Toast.makeText(this, "Товар на складі не знайдено", Toast.LENGTH_SHORT).show();
        }).execute();

        setTitle("Характеристики");
    }

    private void onItem(Item item) {
        mItem = item;

        mViewPager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tabLayout);
        mAdapter = new ItemDetailTabAdapter(getSupportFragmentManager());
        ItemFeatureListFragment itemFeatureListFragment = ItemFeatureListFragment
                .newInstance(1, item);

        mAdapter.addFragment(itemFeatureListFragment, "Характеристики");
        mAdapter.addFragment(new ItemAvailabilityListFragment(), "Наявність");
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        ((TextView)findViewById(R.id.cart_item_name)).setText(item.getName());
        ((TextView)findViewById(R.id.cart_item_code)).setText(item.getCode());
        ((TextView)findViewById(R.id.cart_item_price)).setText(getPriceFormat(item.getPrice()));
        ((TextView)findViewById(R.id.cart_item_available))
                .setText(String.format("На складі - %s", item.getAvailable() ? "Так" : "Ні"));
    }

    private String getPriceFormat(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        return strPrice;
    }

    public void onBackButtonClick(View v) {
        onBackPressed();
    }

}
