package com.example.acer.myapplication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

/***
 * ClientTask is an AsyncTask that should send a string over the network.
 * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
 * an enter key press event.
 * 
 * @author stevko
 *
 */
public class ClientTask extends AsyncTask<String, Void, Void> {
	
	static final String TAG = MainActivity.class.getSimpleName();
	static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";

	@Override
    protected Void doInBackground(String... msgs) {
		System.out.println("In doInBackground method ClientTask");
        try {
            String remotePort = REMOTE_PORT0;
            if (msgs[1].equals(REMOTE_PORT0))
                remotePort = REMOTE_PORT1;

            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(remotePort));
            
            String msgToSend = msgs[0];
            /*
             * TODO: Fill in your client code that sends out a message.
             */
            System.out.println("**CLIENT***"+msgToSend);
            // Initialize Output Stream
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            
    		output.writeObject(msgToSend);
			output.flush();
			socket.close();
            
        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "ClientTask socket IOException");
        }
        
        return null;
    }
}