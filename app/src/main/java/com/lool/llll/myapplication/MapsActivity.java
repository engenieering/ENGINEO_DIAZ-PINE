package com.lool.llll.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener  {
    LatLng ss = new LatLng(-34, 101);
    LatLng sss = new LatLng(-34, 102);
    LatLng ssss = new LatLng(-34, 103);
    List<LatLng> listLatLng ;
    static final int SocketServerPORT = 8080;

    TextView infoIp, infoPort, chatMsg;
    Spinner spUsers;
    ArrayAdapter<ChatClient> spUsersAdapter;
    Button btnSentTo;
    String msgLog = "";
    List<ChatClient> userList;
    ServerSocket serverSocket;

    public GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    SupportMapFragment mapFragment ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // obtain th position

        listLatLng = new ArrayList<LatLng>();
        addCustomMarker(ss);
        addCustomMarker(sss);
        addCustomMarker(ssss);
        listLatLng.add(ss);
        listLatLng.add(sss);
        listLatLng.add(ssss);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        // TODO: verification of permisstion
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null){
          onLocationChanged(location);

        }else {
            Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        infoIp = (TextView) findViewById(R.id.infoip);
        infoPort = (TextView) findViewById(R.id.infoport);
        chatMsg = (TextView) findViewById(R.id.chatmsg);

        spUsers = (Spinner) findViewById(R.id.spusers);
        userList = new ArrayList<ChatClient>();
        spUsersAdapter = new ArrayAdapter<ChatClient>(
                MapsActivity.this, android.R.layout.simple_spinner_item, userList);
        spUsersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUsers.setAdapter(spUsersAdapter);

        btnSentTo = (Button)findViewById(R.id.sentto);
        btnSentTo.setOnClickListener(btnSentToOnClickListener);

        infoIp.setText(getIpAddress());



        ChatServerThread chatServerThread = new ChatServerThread();
        chatServerThread.start();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //type de map : hybrid normal ...

        // Add a marker in Sydney and move the camera
       // LatLng sydney = new LatLng(-34, 151);

        // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        //  mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //
      //  drawPolyLineOnMap(listLatLng);
        refreshPosition();
      //  addCustomMarker(ss);
      //  addCustomMarker(sss);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
/*
        mMap.clear();
        mMap.addMarker(new  MarkerOptions().position(new LatLng(lat,lng)).title("location"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), 15)); // zoom
*/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    View.OnClickListener btnSentToOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ChatClient client = (ChatClient)spUsers.getSelectedItem();
            if(client != null){
                String dummyMsg = "Dummy message00000000 from server.\n";
                client.chatThread.sendMsg(dummyMsg);
                msgLog += "- Dummy message to " + client.name + "\n";
                chatMsg.setText(msgLog);

            }else{
                Toast.makeText(MapsActivity.this, "No user connected", Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class ChatServerThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;



            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MapsActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        infoPort.setText("I'm waiting here: " + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    socket = serverSocket.accept();
                    ChatClient client = new ChatClient();
                    userList.add(client);
                    ConnectThread connectThread = new ConnectThread(client, socket);
                    connectThread.start();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spUsersAdapter.notifyDataSetChanged();
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    private class ConnectThread extends Thread {

        Socket socket;
        ChatClient connectClient;
        String msgToSend = "";

        ConnectThread(ChatClient client, Socket socket){
            connectClient = client;
            this.socket= socket;
            client.socket = socket;
            client.chatThread = this;
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                String n = dataInputStream.readUTF();

                connectClient.name = n;

                msgLog += connectClient.name + " connected@" +
                        connectClient.socket.getInetAddress() +
                        ":" + connectClient.socket.getPort() + "\n";
                MapsActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        chatMsg.setText(msgLog);
                    }
                });

                dataOutputStream.writeUTF("Welcome " + n + "\n");
                dataOutputStream.flush();

                broadcastMsg(n + " join our chat.\n");

                while (true) {
                    if (dataInputStream.available() > 0) {
                        String newMsg = dataInputStream.readUTF();

                        // newMsg...................................................................
                        //..........................................................................

                        Pattern pLat=Pattern.compile("lat(.*?)lng");

                        Matcher mLat=pLat.matcher(newMsg);

                        while (mLat.find()){
                           // // TODO: 28/07/2016

                            String[] strLATLNG = mLat.group(1).split("l");
                            LatLng pReceived =  new LatLng( Double.parseDouble(strLATLNG[0]),Double.parseDouble(strLATLNG[1]));
                            Log.i("infoo","latll:"+ pReceived.toString());
                            connectClient.latLng=pReceived ;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //
                                   // drawPolyLineOnMap(listLatLng);
                                   refreshPosition();
                                }
                            });
                           // mapFragment.getMapAsync(MapsActivity.this);
                           // MapsActivity.this.mapFragment.getMapAsync(MapsActivity.this);
                           //addCustomMarker(ss);
                            Log.i("infoo","calllllllllllllllll");
                        //    mapFragment.getMapAsync(MapsActivity);

                            //Double.parseDouble(aString);


                       /*    mMap.clear();
                            LatLng sydney = new LatLng(-34, 151);
                            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                      */
                       /*   LatLng pReceived =  new LatLng( Double.parseDouble(mLat.group(1)),Double.parseDouble(mLng.group(1)));
                            mMap.addMarker(new  MarkerOptions().position(pReceived).title("received"));
                            // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pReceived, 15)); // zoom
*/

                        }
                        msgLog += n + ": " + newMsg;
                        MapsActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                chatMsg.setText(msgLog);
                            }
                        });

                        broadcastMsg(n + ": " + newMsg);
                    }

                    if(!msgToSend.equals("")){
                        dataOutputStream.writeUTF(msgToSend);
                        dataOutputStream.flush();
                        msgToSend = "";
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                userList.remove(connectClient);

                MapsActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        spUsersAdapter.notifyDataSetChanged();
                        Toast.makeText(MapsActivity.this,
                                connectClient.name + " removed.", Toast.LENGTH_LONG).show();

                        msgLog += "-- " + connectClient.name + " leaved\n";
                        MapsActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                chatMsg.setText(msgLog);
                            }
                        });

                        broadcastMsg("-- " + connectClient.name + " leaved\n");
                    }
                });
            }

        }

        private void sendMsg(String msg){
            msgToSend = msg;
        }

    }

    private void broadcastMsg(String msg){
        for(int i=0; i<userList.size(); i++){
            userList.get(i).chatThread.sendMsg(msg);
            msgLog += "- send to " + userList.get(i).name + "\n";
        }

        MapsActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                chatMsg.setText(msgLog);
            }
        });
    }

    private String getIpAddress() {
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
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }


        LatLng latLng ;       return ip;
    }

    class ChatClient {
        String name;
        Socket socket;
        ConnectThread chatThread;
        LatLng latLng ;
        List<LatLng> latLngLista =  new ArrayList<LatLng>();
        @Override
        public String toString() {
            return name + ": " + socket.getInetAddress().getHostAddress();
        }
    }







    private void refreshPosition(){
       mMap.clear();

        for(int i=0; i<userList.size(); i++){
            if( userList.get(i).latLng != null) {
                addCustomMarker(userList.get(i));
                userList.get(i).latLngLista.add(userList.get(i).latLng);
               // userList.get(i).latLng= null ;
                drawPolyLineOnMap(userList.get(i).latLngLista);
                // mMap.addMarker(new MarkerOptions().position(userList.get(i).latLng).title("Marker"));
                // Log.i("infoo","latlllllll:"+ userList.get(i).latLng.toString());
            }
            cameraset();
        }
    }

    public void drawPolyLineOnMap(List<LatLng> list) {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.RED);
        polyOptions.width(5);
        polyOptions.addAll(list);
        //step 2  MapsActivity.this.mMap
       // mMap.clear();

        mMap.addPolyline(polyOptions);

    }

    public void cameraset(){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ChatClient client : userList) {
            builder.include(client.latLng);
        }
        final LatLngBounds bounds = builder.build();
        //BOUND_PADDING is an int to specify padding of bound.. try 100.
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cu);


    }

    private void addCustomMarker(ChatClient chatClient) {


        if (mMap == null) {
            return;
        }
        Log.d("infooo", "addCustomMarker()");
        // adding a marker on map with image from  drawable
        MapsActivity.this.mMap.addMarker(new MarkerOptions()
                .position(chatClient.latLng)
                .title(chatClient.name)

        );





        //  MapsActivity.this.mMap.moveCamera(CameraUpdateFactory.newLatLng(lll));
    }
    private void addCustomMarker(LatLng latLng) {


        if (mMap == null) {
            return;
        }
        Log.d("infooo", "addCustomMarker()");
        // adding a marker on map with image from  drawable
        MapsActivity.this.mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("latlng")

        );





        //  MapsActivity.this.mMap.moveCamera(CameraUpdateFactory.newLatLng(lll));
    }
}
