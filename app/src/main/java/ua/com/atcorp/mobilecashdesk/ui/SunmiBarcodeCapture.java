package ua.com.atcorp.mobilecashdesk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.sunmi.scan.Config;
import com.sunmi.scan.Image;
import com.sunmi.scan.ImageScanner;
import com.sunmi.scan.Symbol;
import com.sunmi.scan.SymbolSet;

import ua.com.atcorp.mobilecashdesk.R;
import ua.com.atcorp.mobilecashdesk.ui.sunmi.scanner.FinderView;

public class SunmiBarcodeCapture extends Activity implements SurfaceHolder.Callback {

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private SurfaceView surface_view;
    private ImageScanner scanner;
    private Handler autoFocusHandler;
    private AsyncDecode asyncDecode;
    public int decode_count = 0;

    private FinderView finder_view;
    private TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunmi_barcode_capture);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.destroy();
        }
    }

    private void init() {
        surface_view = findViewById(R.id.surface_view);
        finder_view = findViewById(R.id.finder_view);
        textview = findViewById(R.id.textview);
        mHolder = surface_view.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);

        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 2);
        scanner.setConfig(0, Config.Y_DENSITY, 2);
        scanner.setConfig(0, Config.ENABLE_MULTILESYMS, 0);
        scanner.setConfig(0, Config.ENABLE_INVERSE, 0);
        scanner.setConfig(Symbol.PDF417, Config.ENABLE, 0);

        autoFocusHandler = new Handler();
        asyncDecode = new AsyncDecode();
        decode_count = 0;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }
        try {
//			Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setPreviewSize(800, 480);
            //     parameters.set("zoom", String.valueOf(27 / 10.0));
//            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCallback);
        } catch (Exception e) {
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (asyncDecode.isStoped()) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();

                Image source = new Image(size.width, size.height, "Y800");
                Rect scanImageRect = finder_view.getScanImageRect(size.height, size.width);
                source.setCrop(scanImageRect.top, scanImageRect.left, scanImageRect.height(), scanImageRect.width());
                source.setData(data);
                asyncDecode = new AsyncDecode();
                asyncDecode.execute(source);
            }
        }
    };

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (null == mCamera || null == autoFocusCallback) {
                return;
            }
            mCamera.autoFocus(autoFocusCallback);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.d("DBG", "surfaceCreated: " + e.getMessage());
            mCamera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private class AsyncDecode extends AsyncTask<Image, Void, Void> {
        private boolean stoped = true;
        private String barcode = "";

        @Override
        protected Void doInBackground(Image... params) {
            stoped = false;
            StringBuilder sb = new StringBuilder();
            Image src_data = params[0];

            long startTimeMillis = System.currentTimeMillis();

            int nsyms = scanner.scanImage(src_data);

            long endTimeMillis = System.currentTimeMillis();
            long cost_time = endTimeMillis - startTimeMillis;

            if (nsyms != 0) {

                decode_count++;
                sb.append("decode_count: " + String.valueOf(decode_count) + ", cost_time: " + String.valueOf(cost_time) + " ms \n");

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    barcode = sym.getResult();
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            stoped = true;
            if (null != barcode && !barcode.equals("")) {
                textview.setText(barcode);
                Intent intent = new Intent();
                intent.putExtra("barCode",  barcode);
                setResult(CommonStatusCodes.SUCCESS, intent);
                finish();
            }
        }

        public boolean isStoped() {
            return stoped;
        }
    }

}
