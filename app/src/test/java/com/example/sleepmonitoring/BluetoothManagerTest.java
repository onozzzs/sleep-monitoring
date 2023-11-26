//package com.example.sleepmonitoring;
//
//import android.bluetooth.BluetoothAdapter;
//import android.content.Intent;
//import android.widget.Toast;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//public class BluetoothManagerTest {
//
//    @Mock
//    private BluetoothAdapter bluetoothAdapter;
//
//    @Mock
//    private MainActivity mainActivity;
//
//    private BluetoothManager bluetoothManager;
//
//    @Before
//    public void setUp() {
//        bluetoothManager = new BluetoothManager(mainActivity, bluetoothAdapter);
//    }
//
//    @Test
//    public void testSetBluetoothWhenAdapterIsNull() {
//        // Given
//        when(bluetoothAdapter.isEnabled()).thenReturn(true);
//
//        // When
//        bluetoothManager.setBluetooth();
//
//        // Then
//        verify(mainActivity).showToast("블루투스 미지원 기기입니다.");
//    }
//
//    @Test
//    public void testSetBluetoothWhenAdapterIsEnabled() {
//        // Given
//        when(bluetoothAdapter.isEnabled()).thenReturn(true);
//
//        // When
//        bluetoothManager.setBluetooth();
//
//        // Then
//        verify(mainActivity).selectBluetoothDevice();
//    }
//
//    @Test
//    public void testSetBluetoothWhenAdapterIsNotEnabled() {
//        // Given
//        when(bluetoothAdapter.isEnabled()).thenReturn(false);
//        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        when(mainActivity.getEnableBluetoothIntent()).thenReturn(enableBluetoothIntent);
//
//        // When
//        bluetoothManager.setBluetooth();
//
//        // Then
//        verify(mainActivity).startActivityForResult(enableBluetoothIntent, MainActivity.REQUEST_ENABLE_BT);
//    }
//}
