package com.example.acer.myapplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

/***
 * ServerTask is an AsyncTask that should handle incoming messages. It is created by
 * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
 * 
 * Please make sure you understand how AsyncTask works by reading
 * http://developer.android.com/reference/android/os/AsyncTask.html
 * 
 * @author stevko
 *
 */
public class ServerTask extends AsyncTask<ServerSocket, String, Void> {
	
	static final String TAG = ServerTask.class.getSimpleName();
	
    @Override
    protected Void doInBackground(ServerSocket... sockets) {
		System.out.println("In doInBackground method ServerTask");

        ServerSocket serverSocket = sockets[0];
        
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
            	ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			
				msg = (String) input.readObject();
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
    	System.out.println("In progressUpdate Server*****");
        /*
         * The following code displays what is received in doInBackground().
         */
        String strReceived = strings[0].trim();
        TextView remoteTextView = (TextView)new MainActivity().findViewById(R.id.remote_text_display);
        remoteTextView.append(strReceived + "\t\n");
        TextView localTextView = (TextView) new MainActivity().findViewById(R.id.local_text_display);
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
            outputStream = new MainActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "File write failed");
        }

        return;
    }
}