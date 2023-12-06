package com.example.sleepmonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StringBuilder csvDataBuilder = new StringBuilder();
    private StringBuilder csvAverageDataBuilder = new StringBuilder();
    private static final int REQUEST_ENABLE_BT = 10;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> devices;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private Thread workerThread = null;
    private byte[] readBuffer;
    private int readBufferPosition;
    int pariedDeviceCount;
    boolean connect_status;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private Button startButton, nextActivityButton;

    private int timestamp = 0;
    public List<Integer> scores = new ArrayList<>();
    public List<Integer> heartRates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.nextActivityButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }


    private String getFormattedCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }
//    @SuppressLint("MissingPermission")
//    private void setBluetooth() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
//        if (bluetoothAdapter == null) {
//            Toast.makeText(getApplicationContext(), "블루투스 미지원 기기입니다.", Toast.LENGTH_LONG).show();
//            return;
//        }
//        if (bluetoothAdapter.isEnabled()) {
//            selectBluetoothDevice();
//        } else {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_ENABLE_BT);
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    public void selectBluetoothDevice() {
//        devices = bluetoothAdapter.getBondedDevices();
//        pariedDeviceCount = devices.size();
//        if (pariedDeviceCount == 0) {
//            Toast.makeText(getApplicationContext(), "먼저 Bluetooth 설정에 들어가 페어링 해주세요", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        createDeviceDialog();
//    }
//
//    @SuppressLint("MissingPermission")
//    private void createDeviceDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
//        List<String> list = new ArrayList<>();
//        for (BluetoothDevice bluetoothDevice : devices) {
//            list.add(bluetoothDevice.getName());
//        }
//        list.add("취소");
//
//        final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
//        list.toArray(new CharSequence[list.size()]);
//
//        builder.setItems(charSequences, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                connectDevice(charSequences[which].toString());
//            }
//        });
//        builder.setCancelable(false);
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//    }
//
//    @SuppressLint("MissingPermission")
//    private void connectDevice(String deviceName) {
//        for (BluetoothDevice tempDevice : devices) {
//            if (deviceName.equals(tempDevice.getName())) {
//                bluetoothDevice = tempDevice;
//                break;
//            }
//        }
//        Toast.makeText(this, bluetoothDevice + "연결 완료", Toast.LENGTH_SHORT).show();
//        connect_status = true;
//
//        try {
//            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            bluetoothSocket.connect();
//        } catch (IOException e) {
//            Log.e("arduino", e.getMessage());
//            try {
//                bluetoothSocket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(bluetoothDevice, 1);
//                bluetoothSocket.connect();
//            } catch (Exception e2) {
//                Log.e("error", "could not establish");
//            }
//        }
//
//        try{
//            outputStream = bluetoothSocket.getOutputStream();
//            inputStream = bluetoothSocket.getInputStream();
//            receiveData();
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//    }
//
//    public void receiveData() {
//        final Handler handler = new Handler();
//        readBufferPosition = 0;
//        readBuffer = new byte[1024];
//        workerThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (!Thread.currentThread().isInterrupted()) {
//                    try {
//                        int byteAvailable = inputStream.available();
//                        if (byteAvailable > 0) {
//                            byte[] bytes = new byte[byteAvailable];
//                            inputStream.read(bytes);
//                            for (int i = 0; i < byteAvailable; i++) {
//                                byte tempByte = bytes[i];
//                                if (tempByte == '\n') {
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String text = new String(encodedBytes, "UTF-8");
//                                    readBufferPosition = 0;
//
//                                    handler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Log.d("arduino", text);
//                                            addDataToFirebase(text);
//                                        }
//                                    });
//                                } else {
//                                    readBuffer[readBufferPosition++] = tempByte;
//                                }
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        workerThread.start();
//    }
//
//    private void addDataToFirebase(String data){
//        if (data.startsWith("Average")){
//            int heartRate = Integer.parseInt(data.split(" ")[1]);
//            heartRates.add(heartRate);
//            Log.d("Firebase", String.valueOf(heartRate));
//
//            uploadToFirebaseStorage(csvDataBuilder.toString());
//            csvDataBuilder = new StringBuilder();
//
//            saveHeartRateToDatabase(heartRate);
//        } else{
//            if(csvDataBuilder.length() == 0){
//                csvDataBuilder.append("Timestamp,Elapsed Time,X,Y,Z");
//            }
//            if(!data.startsWith("Timestamp")){
//                csvDataBuilder.append(data).append("\n");
//            }
//        }
//    }
//
//    private void saveHeartRateToDatabase(int heartRate){
//        databaseReference = database.getReference("meditation").child("heartRate").child(getFormattedCurrentDate());
//
//        timestamp += 1;
//        databaseReference.child(String.valueOf(timestamp)).setValue(heartRate);
//    }
//
//    private void uploadToFirebaseStorage(String csvData){
//        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//        String fileName = currentDate + ".csv";
//
//        try {
//            ByteArrayInputStream stream = new ByteArrayInputStream(csvData.getBytes("UTF-8"));
//            StorageReference fileRef = storageReference.child(fileName);
//            fileRef.putStream(stream)
//                    .addOnSuccessListener(taskSnapshot -> {
//                        Log.d("Firebase", "Upload successful");
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("Firebase", "Upload failed: " + e.getMessage());
//                    });
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }
}

