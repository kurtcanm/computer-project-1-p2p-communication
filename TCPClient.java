package com.hcoder.batu.hcoder;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


public class TCPClient {
    public static final String SERVER_IP = "160.75.6.107";
    public static final int SERVER_PORT = 12000;

    private String mServerMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    int BUFFER_SIZE=1000;

    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    public void stopClient() {
        Log.i("Debug", "stopClient");

        mRun = false;
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.e("TCP Client", "C: Connecting...");
            Socket socket = new Socket(serverAddr, SERVER_PORT);

            int charsRead = 0;
            char[] buffer = new char[BUFFER_SIZE];

            mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (mRun) {
                charsRead = mBufferIn.read(buffer);
                mServerMessage = new String(buffer).substring(0, charsRead);
                mMessageListener.messageReceived(mServerMessage);
            }
        } catch (Exception e) {
            Log.e("HCODER", "C: Error", e);
        }
    }
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}