package obd.mapbar.com.bleperipheraldemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author huangkeyuan
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    public static final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private TextView logText;
    private static final UUID UUID_SERVER = UUID.fromString("0000fff0-0000-1000-8000-2ca5b7590d49");
    private static final UUID UUID_NOTIFY = UUID.fromString("0000fff1-0000-1000-8000-2ca5b7590d49");
    private static final UUID UUID_WRITE = UUID.fromString("0000fff2-0000-1000-8000-2ca5b7590d49");
    private static final UUID UUID_DESCRIPTOR = UUID.fromString
            ("00002902-0000-1000-8000-00805F9B34FB");
    private BluetoothManager bluetoothManager;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothGattCharacteristic characteristicNotify;
    private BluetoothGattCharacteristic characteristicWrite;
    private Disposable ssidDisposable;
    private Disposable passwordDisposable;
    private BluetoothDevice connectedDevice;
    private TextView enEditText;
    private EditText passwordEditText;
    private TextView ssidEditText;
    private Button actionAd;

    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_WEP = 1;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_EAP = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }/* Initializes Bluetooth adapter.*/
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.setName("A3");/*2.Enable Bluetooth Ensures Bluetooth is available on the
         device and it is enabled.  If not, displays a
        dialog requesting user permission to enable Bluetooth.*/
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        logText.setText("");
        showText("starting ...");
    }

    private void initView() {
        logText = findViewById(R.id.activity_main_log);
        enEditText = findViewById(R.id.activity_main_en);
        passwordEditText = findViewById(R.id.activity_main_password);
        ssidEditText = findViewById(R.id.activity_main_ssid);
        actionAd = findViewById(R.id.activity_main_action);
        passwordEditText.setText(SharedPrefsUtil.getValue(this, "PASSWORD", ""));
        try {
            WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext()
                    .getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            Log.d("wifiInfo", wifiInfo.toString());
            Log.d("SSID", wifiInfo.getSSID());
            for (WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
                if (wifiConfiguration.SSID.equals(wifiInfo.getSSID())) {
                    switch (getSecurity(wifiConfiguration)) {
                        case SECURITY_NONE:
                            enEditText.setText(String.valueOf(2));
                            break;
                        case SECURITY_WEP:
                            enEditText.setText(String.valueOf(1));
                            break;
                        case SECURITY_PSK:
                        case SECURITY_EAP:
                            enEditText.setText(String.valueOf(0));
                            break;
                        default:
                            enEditText.setText(String.valueOf(0));
                            break;
                    }
                }
            }
            ssidEditText.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        } catch (Exception e) {
            e.printStackTrace();
            ssidEditText.setText("请连接WIFI");
        }
        actionAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(passwordEditText.getText().toString()) || TextUtils.isEmpty
                        (ssidEditText.getText().toString()) || TextUtils
                        .isEmpty(enEditText.getText().toString())) {
                    showText("需要填写完整的配置信息");
                    return;
                }
                SharedPrefsUtil.putValue(MainActivity.this, "EN", Integer.valueOf(enEditText
                        .getText().toString().trim()));
                SharedPrefsUtil.putValue(MainActivity.this, "PASSWORD", passwordEditText.getText
                        ().toString().trim());
                initGattServer();
            }
        });
    }



    /**
     * 获取WIFI的加密方式
     *
     * @param config
     * @return
     */
    private int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config
                .allowedKeyManagement.get(WifiConfiguration.KeyMgmt
                        .IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    private void initGattServer() {
        AdvertiseSettings settings = new AdvertiseSettings.Builder().setConnectable(true)
                .setAdvertiseMode(AdvertiseSettings
                        .ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings
                        .ADVERTISE_TX_POWER_HIGH).build();
        AdvertiseData advertiseData = new AdvertiseData.Builder().setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true).addServiceUuid(new
                        ParcelUuid(UUID_SERVER)).build();
        AdvertiseData scanResponseData = new AdvertiseData.Builder().addServiceUuid(new
                ParcelUuid(UUID_SERVER)).setIncludeTxPowerLevel(true).build();
        AdvertiseCallback callback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.d(TAG, "BLE advertisement added successfully");
                showText("1. initGATTServer success");
                println("1. initGATTServer success");
                actionAd.setEnabled(false);
                initServices(getContext());
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Failed to add BLE advertisement, reason: " + errorCode);
                showText("1. initGATTServer failure");
            }
        };
        BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, callback);
    }

    private void initServices(Context context) {
        bluetoothGattServer = bluetoothManager.openGattServer(context, bluetoothGattServerCallback);
        BluetoothGattService service = new BluetoothGattService(UUID_SERVER, BluetoothGattService
                .SERVICE_TYPE_PRIMARY);/*add a read characteristic.*/
        characteristicNotify = new BluetoothGattCharacteristic(UUID_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);/*add a descriptor*/
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(UUID_DESCRIPTOR,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicNotify.addDescriptor(descriptor);
        service.addCharacteristic(characteristicNotify);/*add a write characteristic.*/
        characteristicWrite = new BluetoothGattCharacteristic(UUID_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic
                .PERMISSION_WRITE);
        service.addCharacteristic(characteristicWrite);
        bluetoothGattServer.addService(service);
        Log.e(TAG, "2. initServices ok");
        showText("2. initServices ok");
    }

    private BluetoothGattServerCallback bluetoothGattServerCallback = new
            BluetoothGattServerCallback() {
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int
                        newState) {
                    Log.e(TAG, String.format("1.onConnectionStateChange：device name = %s, address" +
                                    " = %s",
                            device.getName(), device.getAddress()));
                    Log.e(TAG, String.format("1.onConnectionStateChange：status = %s, newState =%s ",
                            status, newState));
                    super.onConnectionStateChange(device, status, newState);
                    switch (newState) {
                        case BluetoothProfile.STATE_DISCONNECTED:
                            showText("mac:" + device.getAddress() + " 断开连接");
                            if (ssidDisposable != null && !ssidDisposable.isDisposed()) {
                                ssidDisposable.dispose();
                            }
                            break;
                        case BluetoothProfile.STATE_CONNECTED:
                            connectedDevice = device;
                            showText("MAC:" + device.getAddress() + " 已连接");
                            showText("1秒后发送SSID指令");
                            sendSsid();
                            break;
                        case BluetoothProfile.STATE_CONNECTING:
                            showText("MAC:" + device.getAddress() + " 连接中");
                            break;
                        case BluetoothProfile.STATE_DISCONNECTING:
                            showText("MAC:" + device.getAddress() + " 断开连接中");
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onServiceAdded(int status, BluetoothGattService service) {
                    super.onServiceAdded(status, service);
                    Log.e(TAG, String.format("onServiceAdded：status = %s", status));
                }

                @Override
                public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int
                        offset, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                    Log.e(TAG, String.format("onCharacteristicReadRequest：device name = %s, " +
                            "address = " +
                            "%s", device.getName(), device.getAddress()));
                    Log.e(TAG, String.format("onCharacteristicReadRequest：requestId = %s, offset " +
                                    "= %s",
                            requestId, offset));
                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt
                                    .GATT_SUCCESS, 0,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }

                @Override
                public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                         BluetoothGattCharacteristic
                                                                 characteristic, boolean
                                                                 preparedWrite, boolean
                                                                 responseNeeded, int offset,
                                                         byte[] requestBytes) {
                    Log.e(TAG, String.format("3.onCharacteristicWriteRequest：device name = %s, " +
                            "address = " +
                            "%s", device.getName(), device.getAddress()));
                    Log.e(TAG, String.format("3.onCharacteristicWriteRequest：requestId = %s, " +
                                    "preparedWrite=%s, responseNeeded=%s, offset=%s, value=%s",
                            requestId, preparedWrite, responseNeeded, offset, OutputStringUtil
                                    .toHexString(requestBytes)));
                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                            offset, requestBytes);/*4.处理响应内容*/
                    onResponseToClient(requestBytes, device, requestId, characteristic);
                }

                @Override
                public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattDescriptor descriptor, boolean
                                                             preparedWrite,
                                                     boolean responseNeeded, int offset, byte[]
                                                             value) {
                    Log.e(TAG, String.format("2.onDescriptorWriteRequest：device name = %s, " +
                                    "address = %s",
                            device.getName(), device.getAddress()));
                    Log.e(TAG, String.format("2.onDescriptorWriteRequest：requestId = %s, " +
                                    "preparedWrite = " +
                                    "" + "%s, " + "responseNeeded = %s, offset = %s, " +
                                    "value = %s,", requestId, preparedWrite, responseNeeded, offset,
                            OutputStringUtil.toHexString(value)));/* now tell the
                    connected device that this was all successfull*/
                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                            offset, value);
                }

                @Override
                public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int
                        offset,
                                                    BluetoothGattDescriptor descriptor) {
                    Log.e(TAG, String.format("onDescriptorReadRequest：device name = %s, address =" +
                                    " %s",
                            device.getName(), device.getAddress()));
                    Log.e(TAG, String.format("onDescriptorReadRequest：requestId = %s", requestId));
                    super.onDescriptorReadRequest(device, requestId, offset, descriptor);
                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                            offset, null);
                }

                @Override
                public void onNotificationSent(BluetoothDevice device, int status) {
                    super.onNotificationSent(device, status);
                    Log.e(TAG, String.format("5.onNotificationSent：device name = %s, address = %s",
                            device.getName(), device.getAddress()));
                    Log.e(TAG, String.format("5.onNotificationSent：status = %s", status));
                }

                @Override
                public void onMtuChanged(BluetoothDevice device, int mtu) {
                    super.onMtuChanged(device, mtu);
                    Log.e(TAG, String.format("onMtuChanged：mtu = %s", mtu));
                }

                @Override
                public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                    super.onExecuteWrite(device, requestId, execute);
                    Log.e(TAG, String.format("onExecuteWrite：requestId = %s", requestId));
                }
            };

    /**
     * 处理收到的数据
     */
    private void onResponseToClient(byte[] reqeustBytes, BluetoothDevice device, int requestId,
                                    BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, String.format("4.onResponseToClient：device name = %s, address = %s", device
                .getName(), device.getAddress()));
        Log.e(TAG, String.format("4.onResponseToClient：requestId = %s", requestId));
        String msg = OutputStringUtil.toHexString(reqeustBytes);
        if (Utils.xorResult(reqeustBytes, 1, reqeustBytes.length - 1) ==
                reqeustBytes[reqeustBytes.length - 2] && reqeustBytes[0] == (byte) 0XB6 &&
                reqeustBytes.length >= 4) {
            showText("收到:" + msg + "  校验通过");
            switch (reqeustBytes[1]) {
                case 0X01:
                    if (ssidDisposable != null && !ssidDisposable.isDisposed()) {
                        ssidDisposable.dispose();
                    }
                    showText("SSID得到响应，1秒后发送PASSWORD指令");
                    sendPassword();
                    break;
                case 0X02:
                    if (passwordDisposable != null && !passwordDisposable.isDisposed()) {
                        passwordDisposable.dispose();
                    }
                    showText("PASSWORD得到响应，等待配网");
                    break;
                case 0X03:
                    if (reqeustBytes[3] == 0X01) {
                        showText("配网失败");
                        sendSsid();
                    } else if (reqeustBytes[3] == 0X00) {
                        showText("配网成功,即将断开连接");
                    }
                    break;
                default:
                    showText("无法匹配指令");
                    break;
            }
        } else {
            showText("收到:" + msg + "  校验失败");
        }
    }

    private void println(String s) {
        Log.e(TAG, s);
    }

    private void showText(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.append(msg + "\r\n");
            }
        });
    }

    public Context getContext() {
        return this;
    }

    /**
     * 每秒发送SSID指令
     */
    private void sendSsid() {
        if (ssidDisposable != null && !ssidDisposable.isDisposed()) {
            ssidDisposable.dispose();
        }
        ssidDisposable = Observable.interval(1, 1, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io()).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long along) {
                        Log.e("sendssid", "sendssid");
                        try {
                            byte[] buffer = Utils.getSsidData(ssidEditText.getText().toString()
                                    .trim());
                            if (buffer.length > 20) {
                                for (byte[] tmpBytes : Utils.splitBytes(buffer, 20)) {
                                    characteristicNotify.setValue(tmpBytes);
                                    bluetoothGattServer.notifyCharacteristicChanged(connectedDevice,
                                            characteristicNotify, false);
                                }
                            } else {
                                characteristicNotify.setValue(buffer);
                                bluetoothGattServer.notifyCharacteristicChanged(connectedDevice,
                                        characteristicNotify, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e("sendssid", "throwable");
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        Log.e("sendssid", "Action");
                    }
                });
    }

    /**
     * 每秒发送密码指令
     */
    private void sendPassword() {
        if (passwordDisposable != null && !passwordDisposable.isDisposed()) {
            passwordDisposable.dispose();
        }
        passwordDisposable = Observable.interval(1, 1, TimeUnit.SECONDS).observeOn(Schedulers.io
                ()).subscribeOn(Schedulers.io()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long along) {
                Log.e("sendPassword", "sendssid");
                try {
                    byte[] buffer = Utils.getPasswordData(passwordEditText.getText().toString()
                            .trim(), Integer.valueOf(enEditText.getText().toString().trim()));
                    if (buffer.length > 20) {
                        for (byte[] tmpBytes : Utils.splitBytes(buffer, 20)) {
                            characteristicNotify.setValue(tmpBytes);
                            bluetoothGattServer.notifyCharacteristicChanged(connectedDevice,
                                    characteristicNotify, false);
                        }
                    } else {
                        characteristicNotify.setValue(buffer);
                        bluetoothGattServer.notifyCharacteristicChanged(connectedDevice,
                                characteristicNotify, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Log.e("sendPassword", "throwable");
            }
        }, new Action() {
            @Override
            public void run() {
                Log.e("sendPassword", "Action");
            }
        });
    }
}