package com.example.udptest;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private int port = 2222;
    TextView receivedMessage;
    DatagramSocket socket;

    private interface Callback {
        void onError(String error);

        void onMessage(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receivedMessage = findViewById(R.id.message);
        new EstablishConnectionTask(callback).start();
        new PacketReceiveRunnable(callback).start();
    }

    private Callback callback = new Callback() {
        @Override
        public void onError(final String error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(error);
                }
            });
        }

        @Override
        public void onMessage(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    receivedMessage.setText(message);
                }
            });
        }
    };

    class EstablishConnectionTask extends Thread {

        private Callback listener;

        EstablishConnectionTask(Callback callback) {
            this.listener = callback;
        }

        @Override
        public void run() {
            try {
                socket = new DatagramSocket(port);
                socket.setReuseAddress(true);
                socket.setBroadcast(true);
                InetAddress serverAddress = InetAddress.getByName("192.168.178.94");
                byte[] buf = ("Android").getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, port);
                socket.send(packet);
            } catch (IOException e) {
                listener.onError("Establish connection error: " + e.getMessage());
            }
        }
    }

    class PacketReceiveRunnable extends Thread {

        private Callback listener;

        PacketReceiveRunnable(Callback callback) {
            this.listener = callback;
        }

        @Override
        public void run() {
//                socket = new DatagramSocket();
            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                try {
                    socket.receive(packet);
                    String message = new String(buffer, 0, packet.getLength());
                    packet.setLength(buffer.length);
                    listener.onMessage("Receive packet: " + message);
                } catch (IOException exception) {
                    listener.onError("Receive packet: " + exception.getMessage());
                }
            }
        }
    }
}


