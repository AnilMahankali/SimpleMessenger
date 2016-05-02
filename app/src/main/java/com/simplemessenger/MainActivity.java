package com.simplemessenger;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.jar.Manifest;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends Activity
{
    static final String TAG = MainActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "9999";
    static final String REMOTE_PORT1 = "8888";
    static final int SERVER_PORT = 8888;


    EditText editText;
    TextView textView;

    /** Called when the Activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Allow this Activity to use a layout file that defines what UI elements to use.
         * Please take a look at res/layout/main.xml to see how the UI elements are defined.
         *
         * R is an automatically generated class that contains pointers to statically declared
         * "resources" such as UI elements and strings. For example, R.layout.main refers to the
         * entire UI screen declared in res/layout/main.xml file. You can find other examples of R
         * class variables below.
         */
        setContentView(R.layout.activity_main);
        System.out.println("In onCreateMethod*****");



        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            // return;
        }

        editText = (EditText)findViewById(R.id.edit_message);

        Button onClick = (Button)findViewById(R.id.button);
        onClick.setOnClickListener(sendMessage);

        textView = (TextView)findViewById(R.id.local_text_display);
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            Socket socket = null;
            String msg;

            // do {
            while (true)
                try {
                    // Accept Connection and Initialize Input Stream
                    socket = serverSocket.accept();
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                    msg = (String) input.readObject();
                    System.out.println("Server Message Received::::::" + msg);
                    publishProgress(msg);
                } catch (IOException e) {
                    Log.e(TAG, "ServerTask IOException");
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "ServerTask ClassNotFoundException");
                }
            //} while(!socket.isInputShutdown());
        }
        //return null;


        protected void onProgressUpdate(String... strings) {
            System.out.println("String***");
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            remoteTextView.append(strReceived + "\t\n");
            remoteTextView.setGravity(Gravity.BOTTOM);

            TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            localTextView.append("\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }

    }




    public int getPort(){
        return SERVER_PORT;
    }
    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener sendMessage = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked
            // Yes we will handle click here but which button clicked??? We don't know
            String message = editText.getText().toString()+"\n";
            editText.setText("");
            System.out.println("MESSAGE ON CLICK***" + message);
            textView.append("\t" + message);
            textView.setGravity(Gravity.BOTTOM);



            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, REMOTE_PORT0);
        }
    };

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            Socket socket = null;
            System.out.println("In Client Task doInBackground method****"+msgs);
            try {
                String remotePort = REMOTE_PORT0;
                if (msgs[1].equals(REMOTE_PORT0))
                    remotePort = REMOTE_PORT1;

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 15}),
                        Integer.parseInt(remotePort));

                String msgToSend = msgs[0];

                /** TODO: Fill in your client code that sends out a message.*/


                //Initialize Output Stream
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                output.writeObject(msgToSend);
                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException",e);
            } catch (Exception e){
                Log.e(TAG,"Exception",e);
            }

            return null;
        }
    }
}
