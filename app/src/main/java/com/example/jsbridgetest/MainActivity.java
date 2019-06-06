package com.example.jsbridgetest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.github.lzyzsd.jsbridge.action.BridgeWebView;
import com.google.gson.Gson;

/**
 * android java和javascript桥，灵感来自于微信webview jsbridge
 * https://github.com/lzyzsd/JsBridge
 *
 * android端: 发送消息，接收消息
 *
 *
 *
 * js端发送消息， 接收消息;
 *
 *
 *
 * Register a Java handler function so that js can call
 *
 *     webView.registerHandler("submitFromWeb", new BridgeHandler() {
 *         @Override
 *         public void handler(String data, CallBackFunction function) {
 *             Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
 *             function.onCallBack("submitFromWeb exe, response data from Java");
 *         }
 *     });
 *
 * js can call this Java handler method "submitFromWeb" through:
 *
 *     WebViewJavascriptBridge.callHandler(
 *         'submitFromWeb'
 *         , {'param': str1}
 *         , function(responseData) {
 *             document.getElementById("show").innerHTML = "send get responseData from java, data = " + responseData
 *         }
 *     );
 *
 *
 *
 */
public class MainActivity extends Activity implements OnClickListener {

	private final String TAG = "JsBridgeTest";

	BridgeWebView bridgeWebView;

	Button button;

	int RESULT_CODE = 0;

	ValueCallback<Uri> mUploadMessage;

	ValueCallback<Uri[]> mUploadMessageArray;

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
        String testStr;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bridgeWebView = (BridgeWebView) findViewById(R.id.webView);

		button = (Button) findViewById(R.id.button);

		button.setOnClickListener(this);

		bridgeWebView.setDefaultHandler(new DefaultHandler());

		bridgeWebView.setWebChromeClient(new WebChromeClient() {

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
				this.openFileChooser(uploadMsg);
			}

			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
				this.openFileChooser(uploadMsg);
			}

			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				mUploadMessage = uploadMsg;
				pickFile();
			}

			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
				mUploadMessageArray = filePathCallback;
				pickFile();
				return true;
			}
		});

		bridgeWebView.loadUrl("file:///android_asset/demo.html");

		// Register a Java handler function so that js can call
		bridgeWebView.registerHandler("submitFromWeb", new BridgeHandler() {

			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
				// I/MainActivity: handler = submitFromWeb, data from web = {"param":"中文测试"}
                function.onCallBack("submitFromWeb exe, response data 中文 from Java");
			}

		});


        User user = new User();
        Location location = new Location();
        location.address = "SDU";
        user.location = location;
        user.name = "大头鬼";

		bridgeWebView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
				Log.i(TAG, "functionInJs: " + "data = " + data);
				// I/JsBridgeTest: onCallBack: data = Javascript Says Right back aka!
//				Toast.makeText(MainActivity.this, "data = " + data, Toast.LENGTH_SHORT).show();
            }
        });

		// 测试没用
//		bridgeWebView.send("hello");
//		bridgeWebView.send("test", new CallBackFunction() {
//			@Override
//			public void onCallBack(String data) {
//				Log.i(TAG, "发消息给Native : " + data);
//			}
//		});

	}

	public void pickFile() {
		Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
		chooserIntent.setType("image/*");
		startActivityForResult(chooserIntent, RESULT_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == RESULT_CODE) {
			if (null == mUploadMessage && null == mUploadMessageArray){
				return;
			}
			if(null!= mUploadMessage && null == mUploadMessageArray){
				Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
				mUploadMessage.onReceiveValue(result);
				mUploadMessage = null;
			}

			if(null == mUploadMessage && null != mUploadMessageArray){
				Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
				mUploadMessageArray.onReceiveValue(new Uri[]{result});
				mUploadMessageArray = null;
			}

		}
	}

	@Override
	public void onClick(View v) {
		if (button.equals(v)) {
			bridgeWebView.callHandler("functionInJs", "点击了原生button", new CallBackFunction() {

				@Override
				public void onCallBack(String data) {
					Log.i(TAG, "response data from js " + data);
					// I/JsBridgeTest: response data from js Javascript Says Right back aka!
				}

			});
		}


	}

}
