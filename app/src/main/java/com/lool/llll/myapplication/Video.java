package com.lool.llll.myapplication;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Video.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Video#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Video extends Fragment {

    public WebView mWebView;
    EditText urlinput;
    Button okbutton;
    String user = "";
    MapsActivity ch = new MapsActivity();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_video, container, false);

        mWebView = (WebView) v.findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        urlinput = (EditText)v.findViewById(R.id.editText);
        okbutton = (Button) v.findViewById(R.id.button);


        if (okbutton != null)
            okbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.setWebViewClient(new WebViewClient());
                //    user = ch.selectedUser();
                 //   urlinput.setText(user);
                    mWebView.loadUrl("http://"+urlinput.getText().toString());
                }
            });
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    public Video() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
