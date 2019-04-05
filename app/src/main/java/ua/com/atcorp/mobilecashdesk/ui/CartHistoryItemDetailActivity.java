package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.repositories.CartRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.CartDto;

public class CartHistoryItemDetailActivity extends AppCompatActivity {

    private WebView mWebView;
    private CartRepository mCartRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_history_item_detail);
        mWebView = findViewById(R.id.webView);
        mCartRepository = new CartRepository(this);

        Intent intent = getIntent();
        String cartHistoryId = intent.getStringExtra("cartHistoryId");
        if (cartHistoryId != null && cartHistoryId.length() > 0)
            loadHistoryItem(cartHistoryId);
    }

    private void loadHistoryItem(String id) {
        mCartRepository.getCartById(id, (cart, err) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            } else if (cart != null) {
                showCart(cart);
            }
        });
    }

    private void showCart(CartDto cart) {
        String html = "<html><body><div class=\"container\">";
        html += "<p>Створено</p>";
        html += String.format("<p>%s</p>", getDateString(cart.createdOn));
        html += String.format("<p>%s</p>", cart.createdBy.login);
        html += "<hr /><p>";

        for(CartDto historyItem : cart.history) {
            html += "<div class=\"history-item\" style=\"border: 1px grey solid; margin: 6px;\">";
            // html += String.format("<p>Станом на: %s</p>", getDateString(historyItem.createdOn));
            if (historyItem.items != null) {
                for (CartDto.CartItemDto item : historyItem.items) {
                    html += "<div style=\"background-color: lightgray; margin: 3px; padding: 3px\">";
                    if (!TextUtils.isEmpty(item.name))
                        html += String.format("<p><strong>Назва</strong>: %s</p>", item.name);
                    if (!TextUtils.isEmpty(item.barCode))
                        html += String.format("<p><strong>Штрих-код</strong>: %s</p>", item.barCode);
                    if (!TextUtils.isEmpty(item.code))
                        html += String.format("<p><strong>Код</strong>: %s</p>", item.code);
                    html += String.format("<p><strong>Ціна</strong>: %s</p>", item.price);
                    html += String.format("<p><strong>Кількість</strong>: %s</p>", item.qty);
                    html += "</div>";
                }
            }
            html += "</div>";
        }
        html += "</div></body></html>";
        mWebView.loadData(html, "text/html; charset=utf-8","UTF-8");
    }

    private String getDateString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = df.format(date);
        return dateString;
    }
}
