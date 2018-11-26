package ua.com.atcorp.mobilecashdesk.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.*;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.atcorp.mobilecashdesk.adapters.PaymentCartItemAdapter;
import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.models.User;
import ua.com.atcorp.mobilecashdesk.repositories.TransactionRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;
import ua.com.atcorp.mobilecashdesk.services.AuthService;
import ua.com.atcorp.mobilecashdesk.services.CartService;
import ua.com.atcorp.mobilecashdesk.services.UserService;
import ua.com.atcorp.mobilecashdesk.ui.dialog.ChoicePrinterDialog;
import ua.pbank.dio.minipos.MiniPosManager;
import ua.pbank.dio.minipos.interfaces.MiniPosConnectionListener;
import ua.pbank.dio.minipos.interfaces.MiniPosPrinterListener;
import ua.pbank.dio.minipos.interfaces.MiniPosServiceErrorListener;
import ua.pbank.dio.minipos.interfaces.MiniPosTransactionListener;
import ua.pbank.dio.minipos.models.Transaction;

import android.util.*;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.ui.dialog.BaseDialogFragment;
import ua.com.atcorp.mobilecashdesk.ui.dialog.ChoicePinpadDialog;


public class PaymentActivity extends AppCompatActivity
{
    public static final String TAG = PaymentActivity.class.getSimpleName();
    private final int MAKE_PAYMENT_CODE = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static PaymentActivity instance;

    // region Fields: Private

    private BaseDialogFragment mDialog;
    private Double mAmount;
    private String mStrAmount;
    private String mPurpose = "Покупка в магазині";
    private String mReceipt;
    private Cart mCart;
    private Bitmap mReceiptBitmap;
    private TransactionRepository mTransactionRepository;
    private TransactionDto mTransaction;
    private CartService mCartService;
    private AuthService mAuthService;
    private UserService mUserService;
    private int mErrorCode;

    // endregion

    // region Fields: View

    @BindView(R.id.payment_status_message_wrap)
    View tvMessageWrap;
    @BindView(R.id.payment_status_message)
    TextView tvMessage;
    @BindView(R.id.btn_pay)
    Button btnPay;
    @BindView(R.id.btn_print)
    Button btnPrint;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    // @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.tvError)
    TextView tvError;
    @BindView(R.id.viewError)
    View viewError;
    @BindView(R.id.payment_preview_list)
    ListView paymentPreviewListView;
    ArrayList<CartItem> paymentPreviewList;
    ArrayAdapter paymentPreviewListAdapter;
    @BindView(R.id.printPreview)
    ImageView imgView;

    Snackbar mSnackBar;

    // endregion

