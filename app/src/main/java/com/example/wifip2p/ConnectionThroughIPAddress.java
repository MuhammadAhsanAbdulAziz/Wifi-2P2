package com.example.wifip2p;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionThroughIPAddress extends AppCompatActivity {
    Button startServerBtn, connectBtn;
    ImageButton btnSend;
    TextView connectionStatus, messageTextView;
    EditText typeMsg, ipInput;
    Socket socket;
    Server server;
    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_through_ipaddress);

        initialWork();
        execListener();
    }

    private void execListener() {
        startServerBtn.setOnClickListener(v -> {
            server = new Server();
            server.start();
            connectionStatus.setText("Server Started");
        });

        connectBtn.setOnClickListener(v -> {
            String ipAddress = ipInput.getText().toString();
            client = new Client(ipAddress);
            client.start();
            connectionStatus.setText("Client Started");
        });

        btnSend.setOnClickListener(v -> {
            ExecutorService service = Executors.newSingleThreadExecutor();
            String msg = typeMsg.getText().toString();
            service.execute(() -> {
                if (msg != null) {
                    if (server != null && server.isConnected()) {
                        server.write(msg.getBytes());
                    } else if (client != null && client.isConnected()) {
                        client.write(msg.getBytes());
                    }
                }
            });
        });
    }

    private void initialWork() {
        startServerBtn = findViewById(R.id.startServerBtn);
        connectBtn = findViewById(R.id.connectBtn);
        btnSend = findViewById(R.id.sendBtn);
        connectionStatus = findViewById(R.id.connection_status);
        messageTextView = findViewById(R.id.message);
        typeMsg = findViewById(R.id.inputText);
        ipInput = findViewById(R.id.ipInput);
    }

    public class Server extends Thread {
        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean connected = false;

        public void write(byte[] bytes) {
            if (outputStream != null) {
                try {
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean isConnected() {
            return connected;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            service.execute(() -> {
                byte[] buffer = new byte[1024];
                int bytes;
                while (socket != null) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            int finalBytes = bytes;
                            handler.post(() -> {
                                String tempMsg = new String(buffer, 0, finalBytes);
                                messageTextView.setText(tempMsg);
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public class Client extends Thread {
        String hostAdd;
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean connected = false;

        public Client(String address) {
            hostAdd = address;
            socket = new Socket();
        }

        public void write(byte[] bytes) {
            if (outputStream != null) {
                try {
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean isConnected() {
            return connected;
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            service.execute(() -> {
                byte[] buffer = new byte[1024];
                int bytes;
                while (socket != null) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            int finalBytes = bytes;
                            handler.post(() -> {
                                String tempMsg = new String(buffer, 0, finalBytes);
                                messageTextView.setText(tempMsg);
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
