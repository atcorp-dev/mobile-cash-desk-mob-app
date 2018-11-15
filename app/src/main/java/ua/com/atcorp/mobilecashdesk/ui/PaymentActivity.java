package ua.com.atcorp.mobilecashdesk.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.support.v7.app.*;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.repositories.BaseRepository;
import ua.com.atcorp.mobilecashdesk.repositories.TransactionRepository;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;
import ua.com.atcorp.mobilecashdesk.services.CartService;
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
    private BaseDialogFragment mDialog;
    private Double mAmount;
    private String mStrAmount;
    private String mPurpose;
    private String mReceipt;
    private Cart mCart;
    private Bitmap mReceiptBitmap;
    private TransactionRepository mTransactionRepository;
    private TransactionDto mTransaction;

    @BindView(R.id.payment_status_message)
    TextView tvMessage;
    @BindView(R.id.purpose)
    TextView tvPurpose;
    @BindView(R.id.btn_pay)
    Button btnPay;
    @BindView(R.id.btn_print)
    Button btnPrint;
    // @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.tvError)
    TextView tvError;
    @BindView(R.id.viewError)
    View viewError;
    private int mErrorCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ButterKnife.bind(this);
        Intent intent = getIntent();

        double amount = intent.getDoubleExtra("amount", 0);
        int intValue = (int)amount;
        int rest = (int) ((amount - intValue) * 100);
        String amountStr = String.format("%s.%s", intValue, rest);
        String price = formatPrice(amount);
        mAmount = amount;
        mStrAmount = amountStr;
        setEditTextValue(R.id.payment_amount, price);

        mCart = new CartService(this).restoreState().getCurrentCart();
        btnPay = findViewById(R.id.btn_pay);
        if (mCart.getType() == 1)
            btnPay.setText("Сплатити на касі");
        initWebView();
        mPurpose = "Покупка в магазині";
        tvPurpose.setText(mPurpose);
        //onReceipt();
        mTransactionRepository = new TransactionRepository(this);
        initTransaction();
    }

    private void initTransaction() {
        TransactionDto transactionDto = new TransactionDto(mCart);
        showProgress();
        View view = findViewById(R.id.payment_layout);
        mTransactionRepository.create(transactionDto, (transaction, err) -> {
            if (err != null) {
                err.printStackTrace();
                Snackbar.make(view, err.getMessage(), Snackbar.LENGTH_LONG).show();
            }
            mTransaction = transaction;
            if (transaction.type == 1) {
                loadReceipt(transaction.getOrderNumPrint());
            }
            hideProgress();
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
        });
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

    public static boolean isTablet(Context context) {
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

    public Bitmap getProportionalBitmap(Bitmap bitmap, int newDimensionXorY, String XorY) {
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

    public  void onCancelButtonClick(View view) {
        this.onBackPressed();
    }

    public  void onPayment(View view) {
        // view.setEnabled(false);
        try {
            if (mCart.getType() == 1) {
                sentToCashDesk();
                return;
            }
        // makePayment(mStrAmount);
            makePaymentPrivate(mAmount);
        } catch (Exception err) {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
            String m = "";
            for(StackTraceElement line : err.getStackTrace())
                m += line.getClassName() + "." + line.getMethodName() + "." + line.getLineNumber();
            Toast.makeText(this, m, Toast.LENGTH_LONG).show();
        }
    }

    public void sentToCashDesk() {
        markTransactionAsPayed();
    }

    public void onReceipt() {
        ArrayList<String> htmlBuilder = new ArrayList<>();
        htmlBuilder.add("<html>");
        htmlBuilder.add("<body>");
        htmlBuilder.add("<div style='display: flex; justify-content: center'>");
        htmlBuilder.add("<h1>Фіскальний чек</h1>");
        htmlBuilder.add("</div>");
        htmlBuilder.add("<table width=100%>");
        for(CartItem item : mCart.getmItems())
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

    void loadReceipt(String receipt) {
        mReceipt = receipt;
        webView.loadData(mReceipt,"text/html; charset=utf-8","UTF-8");
    }

    public void onPrint(View view) {
        ImageView imgView = findViewById(R.id.printPreview);
        final Bitmap bitmap = getBitmapFromWebView(webView);
        mReceiptBitmap  =  getProportionalBitmap(bitmap,384,"X"); //horizontal max dot 384
        Bitmap previewBitmap  =  getProportionalBitmap(bitmap,bitmap.getWidth(),"X");
        bitmap.recycle();
        imgView.setImageBitmap(previewBitmap);
        if (mCart.getType() == 0)
            MiniPosManager.getInstance().initPrinter(printerConnectionListener);
    }

    private void showPrinterDialog() {
        mDialog = new ChoicePrinterDialog().newInstance();
        mDialog.show(this);
    }

    public void showProgress() {
        ProgressBar progressView = findViewById(R.id.progress);
        if (progressView != null) {
            progressView.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgress() {
        ProgressBar progressView = findViewById(R.id.progress);
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
        }
    }

    void makePayment(String amount) {
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

    String getEditTextValue(int id) {
        TextView view = (TextView) findViewById(id);
        if (view != null)
            return view.getText().toString();
        return "";
    }

    void setEditTextValue(int id, String text) {
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
            tvMessage.setText(msg);
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
            tvMessage.setText(message);
        }


        @Override
        public void onTransactionFinish(Transaction transaction) {
            Log.d(TAG, "onTransactionFinish");
            hideProgress();
            String receipt = transaction.getTransactionData().getReceipt(); //получаем чек
            markTransactionAsPayed();
            loadReceipt(receipt);
        }
    };

    /**
     * Service Error Listener
     */
    private MiniPosServiceErrorListener miniPosServiceErrorListener = new MiniPosServiceErrorListener() {
        @Override
        public void onError(String message) {
            Log.d(TAG, "ERROR:" + message);
            tvMessage.setText(message);
            hideProgress();
        }

        @Override
        public void onUnauthorized() {
            Log.d(TAG, "ERROR: необходима авторизация");
            tvMessage.setText("Необходима авторизация");
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

    private String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String strPrice = df.format(price) + " грн.";
        return strPrice;
    }
}
