package ua.com.atcorp.mobilecashdesk.ui.dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.reactiveandroid.query.Delete;
import com.reactiveandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import ua.com.atcorp.mobilecashdesk.models.PairedDevice;
import ua.pbank.dio.minipos.MiniPosManager;
import ua.pbank.dio.minipos.interfaces.MiniPosConnectionListener;
import ua.com.atcorp.mobilecashdesk.R;


/**
 * Created by dn160671kav on 17.08.2016.
 */
public class ChoicePinpadDialog extends BaseChoiceDeviceDialog {

    private static final String TAG = ChoicePinpadDialog.class.getName();

    @Override
    protected String getDialogTag() {
        return TAG;
    }

    public static ChoicePinpadDialog newInstance() {
        ChoicePinpadDialog dialog = new ChoicePinpadDialog();
        return dialog;
    }

    @Override
    public String getSavedDeviceAddress() {
        PairedDevice device = getSavedDevice();
        if (device != null) Log.d(TAG,"srored address:"+device.getAddress());
        return (device != null ? device.getAddress() : null);
    }

    private  PairedDevice getSavedDevice() {
        PairedDevice device = Select
                .from(PairedDevice.class)
                .where("type = ?","t")
                .fetchSingle();
        return  device;
    }

    @Override
    protected int getImageRes() {
        if (getSavedDeviceAddress() != null)
            return R.drawable.red_pad;
        else
            return R.drawable.pad;
    }

    @Override
    protected String getMessage() {
        if (getSavedDeviceAddress() != null)
            return getString(R.string.message_pinpad);
        else
            return getString(R.string.message_pinpad_new);
    }

    @Override
    protected void connectToDevice(final BluetoothDevice device) {
        //
        logLocalPaired(); //log from DB
        //
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        MiniPosManager.getInstance().connectToPinpad(bluetoothAdapter, device, new MiniPosConnectionListener() {
            @Override
            public void onConnectionSuccess(String sn) {
                saveDevice(device, sn, true);
                if (getActivity() != null) {
                    showToast(getString(R.string.connect_device_success));
                    MiniPosConnectionListener miniPosConnectionListener = MiniPosManager.getInstance().getPinpadConnectionListener();
                    if (miniPosConnectionListener != null) {
                        miniPosConnectionListener.onConnectionSuccess(sn);
                    }
                }
                dismiss();
            }

            @Override
            public void onConnectionFailed() {
                if (getActivity() != null) {
                    showToast(getString(R.string.connect_device_error));
                    scanDevice();
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
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> startScan());
    }

    /**
     * сохраняем в базе успешно подсоединенное устройство
     * @param device
     */
    public void saveDevice(BluetoothDevice device, String serialnumber, boolean verified) {
       Delete
                .from(PairedDevice.class)
                .where("type = ?","t")
                .execute(); //удаляем сохраненные
        PairedDevice pairedDevice = new PairedDevice("",device.getAddress(),"t"); //терминал
        pairedDevice.setSerialNumber(serialnumber);
        pairedDevice.save();
    }


    public void deleteSavedDevice(BluetoothDevice device) {
        Delete
                .from(PairedDevice.class)
                .where("address = ?",device.getAddress())
                .execute();
    }



    /**
     * вывод лога для отладки
     */
    private void logLocalPaired() {
        List<PairedDevice> list;
        list  = Select
                .from(PairedDevice.class)
                .where("type = ?","t")
                .fetch();
        for (PairedDevice pairedDevice :list ) {
            Log.d(TAG,"DataBase PairedDevice name="+pairedDevice.getName()+" mac:"+pairedDevice.getAddress()+" type:"+pairedDevice.getType());
        }
    }
}
