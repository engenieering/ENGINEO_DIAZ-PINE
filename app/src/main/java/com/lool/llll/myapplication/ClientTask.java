package com.lool.llll.myapplication;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by llll on 15/07/2016.
 */
public class ClientTask extends AsyncTask<String,String,String> implements OnListener  {
    MapsActivity mapsActivity=new MapsActivity();

    String dstAddress;
    int dstPort;
    PrintWriter out1;
    ClientTask(String addr, int port){
        dstAddress = addr;
        dstPort = port;
    }
    @Override
    protected void onProgressUpdate(String... values){
        super.onProgressUpdate(values);
    }
    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub

        try {

             mapsActivity.socket= new Socket(dstAddress, dstPort);
            out1 = new PrintWriter(mapsActivity.socket.getOutputStream(), true);
            //out1.print("Hello server!");
            out1.flush();

            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    mapsActivity.socket.getInputStream()));
            do {
                try {
                    if (!in1.ready()) {
                        if (message != null) {
                            MainActivity.handler.obtainMessage(0, 0, -1,
                                    "Server: " + message).sendToTarget();
                            message = "";
                        }
                    }
                    int num = in1.read();
                    message += Character.toString((char) num);
                } catch (Exception classNot) {
                }

            } while (!message.equals("bye"));

            try {
                sendMessage("bye");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {
        try {
            if ( mapsActivity.socket.isClosed()) {
                flag = false;
            }
        } catch (Exception e) {

        }

        super.onPostExecute(result);
    }

    @Override
    public void listener(String text) {
        // TODO Auto-generated method stub
        sendMessage(text);
    }
    void sendMessage(String msg) {
        try {
            out1.print(msg);
            out1.flush();
            if (!msg.equals("bye"))
                MainActivity.handler.obtainMessage(0, 0, -1, "Me: " + msg)
                        .sendToTarget();
            else
                MainActivity.handler.obtainMessage(0, 0, -1,
                        "Disconnect!").sendToTarget();
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }

}
