package ua.com.atcorp.mobilecashdesk.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.ui.dialog.BaseDialogFragment;
import ua.com.atcorp.mobilecashdesk.ui.dialog.ChoicePrinterDialog;
import ua.pbank.dio.minipos.MiniPosManager;
import ua.pbank.dio.minipos.interfaces.MiniPosConnectionListener;
import ua.pbank.dio.minipos.interfaces.MiniPosPrinterListener;

public class PrintReceiptActivity extends AppCompatActivity {

    @BindView(R.id.printPreview)
    ImageView imgView;
    // @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.btn_print)
    Button btnPrint;

    private BaseDialogFragment mDialog;
    private Bitmap mReceiptBitmap;
    int mErrorCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_receipt);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String receipt = intent.getStringExtra("receipt");
        imgView.setVisibility(View.GONE);
        initWebView();
        if (!TextUtils.isEmpty(receipt))
            loadReceipt(receipt);
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

    public void onPrint(View view) {
        previewReceipt();
        MiniPosManager.getInstance().initPrinter(printerConnectionListener);
    }

    private void previewReceipt() {
        webView.setVisibility(View.INVISIBLE);
        imgView.setVisibility(View.VISIBLE);
        final Bitmap bitmap = getBitmapFromWebView(webView);
        mReceiptBitmap  =  getProportionalBitmap(bitmap,384,"X"); //horizontal max dot 384
        Bitmap previewBitmap  =  getProportionalBitmap(bitmap,bitmap.getWidth(),"X");
        bitmap.recycle();
        imgView.setImageBitmap(previewBitmap);
    }

    private void initWebView() {

        webView = findViewById(R.id.webView);
        // webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);

        //меняем ширину webview для планшетов на 384 (max для принтера !!!)
        if (isTablet(this)) {
            webView.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            (int) (384 * getResources().getDisplayMetrics().density), LinearLayout.LayoutParams.WRAP_CONTENT ));
        }

        //webView.setVisibility(View.GONE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mErrorCode == 0) {
                    webView.setVisibility(View.VISIBLE);
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
                    mErrorCode = errorCode;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mErrorCode = error.getErrorCode();
            }

        });

    }

    private void loadReceipt(String receipt) {
        webView.loadData(receipt,"text/html; charset=utf-8","UTF-8");
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

    private void showPrinterDialog() {
        mDialog = new ChoicePrinterDialog().newInstance();
        mDialog.show(this);
    }

    private void showLoadError(int error) {
        String msg = getString(R.string.error_on_client);
        View view = findViewById(R.id.payment_layout);
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .show();
    }

    private void warning(String message) {
        View view = findViewById(R.id.payment_layout);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .show();
    }

    private void info(String message) {
        View view = findViewById(R.id.payment_layout);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .show();
    }

    private Snackbar getSnackBar(String message) {
        View view = findViewById(R.id.payment_layout);
        return Snackbar.make(view, message, Snackbar.LENGTH_LONG);
    }

    // region Listeners
    /**
     * Printer listener
     */
    private MiniPosPrinterListener printerListener = new MiniPosPrinterListener() {

        Snackbar mSnackbar;

        @Override
        public void onPrintStateChanged(String message) {
            warning(message);
        }

        @Override
        public void onPrintStart() {
            mSnackbar = getSnackBar("Печать чека...");
        }

        @Override
        public void onPrintFinish() {
            if (mSnackbar.isShown())
                mSnackbar.dismiss();
            info("Печать завершена");
        }
    };

    /**
     * Printer ConnectionListener
     */
    private MiniPosConnectionListener printerConnectionListener = new MiniPosConnectionListener() {
        @Override
        public void onConnectionSuccess(String sn) {
            info("Принтер подключен");
            MiniPosManager.getInstance().startPrint(mReceiptBitmap, printerListener);
        }

        @Override
        public void onConnectionFailed() {
            showPrinterDialog();
        }

        @Override
        public void onDeviceRelease() {
            //принтер отключен
            String msg = getString(R.string.message_printer_disconnected);
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