    // region Methods: Override

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);
        btnCancel.setOnClickListener(v -> onCancelButtonClick(v));
        mCartService = new CartService(this);
        mCart = mCartService.getCurrentCart();
        mAuthService = new AuthService(this);

        setPaymentInfo();

        btnPay = findViewById(R.id.btn_pay);
        initWebView();
        //onReceipt();
        mTransactionRepository = new TransactionRepository(this);
        initTransaction();
        initPaymentPreviewList();
        imgView.setVisibility(View.GONE);

        mUserService = new UserService(this);

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

    @Override
    protected void onStart() {
        super.onStart();
        try {
            MiniPosManager.getInstance().pinpadSubscribe();
        } catch (Exception error) {
            Log.e("PAYMENT ACTIVITY ERR", error.getMessage());
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MiniPosManager.getInstance().pinpadUnsubscribe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        hideProgress();
        if (data == null) {return;}
        if (requestCode == MAKE_PAYMENT_CODE) {
            String transaction_response_code = data.getStringExtra("transaction_response_code");
            Intent intent = new Intent();
            // transaction_response_code = "000";
            boolean payed = transaction_response_code == "000" && resultCode == RESULT_OK;
            intent.putExtra("payed",  payed);
            if (resultCode == RESULT_OK) {
                String message = null;
                switch(transaction_response_code) {
                    case "000":
                        createTransaction(data);
                        openOkDialog("Оплата прошла успешно", (dialog, id) -> finish());
                        break;
                    case "-941":
                        message = "Ошибка валидации входящих параметров";
                        break;
                    case "-999":
                        message ="Отменено пользователем";
                        break;
                    default:
                        message = "Не известная ошибка";
                        break;
                }
                setResult(resultCode, intent);
                if (message != null)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                // finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case PERMISSION_REQUEST_COARSE_LOCATION: {

                    final Boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (grantResults.length == 0) return;

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "coarse location permission granted");
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.functionality_limited));
                        builder.setMessage(showRationale ?
                                getString(R.string.functionality_message) + "\n" + getString(R.string.grant_permission) :
                                getString(R.string.functionality_message) + "\n" + getString(R.string.go_settings));
                        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            try {
                                if (showRationale) {
                                    ActivityCompat.requestPermissions((Activity) PaymentActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                            PERMISSION_REQUEST_COARSE_LOCATION);
                                } else {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, 0);
                                }
                            } catch (Exception err) {
                                Toast.makeText(getApplicationContext(), "onRequestPermissionsResult" + err.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton(getString(R.string.btnCancelText), null);
                        builder.show();
                    }
                    return;
                }
            }
        } catch (Exception err) {
            Toast.makeText(this, "onRequestPermissionsResult.bottom" + err.getMessage(), Toast.LENGTH_LONG).show();
            findViewById(R.id.btn_pay).setEnabled(true);
        }
    }

    // endregion

    // region Methods: Public Static

    public static void notifyTransactionUpdated() {
        instance.mTransactionRepository.getById(instance.mTransaction.id, (transaction, err) -> {
            if (err != null) {
                View view = instance.findViewById(R.id.payment_layout);
                Snackbar.make(view, err.getMessage(), Snackbar.LENGTH_LONG).show();
                err.printStackTrace();
            } else {
                instance.updateTransaction(transaction);
            }
        });
    }

    // endregion

    // region Methods: Public View

    public  void onPayment(View view) {
        // view.setEnabled(false);
        try {
            if (mCart.getType() == 1) {
                sendToCashDesk();
                return;
            }
            String method = getPaymentMethod();
            switch (method) {
                case "atcorp":
                    makePayment(mStrAmount);
                    break;
                case "default":
                    markTransactionAsPayed();
                    break;
                default:
                    makePaymentPrivate(mAmount);
            }
        } catch (Exception err) {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
            String m = "";
            for(StackTraceElement line : err.getStackTrace())
                m += line.getClassName() + "." + line.getMethodName() + "." + line.getLineNumber();
            Toast.makeText(this, m, Toast.LENGTH_LONG).show();
        }
    }

    public void onPrint(View view) {
        final Bitmap bitmap = getBitmapFromWebView(webView);
        mReceiptBitmap  =  getProportionalBitmap(bitmap,384,"X"); //horizontal max dot 384
        Bitmap previewBitmap  =  getProportionalBitmap(bitmap,bitmap.getWidth(),"X");
        bitmap.recycle();
        imgView.setImageBitmap(previewBitmap);
        imgView.setVisibility(View.VISIBLE);
        paymentPreviewListView.setVisibility(View.GONE);
        if (mCart.getType() == 0)
            MiniPosManager.getInstance().initPrinter(printerConnectionListener);
    }

    // endregion

    // region Methods: Private

    private String getPaymentMethod() {
        User user = mUserService.getCurrentUserInfo();
        String login = user.getLogin();
        if (login == null || !login.equals("admin"))
            return "private";
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        String method = sp.getString("payment_method", "default");
        return method;
    }

    private boolean isNeedRecalculateCart() {
        return false;
    }

    private void setPaymentInfo() {
        double price = mCart.getTotalPrice();
        double discount = mCart.getDiscount();
        int intValue = (int)price;
        int rest = (int) ((price - intValue) * 100);
        String amountStr = String.format("%s.%s", intValue, rest);
        String priceStr = formatPrice(price);
        String discountStr = formatPrice(discount);
        mAmount = price;
        mStrAmount = amountStr;
        setEditTextValue(R.id.payment_amount, priceStr);
        setEditTextValue(R.id.payment_discount, discountStr);
    }

    private void setStatusMessage(String message) {
        tvMessageWrap.setVisibility(message == null || message == "" ? View.GONE : View.VISIBLE);
        tvMessage.setText(message);
    }

    private String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        return strPrice;
    }

    private void showPrintButton() {
        btnPrint.setVisibility(View.VISIBLE);
        btnPay.setVisibility(View.GONE);
    }

    private void updateTransaction(TransactionDto transaction) {
        ArrayList<CartItem> cartItems = mCart.getItems();
        paymentPreviewListAdapter.clear();
        for(CartItem cartItem : cartItems) {
            for(TransactionDto.TransactionItemDto transactionItem : transaction.itemList) {
                if (cartItem.getItemRecordId().equals(transactionItem.itemId)) {
                    double discount = cartItem.getItemPrice() - transactionItem.price;
                    cartItem.setItemDiscount(discount);
                    break;
                }
            }
            paymentPreviewList.add(cartItem);
            cartItem.save();
        }
        setPaymentInfo();
        btnPay.setEnabled(true);
        if (mSnackBar != null && mSnackBar.isShown()) {
            mSnackBar.dismiss();
            View view = findViewById(R.id.payment_layout);
            Snackbar.make(view, R.string.payment_transaction_updated, Snackbar.LENGTH_LONG).show();
        }
    }

    private void initPaymentPreviewList() {
        paymentPreviewList = new ArrayList<>();
        for (CartItem cartItem : mCart.getItems())
            paymentPreviewList.add(cartItem);
        paymentPreviewListAdapter = new PaymentCartItemAdapter(this, paymentPreviewList);
        paymentPreviewListView.setAdapter(paymentPreviewListAdapter);
    }

    private String getFcmToken() {
        SharedPreferences sp = getSharedPreferences("fcm", MODE_PRIVATE);
        String token = sp.getString("token", null);
        return token;
    }

    private AsyncTask getTransactionById(String transactionId) {
        return mTransactionRepository.getById(transactionId, (transaction, err) -> {
            if (err == null) {
                mTransaction = transaction;
                setPaymentInfo();
            }
        });
    }

    private String getCartModifiedOn() {
        SharedPreferences sp = getSharedPreferences("transaction", MODE_PRIVATE);
        String res = sp.getString("cartModifiedOn", null);
        return res;
    }
    
    private void saveTransactionId(String transactionId) {
        SharedPreferences sp = getSharedPreferences("transaction", MODE_PRIVATE);
        sp.edit().putString("transactionId", transactionId).commit();
    }

    private String getTransactionId() {
        SharedPreferences sp = getSharedPreferences("transaction", MODE_PRIVATE);
        String transactionId = sp.getString("transactionId", null);
        return transactionId;
    }

    private void setCartModifiedOn(String timestamp) {
        SharedPreferences sp = getSharedPreferences("transaction", MODE_PRIVATE);
        sp.edit().putString("cartModifiedOn", timestamp).commit();
    }

    private void initTransaction() {
        String cartModifiedOn = getCartModifiedOn();
        boolean isChanged = mCartService.isChanged(cartModifiedOn);
        if (!isChanged) {
            btnPay.setEnabled(true);
            // TODO: change to get form local DB
            String transactionId = getTransactionId();
            if (transactionId != null)
                getTransactionById(transactionId);
            return;
        }
        String token = getFcmToken();
        TransactionDto transactionDto = new TransactionDto(mCart);
        transactionDto.extras.recipientId = token;
        View view = findViewById(R.id.payment_layout);
        showProgress();
        setCartModifiedOn(mCartService.getCartModifiedOn());
        btnPay.setEnabled(false);
        mSnackBar = Snackbar.make(view, R.string.payment_updating_transaction, Snackbar.LENGTH_INDEFINITE);
        mTransactionRepository.create(transactionDto, (transaction, err) -> {
            if (err != null) {
                err.printStackTrace();
                Snackbar.make(view, err.getMessage(), Snackbar.LENGTH_LONG).show();
            }
            if (transaction == null)
                return;
            mTransaction = transaction;
            saveTransactionId(transaction.id);
            if (mCart.getType() == 1) {
                loadReceipt(transaction.getOrderNumPrint());
            }
            hideProgress();
            if (!isNeedRecalculateCart() || !isChanged)
                updateTransaction(transaction);
            else
                mSnackBar.show();
        });
    }

    private void markTransactionAsPayed() {
        if (mTransaction == null)
            return;
        mTransactionRepository.markAsPayed(mTransaction.id, (transaction, err) -> {
            if (err != null) {
                err.printStackTrace();
                tvError.setText(err.getMessage());
            }
            mTransaction = transaction;
            View view = findViewById(R.id.payment_layout);
            Snackbar.make(view, "Оплата пройшла успішно", Snackbar.LENGTH_LONG).show();
        });
        mCartService.clearCart();
    }

    private void markTransactionAsReject() {
        if (mTransaction == null)
            return;
        mTransactionRepository.markAsRejected(mTransaction.id, (transaction, err) -> {
            if (err != null) {
                err.printStackTrace();
                tvError.setText(err.getMessage());
            }
            mTransaction = transaction;
        });
    }

    private void initWebView() {

        webView = findViewById(R.id.webView);
        // webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);

        Log.d(TAG,"display widtht:"+getResources().getDisplayMetrics().widthPixels+ " with density:"+384 * getResources().getDisplayMetrics().density);
        //меняем ширину webview для планшетов на 384 (max для принтера !!!)
        if (isTablet(this)) {
            webView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            (int) (384 * getResources().getDisplayMetrics().density), LinearLayout.LayoutParams.WRAP_CONTENT ));
        }

        webView.setVisibility(View.GONE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG,"onPageStarted url:"+url);
                showProgress();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG,"onPageFinished url:"+url);
                if (mErrorCode == 0) {
                    hideProgress();
                    viewError.setVisibility(View.GONE);
                    webView.setVisibility(View.INVISIBLE);
                    btnPrint.setEnabled(true);

                } else {
                    showLoadError(mErrorCode);
                }
            }


            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Log.d(TAG, "onReceivedError old error:" + errorCode);
                    mErrorCode = errorCode;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.d(TAG,"onReceivedError error:"+error.getErrorCode());
                mErrorCode = error.getErrorCode();
            }

        });

    }

    private void showLoadError(int error) {
        hideProgress();
        webView.setVisibility(View.GONE);
        viewError.setVisibility(View.VISIBLE);
        tvError.setText(getString(R.string.error_on_client));
    }

    private boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private Bitmap getBitmapFromWebView(final WebView webView){
        Bitmap bitmap = null;
        try {
            Picture picture = webView.capturePicture();
            bitmap = Bitmap.createBitmap(picture.getWidth(),picture.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            picture.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap getProportionalBitmap(Bitmap bitmap, int newDimensionXorY, String XorY) {
        if (bitmap == null) {
            return null;
        }

        float xyRatio = 0;
        int newWidth = 0;
        int newHeight = 0;

        if (XorY.toLowerCase().equals("x")) {

            xyRatio = (float) newDimensionXorY / bitmap.getWidth();
            newHeight = (int) (bitmap.getHeight() * xyRatio);
            bitmap = Bitmap.createScaledBitmap(bitmap, newDimensionXorY, newHeight, true);

        } else if (XorY.toLowerCase().equals("y")) {

            xyRatio = (float) newDimensionXorY / bitmap.getHeight();
            newWidth = (int) (bitmap.getWidth() * xyRatio);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newDimensionXorY, true);

        }
        return bitmap;
    }

    private  void onCancelButtonClick(View view) {
        this.onBackPressed();
    }

    private void sendToCashDesk() {
        markTransactionAsPayed();
        if (mTransaction != null)
            loadReceipt(mTransaction.getOrderNumPrint());
    }

    private void onReceipt() {
        ArrayList<String> htmlBuilder = new ArrayList<>();
        htmlBuilder.add("<html>");
        htmlBuilder.add("<body>");
        htmlBuilder.add("<div style='display: flex; justify-content: center'>");
        htmlBuilder.add("<h1>Фіскальний чек</h1>");
        htmlBuilder.add("</div>");
        htmlBuilder.add("<table width=100%>");
        for(CartItem item : mCart.getItems())
            htmlBuilder.add(String.format(
                    "<tr><td>%s</td><td>x%s</td><td><div style='margin-left: 6px'>%s грн.</div></td></tr>",
                    item.getItemName(), item.getQty(),item.getPrice()));
        htmlBuilder.add(
                String.format(
                        "<tr><td><div style='margin-top: 48px'>Всього</div></td><td></td><td><div style='margin-top: 48px'>%s грн.</div></td><tr>",
                        mCart.getTotalPrice())
        );
        htmlBuilder.add("</table>");
        htmlBuilder.add("</div>");
        htmlBuilder.add("</body>");
        htmlBuilder.add("</html>");
        String receipt = "";
        for( String line : htmlBuilder)
            receipt += line + "\n";
        loadReceipt(receipt);
    }

    private void loadReceipt(String receipt) {
        mReceipt = receipt;
        webView.loadData(mReceipt,"text/html; charset=utf-8","UTF-8");
    }

    private void showPrinterDialog() {
        mDialog = new ChoicePrinterDialog().newInstance();
        mDialog.show(this);
    }

    private void showProgress() {
        ProgressBar progressView = findViewById(R.id.progress);
        if (progressView != null) {
            progressView.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        ProgressBar progressView = findViewById(R.id.progress);
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
        }
    }

    private void makePayment(String amount) {
		Intent i = new Intent("com.sccp.gpb.emv.MAKE_PAYMENT");
		i.putExtra("amount", amount);
		i.putExtra("source", getApplication().getPackageName());
        try {
			showProgress();
			startActivityForResult(i, MAKE_PAYMENT_CODE);
		} catch (android.content.ActivityNotFoundException e) {
            hideProgress();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
        findViewById(R.id.btn_pay).setEnabled(true);
    }

    private String getEditTextValue(int id) {
        TextView view = (TextView) findViewById(id);
        if (view != null)
            return view.getText().toString();
        return "";
    }

    private void setEditTextValue(int id, String text) {
        TextView view = (TextView) findViewById(id);
        if (view != null)
            view.setText(text);
    }

    private void openOkDialog(String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(message)
                .setPositiveButton(R.string.ok, listener)
                .create()
                .show();
    }

    private void createTransaction(Intent data) {
        String transaction_response_code = data.getStringExtra("transaction_response_code");
        String transaction_number = data.getStringExtra("transaction_number");
        String transaction_ref = data.getStringExtra("transaction_ref");
        String amount = data.getStringExtra("amount");
        String order_info = data.getStringExtra("order_info");
        String auth_code = data.getStringExtra("auth_code");
        String extra = data.getStringExtra("extra");
        String email = data.getStringExtra("customer_email");
        String mobile = data.getStringExtra("mobile");
        String ref1 = data.getStringExtra("ref_1");
        String ref2 = data.getStringExtra("ref_2");
        String ref3 = data.getStringExtra("ref_3");
        String ref4 = data.getStringExtra("ref_4");

        String card_type = data.getStringExtra("card_type");
        String cc_name = data.getStringExtra("cc_name");
        String cc_number = data.getStringExtra("cc_number");
        String currency = data.getStringExtra("currency");

        String tsi = data.getStringExtra("TSI");
        String tvr = data.getStringExtra("TVR");
        String cvm_result = data.getStringExtra("CVMResult");
        String msg = String.format("transaction_response_code: %s\n", transaction_response_code);
        msg += String.format("transaction_number: %s\n", transaction_number);
        msg += String.format("transaction_ref: %s\n", transaction_ref);
        msg += String.format("amount: %s\n", amount);
        msg += String.format("order_info: %s\n", order_info);
        msg += String.format("auth_code: %s\n", auth_code);
        msg += String.format("card_type: %s\n", card_type);
        msg += String.format("cc_name: %s\n", cc_name);
        msg += String.format("cc_number: %s\n", cc_number);
        msg += String.format("tsi: %s\n", tsi);
        msg += String.format("tvr: %s\n", tvr);
        msg += String.format("cvm_result: %s\n", cvm_result);
        // Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void showOkAnimation() {

    }

    private void log(String message)
    {
        Log.d("REST", message);
    }

	/*
		PRIVATE
	 */

    /**
     * проверка пользовательских прав доступа
     */
    private boolean checkUserPermission() {
        // Android M Permission check
        if (!isPermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
            return false;
        } else
            return true;
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showPinpadDialog() {
        if (checkUserPermission()) {
            mDialog = ChoicePinpadDialog.newInstance();
            mDialog.show(this);
        }
    }

    private void warning(final String text) {
        Log.d(TAG, text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makePaymentPrivate(Double amount) {
        mAmount = amount;
        mReceipt = "";
        try {
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                Toast.makeText(this, R.string.msg_bluetooth_is_not_supported, Toast.LENGTH_SHORT).show();
                findViewById(R.id.btn_pay).setEnabled(true);
                return;
            }
            MiniPosManager mng = MiniPosManager.getInstance();
            mng.initPinpad(pinpadConnectionListener);
            mng.setServiceErrorListener(miniPosServiceErrorListener);
        } catch (Exception err) {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
            findViewById(R.id.btn_pay).setEnabled(true);
        }
    }

    // endregion

    // region Listeners
    /**
     * PinPad ConnectionListener
     */
    private MiniPosConnectionListener pinpadConnectionListener = new MiniPosConnectionListener() {
        @Override
        public void onConnectionSuccess(String sn) {
            showProgress();
            MiniPosManager.getInstance().startTransaction(mAmount, mPurpose, transactionListener);
        }

        @Override
        public void onConnectionFailed() {
            hideProgress();
            //Toast.makeText(PaymentActivity.this, "MiniPosConnectionListener.onConnectionFailed", Toast.LENGTH_SHORT).show();
            try {
                showPinpadDialog();
            } catch (Exception err) {
                Toast.makeText(PaymentActivity.this, err.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDeviceRelease() {
            hideProgress();
            String msg = getString(R.string.pinpad_released);
            Log.d(TAG, msg);
            setStatusMessage(msg);
        }

        @Override
        public void onBluetoothRequest() {
            //запрос на включение блютуза
            Toast.makeText(PaymentActivity.this, R.string.turn_on_bluetooth, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Transaction listener
     */
    private MiniPosTransactionListener transactionListener = new MiniPosTransactionListener() {

        @Override
        public void onExeption(String message) {
            Log.d(TAG, "onException messsage:" + message);
            hideProgress();
        }

        @Override
        public void onUpdateUserInterface(String message) {
            setStatusMessage(message);
        }


        @Override
        public void onTransactionFinish(Transaction transaction) {
            Log.d(TAG, "onTransactionFinish");
            hideProgress();
            String receipt = transaction.getTransactionData().getReceipt(); //получаем чек
            markTransactionAsPayed();
            loadReceipt(receipt);
            showPrintButton();
        }
    };

    /**
     * Service Error Listener
     */
    private MiniPosServiceErrorListener miniPosServiceErrorListener = new MiniPosServiceErrorListener() {
        @Override
        public void onError(String message) {
            Log.d(TAG, "ERROR:" + message);
            setStatusMessage(message);
            hideProgress();
        }

        @Override
        public void onUnauthorized() {
            Log.d(TAG, "ERROR: необходима авторизация");
            tvMessage.setText(R.string.authorisation_required);
            hideProgress();
        }
    };

    /**
     * Printer listener
     */
    private MiniPosPrinterListener printerListener = new MiniPosPrinterListener() {
        @Override
        public void onPrintStateChanged(String message) {
            Log.d(TAG, "Ошибка принтера:"+message);
            hideProgress();
            warning(message);
        }

        @Override
        public void onPrintStart() {
            Log.d(TAG, "Печать чека...");
            showProgress();
        }

        @Override
        public void onPrintFinish() {
            Log.d(TAG, "Печать завершена");
            hideProgress();
        }
    };

    /**
     * Printer ConnectionListener
     */
    private MiniPosConnectionListener printerConnectionListener = new MiniPosConnectionListener() {
        @Override
        public void onConnectionSuccess(String sn) {
            Log.d(TAG, "Принтер подключен");
            MiniPosManager.getInstance().startPrint(mReceiptBitmap, printerListener);
        }

        @Override
        public void onConnectionFailed() {
            Log.d(TAG, "Ошибка соединения");
            showPrinterDialog();
        }

        @Override
        public void onDeviceRelease() {
//принтер отключен
            String msg = getString(R.string.message_printer_disconnected);
            Log.d(TAG, msg);
            warning(msg);
        }

        @Override
        public void onBluetoothRequest() {
            //запрос на включение блютуза
            Toast.makeText(getApplicationContext(), R.string.turn_on_bluetooth, Toast.LENGTH_SHORT).show();
        }
    };

    // endregion
}
