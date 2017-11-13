package com.hcoder.batu.hcoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// MAIN ACTIVITY
public class MainActivity extends AppCompatActivity {

    public static TCPClient mTcpClient;

    public TextView recepient_view,
                    filename_view,
                    status_view,
                    username_view ;

    public Button   login_button,
                    name_send_button,
                    accept_button,
                    reject_button,
                    file_button,
                    send_button,
                    recepient_button;

    public EditText username_editText,
                    recepient_editText;

    public Boolean  is_logged_in = false,
                    get_users = false,
                    packet_accept,
                    packet_reject,
                    have_file=false;

    public static String    current_user_name,
                            from,
                            file_content,
                            recepient_name,
                            sender,
                            receiver,
                            received_message ="",
                            login_username,
                            filename,
                            file_txt;

    public String   packet1,
                    packet2,
                    packet2_error,
                    packet3,
                    packet5,
                    packet7,
                    packet8,
                    packet9,
                    packet10,
                    packet_file,
                    packet11,
                    packet12 ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        new connectTask().execute("");

        login_button = (Button) findViewById(R.id.login_button);
        file_button = (Button)findViewById(R.id.file_button);
        recepient_button = (Button)findViewById(R.id.select_user_button);
        name_send_button = (Button)findViewById(R.id.recepient_name_send);
        accept_button = (Button)findViewById(R.id.file_receive_accept);
        reject_button = (Button)findViewById(R.id.file_receive_reject);
        send_button = (Button)findViewById(R.id.sendto_user_button);

        username_view = (TextView) findViewById(R.id.current_username_view);
        filename_view = (TextView) findViewById(R.id.current_filename_view);
        recepient_view = (TextView)findViewById(R.id.current_recepient_view);
        status_view = (TextView) findViewById(R.id.received_text);

        username_editText = (EditText) findViewById(R.id.username_EditText);
        recepient_editText = (EditText) findViewById(R.id.recepient_name_edit);

