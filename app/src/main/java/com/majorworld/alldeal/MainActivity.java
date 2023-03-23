package com.majorworld.alldeal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivityLog";
    // ------------------------- VAR -------------------------- //
    WebView wView;
    ProgressBar pBar;
    String defaultUrl = "http://211.37.174.67:3000";

    // ------------------- DeviceControl ---------------------- //
    @Override
    public void onBackPressed() {
        if(wView.canGoBack()){      // if IS BACK PAGE
            wView.goBack();         // GO BACK
        }else{
            super.onBackPressed();  // IF NOT BACK PAGE -> EXIT APPLICATION
        }
    }

    // ----------------------- ONCREATE ------------------------ //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //base layout

        wView = findViewById(R.id.wView);
        pBar = findViewById(R.id.pBar);
        setWebView();
        wView.loadUrl(defaultUrl);
        pBar.setVisibility(View.GONE);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, token);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ------------------------ SETTING ------------------------ //
    public void setWebView(){
        // 1. Client Setting
        wView.setWebChromeClient(new WebChromeClient());
        wView.setWebViewClient(new WebViewClient() {
            @Override                                   // 1) Loading
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pBar.setVisibility(View.VISIBLE);       // Show Loading
            }
            @Override                                   // 2) Start
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pBar.setVisibility(View.GONE);          // Hide Loading
            }
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.d(TAG, request.getUrl().toString());

                if (request.getUrl().getScheme().equals("intent")) {
                    try {
                        // Intent 생성
                        Intent intent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);

                        // 실행 가능한 앱이 있으면 앱 실행
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                            Log.d(TAG, "ACTIVITY: " + intent.getPackage());
                            return true;
                        }

                        // Fallback URL이 있으면 현재 웹뷰에 로딩
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null) {
                            view.loadUrl(fallbackUrl);
                            Log.d(TAG, "FALLBACK: " + fallbackUrl);
                            return true;
                        }

                        // 앱이 설치되어 있지 않으면 마켓으로 이동
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        if (marketIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(marketIntent);
                            Log.d(TAG, "MARKET: " + intent.getPackage());
                            return true;
                        }

                        Log.e(TAG, "Could not parse anything");

                    } catch (URISyntaxException e) {
                        Log.e(TAG, "Invalid intent request", e);
                    }
                }

                // 나머지 서비스 로직 구현
                return false;
            }
        });


        // 2. WebView Settings
        WebSettings webSettings = wView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
    }
}