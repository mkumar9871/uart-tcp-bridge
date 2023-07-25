package com.example;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class UartTcpBridgeApp {
    private static final int DEFAULT_TCP_PORT = 8080;
    private static final String DEFAULT_UART_PORT = "COM10";
    private static final int DEFAULT_UART_BAUD = 115200;

    public static void main(String[] args) {
        int i = 0;
        int tcpPort = args.length > i ? Integer.parseInt(args[i++]):DEFAULT_TCP_PORT;
        String uartPort = args.length > i ? args[i++]:DEFAULT_UART_PORT;
        int uartBaud = args.length > i ? Integer.parseInt(args[i++]):DEFAULT_UART_BAUD;

        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            while (true) {
                System.out.printf("Listening on %s port to connect with [%s,%s]", tcpPort, uartPort, uartBaud);
                Socket clientSocket = serverSocket.accept();
                SerialPort comm10 = SerialPort.getCommPort(uartPort);
                try {
                    InputStream inputStream = clientSocket.getInputStream();
                    OutputStream outputStream = clientSocket.getOutputStream();

                    // Open com port
                    comm10.setBaudRate(uartBaud);
                    comm10.setNumDataBits(8);
                    comm10.setNumStopBits(1);
                    comm10.setParity(SerialPort.NO_PARITY);
                    comm10.addDataListener(new ComPortDataListener(outputStream));
                    comm10.openPort();

                    System.out.printf("Client connected with [%s]\n", comm10.getSystemPortName());

                    Thread writerThread = new Thread(() -> readAndWrite(inputStream, comm10.getOutputStream()));
                    writerThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Wait until connection is closed
                while (!clientSocket.isClosed()) {
                    ;
                }

                // close serial and tcp
                comm10.closePort();
                clientSocket.close();
                System.out.println("Client disconnected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void readAndWrite(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            try {
                while ((bytesRead = inputStream.read(buffer))!=-1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                    System.out.println(new String(buffer, 0, bytesRead));
                }
            } finally {
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