        packet9 = "{'request': 'send_file', 'request_args': '" + current_user_name + "', 'file': ''}";
        packet11 = "{'request': 'get_file', 'request_args': 'user1',  'status': 'done'}";
        packet12 = "{'request': 'get_file', 'request_args': '','from':'"+current_user_name+"'  'status': 'done'}";

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_user_name = username_editText.getText().toString();
                packet1 = "{'request': 'login', 'request_args': '" + current_user_name + "'}";
                packet2 = "{'status': 'ok', 'request': 'login_result', 'from': 'p2p_server', 'request_args': ''}";
                packet2_error = "{'status': 'nok', 'request': 'login_result', 'from': 'p2p_server', 'request_args': ''}";
                mTcpClient.sendMessage(packet1);
                Log.i("HCODER","SENT MESSAGE: " + packet1);
            }
        });

        recepient_editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                recepient_name = recepient_editText.getText().toString();
                name_send_button.setRawInputType(View.VISIBLE);
            }
        });

        name_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                packet5 = "{'request': 'send_packet', 'request_args': '" + recepient_name +"'}";
                mTcpClient.sendMessage(packet5);
                Log.i("HCODER","SENT NAME: " + packet5);
                String status = "Recepient "+ recepient_name +" is selected ";
                status_view.setText(status);
            }
        });

        accept_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                packet7 = "{'request': 'get_packet', 'from': '" + current_user_name + "', 'to':'" + recepient_name +"', 'request_args': '', status': 'ok'}";
                mTcpClient.sendMessage(packet7);
                packet_accept=true;
                packet_reject=false;
                accept_button.setVisibility(View.INVISIBLE);
                reject_button.setVisibility(View.INVISIBLE);
            }
        });

        reject_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                packet8 = "{'request': 'get_packet', 'from': '" + current_user_name + "', 'to':'" + recepient_name +"', 'request_args': '', status': 'nok'}";
                mTcpClient.sendMessage(packet8);
                packet_accept=false;
                packet_reject=true;
                accept_button.setVisibility(View.INVISIBLE);
                reject_button.setVisibility(View.INVISIBLE);
            }
        });

        recepient_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_users=true;
                packet3 = "{'request': 'get_users', 'request_args': ''}" ;
                mTcpClient.sendMessage(packet3);
                Log.i("HCODER","SENT MESSAGE: " + packet3);
            }
        });

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                packet_file = "{'request': 'send_file', 'request_args': '" + recepient_name + "', 'file': '"+ file_txt +"'}";
                mTcpClient.sendMessage(packet_file);
                Log.i("HCODER","SENT PACKET: " + packet_file);
                String status = "Packet ("+ file_txt + ") has been sent";
                status_view.setText(status);
            }
        });

        file_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showFileChooser();
                    if(filename != null) {
                        String status = filename + " file is selected";
                        status_view.setText(status);
                    }
                }
        });
    }

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    filename = getFileName(uri);
                    filename_view.setText(filename);
                    file_txt = readTextFile(uri);
                    File file = new File(uri.getPath());
                    Log.d("HCODER", "File Uri: " + uri.toString());
                    Log.d("HCODER", "File content: " + file_content);
                    try {
                        String path = FileUtils.getPath(this,uri);
                        //Log.d("HCODER", "File Path: " + path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String readTextFile(Uri uri){
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public class connectTask extends AsyncTask<String, String, TCPClient> {
        @Override
        protected TCPClient doInBackground(String... message) {
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    publishProgress(message);
                }
            });
            mTcpClient.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            received_message = values[0];
            Log.i("HCODER","RECEIVED MESSAGE: " + received_message);
            String status;
            //packet2
            if (received_message.equals(packet2)) {
                username_view.setText(current_user_name);
                status = "Successfully logged in to the server";
                status_view.setText(status);

                login_button.setVisibility(View.INVISIBLE);
                username_editText.setVisibility(View.INVISIBLE);

                LinearLayout info = (LinearLayout) findViewById(R.id.info);
                LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
                info.setVisibility(View.VISIBLE);
                buttons.setVisibility(View.VISIBLE);

                Log.i("HCODER","PACKET2: " + packet2);
                Log.i("HCODER","USER: " + current_user_name);
            }
            //packet4
            if (get_users && received_message.contains("users_result")) {
                String usernames = usersParse(received_message);
                recepient_view.setText(usernames);
                status = "Successfully get users";
                status_view.setText(status);
                recepient_editText.setVisibility(View.VISIBLE);
                name_send_button.setVisibility(View.VISIBLE);
            }
            //packet6
            if(received_message.contains("ask_send")) {
                status = "Do you accept incoming file from " + recepient_name + " ?";
                status_view.setText(status);
                accept_button.setVisibility(View.VISIBLE);
                reject_button.setVisibility(View.VISIBLE);
            }
            //packet9
            if(received_message.equals(packet9)) {
                status = "You can send the file to "+ receiver ;
                status_view.setText(status);
            }
            //packet10
            if(received_message.contains("send_file")) {
                file_content = fileParse(received_message);
                from = fromParse(received_message);

                String DIRECTORY_PATH = Environment.getExternalStorageDirectory().toString();


                try {
                    PrintStream ps = new PrintStream(new FileOutputStream (new File("/storage/emulated/0/Download/filename.txt")));
                    ps.print(file_content);
                } catch (IOException io) {io.printStackTrace();}




                status = "File's content :" + file_content;
                status_view.setText(status);
                packet11 = "{'request': 'get_file', 'request_args': '', 'from': '"+from+"',  'status': 'done'}";
                mTcpClient.sendMessage(packet11);
            }
            //packet12
            if(received_message.equals(packet12)) {
                status = "DONE: File is received by : " + recepient_name;
                status_view.setText(status);
                receiver="";
                sender="";
            }
        }
    }

    public String usersParse(String s) {
        String users="";
        s = s.substring(s.indexOf("[")+1);
        s = s.substring(0, s.indexOf("]"));
        String[] tokens = s.split(",");
        for(int i=0; i< tokens.length; i++) {
            users += i+1 + ". " + tokens[i] + "\n";
        }
        return users;
    }

    public String fileParse(String f) {
        String result = "";
        String textFrom="'request_args': '", textTo="'}";
        result = f.substring(f.indexOf(textFrom) + textFrom.length(), f.length());
        result = result.substring(0, result.indexOf(textTo));
        return result;
    }

    public String fromParse(String f) {
        String result = "";
        String textFrom="'from': ", textTo=",";
        result = f.substring(f.indexOf(textFrom) + textFrom.length(), f.length());
        result = result.substring(0, result.indexOf(textTo));
        return result;
    }

}