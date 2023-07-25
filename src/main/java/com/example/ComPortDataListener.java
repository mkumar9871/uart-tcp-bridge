package com.example;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.OutputStream;

public class ComPortDataListener implements SerialPortDataListener {
    private final OutputStream outputStream;

    public ComPortDataListener(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
            byte[] bytes = serialPortEvent.getReceivedData();
            outputStream.write(bytes);
            System.out.println("R: " + new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
