package com.simplemessenger;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.jar.Manifest;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends Activity
{
    static final String TAG = MainActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final int SERVER_PORT = 10000;

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


        // Assume thisActivity is the current activity
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_SMS);


        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        //TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //System.out.println("tel**"+tel);
        //System.out.println("tel**"+tel.getLine1Number());
        //String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        //final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        final String myPort = "1067";

        System.out.println("my Port*****" + myPort);
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
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        } catch (Exception e){
            System.out.println("IN ActivityMain ERROR******"+e);
        }

        /*
         * Retrieve a pointer to the input box (EditText) defined in the layout
         * XML file (res/layout/main.xml).
         *
         * This is another example of R class variables. R.id.edit_text refers to the EditText UI
         * element declared in res/layout/main.xml. The id of "edit_text" is given in that file by
         * the use of "android:id="@+id/edit_text""
         */
        final EditText editText = (EditText) findViewById(R.id.edit_text);
        System.out.println("EDIT_TEXT");

        /*
         * Register an OnKeyListener for the input box. OnKeyListener is an event handler that
         * processes each key event. The purpose of the following code is to detect an enter key
         * press event, and create a client thread so that the client thread can send the string
         * in the input box over the network.
         */
        try {
            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    //System.out.println("In setOnKeyListener----3*******" + keyCode);
                    //System.out.println("In setOnKeyListener----4*******" + KeyEvent.KEYCODE_ENTER);
                    if ((event.getAction() != KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        System.out.println("In setOnKeyListener----1--******" + event.getAction());
                        System.out.println("In setOnKeyListener----2*******" + KeyEvent.ACTION_DOWN);
                        System.out.print("Key is pressed");
                    /*
                     * If the key is pressed (i.e., KeyEvent.ACTION_DOWN) and it is an enter key
                     * (i.e., KeyEvent.KEYCODE_ENTER), then we display the string. Then we create
                     * an AsyncTask that sends the string to the remote AVD.
                     */

                        String msg = editText.getText().toString() + "\n";
                                System.out.println("msg Typed****"+msg);
                        editText.setText(""); // This is one way to reset the input box.
                        TextView localTextView = (TextView) findViewById(R.id.local_text_display);
                                System.out.println("TEXT_VIEW***"+msg);
                        localTextView.append("\t" + msg); // This is one way to display a string.
                        //localTextView.setText(msg);

                        TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
                        remoteTextView.append("\n");
                        System.out.println("In onKeyListner");

                    /*
                     * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                     * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                     * the difference, please take a look at
                     * http://developer.android.com/reference/android/os/AsyncTask.html
                     */
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println("EVENT ONKEY" + e);
        }

    }

                /***
                 * ServerTask is an AsyncTask that should handle incoming messages. It is created by
                 * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
                 *
                 * Please make sure you understand how AsyncTask works by reading
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 *
                 */
        private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            System.out.print("In ServerTask doInBackground*****:"+serverSocket);
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            Socket socket = null;
            String msg;

            do {
                try {
                    // Accept Connection and Initialize Input Stream
                    socket = serverSocket.accept();
                    System.out.println("Server is listening*****");
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                    msg = (String) input.readObject();
                    System.out.println("In server msg Received****"+msg);
                    // Call this to update your progress
                    publishProgress(msg);
                } catch (IOException e) {
                    Log.e(TAG, "ServerTask IOException");
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "ServerTask ClassNotFoundException");
                }
            } while(!socket.isInputShutdown());

            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            remoteTextView.append(strReceived + "\t\n");
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

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            System.out.println("In ClientTask doInBackground method called");
            try {
                String remotePort = REMOTE_PORT0;
                if (msgs[1].equals(REMOTE_PORT0))
                    remotePort = REMOTE_PORT1;

                String msgToSend = msgs[0];
                System.out.println("In client doBackground ***"+msgToSend);

                //Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}),
                        Integer.parseInt(remotePort));



                /*
                 * TODO: Fill in your client code that sends out a message.
                 */

                // Initialize Output Stream
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                output.writeObject(msgToSend);
                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException",e);
            } catch(Exception e){
                System.out.println("ERROR in client***" + e);
            }

            return null;
        }
    }
}
