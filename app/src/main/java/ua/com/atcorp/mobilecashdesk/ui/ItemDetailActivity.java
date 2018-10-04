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

import java.text.DecimalFormat;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.adapters.ItemDetailTabAdapter;
import ua.com.atcorp.mobilecashdesk.models.Item;


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
        mItem = (Item) intent.getSerializableExtra("item");
        ((TextView)findViewById(R.id.cart_item_name)).setText(mItem.getName());
        ((TextView)findViewById(R.id.cart_item_code)).setText(mItem.getCode());
        ((TextView)findViewById(R.id.cart_item_price)).setText(getPriceFormat(mItem.getPrice()));
        setTitle("Характеристики");

        mViewPager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tabLayout);
        mAdapter = new ItemDetailTabAdapter(getSupportFragmentManager());
        ItemFeatureListFragment itemFeatureListFragment = ItemFeatureListFragment
                .newInstance(1, mItem.getRecordId());
        mAdapter.addFragment(itemFeatureListFragment, "Характеристики");
        mAdapter.addFragment(new ItemAvailabilityListFragment(), "Наявність");
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private String getPriceFormat(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        return strPrice;
    }

}
