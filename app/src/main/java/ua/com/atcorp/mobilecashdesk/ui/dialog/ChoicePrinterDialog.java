package ua.com.atcorp.mobilecashdesk.ui.dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.reactiveandroid.query.Delete;
import com.reactiveandroid.query.Select;

import ua.com.atcorp.mobilecashdesk.models.PairedDevice;
import ua.pbank.dio.minipos.MiniPosManager;
import ua.pbank.dio.minipos.interfaces.MiniPosConnectionListener;

import ua.com.atcorp.mobilecashdesk.R;

/**
 * Created by dn160671kav on 17.08.2016.
 */
public class ChoicePrinterDialog extends BaseChoiceDeviceDialog {

    private static final String TAG = ChoicePrinterDialog.class.getName();

    public static ChoicePrinterDialog newInstance() {
        ChoicePrinterDialog dialog = new ChoicePrinterDialog();
        return dialog;
    }

    @Override
    protected String getDialogTag() {
        return TAG;
    }


    @Override
    public String getSavedDeviceAddress() {
        PairedDevice device = Select
                .from(PairedDevice.class)
                .where("type = ?","p")
                .fetchSingle();
        if (device != null) Log.d(TAG,"srored address:"+device.getAddress());
        return (device != null ? device.getAddress() : null);
    }

    @Override
    protected int getImageRes() {
        if (getSavedDeviceAddress() != null)
            return R.drawable.red_print;
        else
            return R.drawable.print;
    }

    @Override
    protected String getMessage() {
        if (getSavedDeviceAddress() != null)
            return getString(R.string.message_printer);
        else
            return getString(R.string.message_printer_new);
    }

    @Override
    protected void connectToDevice(final BluetoothDevice device) {

        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        /**
         *
         */
        MiniPosManager.getInstance().connectToPrinter(bluetoothAdapter, device, new MiniPosConnectionListener() {
            @Override
            public void onConnectionSuccess(String sn) {
                saveDevice(device);
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> {
                        showToast(getString(R.string.connect_device_success));
                        MiniPosConnectionListener miniPosConnectionListener = MiniPosManager.getInstance().getPrinterConnectionListener();
                        if (miniPosConnectionListener != null) {
                            miniPosConnectionListener.onConnectionSuccess(sn);
                        }
                        dismiss();
                    });
            }

            @Override
            public void onConnectionFailed() {
                if (getActivity() != null) {
                    showToast(getString(R.string.connect_device_error));
                    final Thread thread = new Thread(() -> scanDevice());
                    thread.start();
                }
            }

            @Override
            public void onDeviceRelease() {
                onConnectionFailed();
            }

            @Override
            public void onBluetoothRequest() {

            }
        });

    }

    public void scanDevice() {
        getActivity().runOnUiThread(() -> startScan());
    }

    public void saveDevice(BluetoothDevice device) {
        Delete
                .from(PairedDevice.class)
                .where("type = ?","p")
                .execute();   //удаляем сохраненные
        PairedDevice pairedDevice = new PairedDevice("",device.getAddress(),"p"); //принтер
        pairedDevice.save();
    }



}
