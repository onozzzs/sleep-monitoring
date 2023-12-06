package com.example.sleepmonitoring;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class HomeFragment extends Fragment {
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
    private int timestamp = 0;
    public List<Integer> scores = new ArrayList<>();
    public List<Integer> heartRates = new ArrayList<>();

    private Button startButton, stopButton, resetButton;

    private Chronometer chronometer;
    private boolean isRunning = false;
    private long pauseOffset = 0;
    private TextView textDate;
    private TextView textTime;
    private StopwatchViewModel stopwatchViewModel;
    private int min = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        database = FirebaseDatabase.getInstance();

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mainmenu_home, container, false);

        setBluetooth();

        chronometer = view.findViewById(R.id.chronometer);
        stopwatchViewModel = new ViewModelProvider(requireActivity()).get(StopwatchViewModel.class);
        startButton = view.findViewById(R.id.startButton);
        stopButton = view.findViewById(R.id.stopButton);
        resetButton = view.findViewById(R.id.resetButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToArduino("S");
                startChronometer();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToArduino("T");
                stopChronometer();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetChronometer();
            }
        });

        chronometer.setOnChronometerTickListener(chronometer -> {
            // 가동 시간이 업데이트될 때 ViewModel에 저장
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            stopwatchViewModel.setElapsedTimeMillis(elapsedMillis);
        });

        textDate = view.findViewById(R.id.textDate);
        textTime = view.findViewById(R.id.textTime);

        Handler handler = new Handler();
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateDateTime();
                handler.postDelayed(this, 1000); // 1초마다 업데이트
            }
        };

        updateDateTime(); // 처음 한 번은 수동으로 업데이트
        handler.post(updateTimeRunnable); // Runnable을 처음 한 번 post

        return view;
    }

    private void sendDataToArduino(String message) {
        byte[] bytes = message.getBytes();
        try {
            outputStream.write(bytes);
            Log.d("arduino", "send data to arduino");
        } catch (IOException e) {
            Log.d("arduino", "failed to send data");
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void setBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
        if (bluetoothAdapter == null) {
//            Toast.makeText(getApplicationContext(), "블루투스 미지원 기기입니다.", Toast.LENGTH_LONG).show();
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            selectBluetoothDevice();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
    }

    @SuppressLint("MissingPermission")
    public void selectBluetoothDevice() {
        devices = bluetoothAdapter.getBondedDevices();
        pariedDeviceCount = devices.size();
        if (pariedDeviceCount == 0) {
//            Toast.makeText(getApplicationContext(), "먼저 Bluetooth 설정에 들어가 페어링 해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        createDeviceDialog();
    }

    @SuppressLint("MissingPermission")
    private void createDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
        List<String> list = new ArrayList<>();
        for (BluetoothDevice bluetoothDevice : devices) {
            list.add(bluetoothDevice.getName());
        }
        list.add("취소");

        final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
        list.toArray(new CharSequence[list.size()]);

        builder.setItems(charSequences, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectDevice(charSequences[which].toString());
            }
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @SuppressLint("MissingPermission")
    private void connectDevice(String deviceName) {
        for (BluetoothDevice tempDevice : devices) {
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        Toast.makeText(getContext(), bluetoothDevice + "연결 완료", Toast.LENGTH_SHORT).show();
        connect_status = true;

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            Log.e("arduino", e.getMessage());
            try {
                bluetoothSocket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(bluetoothDevice, 1);
                bluetoothSocket.connect();
            } catch (Exception e2) {
                Log.e("error", "could not establish");
            }
        }

        try{
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            receiveData();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void receiveData() {
        csvDataBuilder = new StringBuilder();
        final Handler handler = new Handler();
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        int byteAvailable = inputStream.available();
                        if (byteAvailable > 0) {
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            for (int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                if (tempByte == '\n') {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String text = new String(encodedBytes, "UTF-8");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("arduino", text);
                                            addDataToFirebase(text);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        workerThread.start();
    }

    private void addDataToFirebase(String data){
        if (data.contains("AverageHeartRate")) {
            min++;
            String[] parts = data.split(" ");
            if(parts[1] instanceof String){
                Integer heartRate = Integer.parseInt(parts[1].trim());
//                heartRates.add(heartRate);
                saveHeartRateToDatabase(heartRate);
            }
        } else{
            if(csvDataBuilder.length() == 0){
                Log.d("Firebase", data);
                csvDataBuilder.append("Timestamp,Elapsed Time,X,Y,Z").append("\n");
            }
            if(!data.startsWith("Timestamp")){
                csvDataBuilder.append(data).append("\n");
            }
        }
        if(min == 3){
            uploadToFirebaseStorage(csvDataBuilder.toString());
            csvDataBuilder = new StringBuilder();
        }
    }

    private void saveHeartRateToDatabase(Integer heartRate){
        Log.d("Firebase", "saveheartratetoDatabase");
        Log.d("Firebase", getFormattedCurrentDate());

        databaseReference = database.getReference("meditation").child("heartRate").child(getFormattedCurrentDate());

        timestamp += 1;
        databaseReference.child(String.valueOf(timestamp)).setValue(heartRate);
    }

    private void uploadToFirebaseStorage(String csvData){
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String fileName = currentDate + ".csv";

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(csvData.getBytes("UTF-8"));
            StorageReference fileRef = storageReference.child(fileName);
            fileRef.putStream(stream)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d("Firebase", "Upload successful");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firebase", "Upload failed: " + e.getMessage());
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getFormattedCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    private long startTimeMillis = 0;

    private void startChronometer() {
        if (!isRunning) {
            startTimeMillis = SystemClock.elapsedRealtime() - pauseOffset;
            chronometer.setBase(startTimeMillis);
            chronometer.start();
            isRunning = true;
        }
    }

    private void stopChronometer() {
        if (isRunning) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            isRunning = false;
            updateElapsedTime();
        }
    }

    private void updateElapsedTime() {
        // 경과 시간 계산
        long elapsedMillis = SystemClock.elapsedRealtime() - startTimeMillis;

        // 가동 시간이 업데이트될 때 ViewModel에 저장
        stopwatchViewModel.setElapsedTimeMillis(elapsedMillis);
    }

    public void setStopwatchViewModel(StopwatchViewModel viewModel) {
        this.stopwatchViewModel = viewModel;
        updateElapsedTime();
    }

    private void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        if (isRunning) {
            chronometer.start();
        }
    }

    private void updateDateTime() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd (EEE)", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = timeFormat.format(currentDate);

        textDate.setText(formattedDate);
        textTime.setText(formattedTime);
    }
}

