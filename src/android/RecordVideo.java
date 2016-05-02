/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 */

package com.caasera.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class RecordVideo extends CordovaPlugin {
    
    CallbackContext _callbackContext;
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        
        _callbackContext = callbackContext;
        
        if (action.equals("record")) {
            record();
            return true;
        }
            
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {   	
    	if (resultCode == Activity.RESULT_OK)
        {
            _callbackContext.success(intent.intent.getStringExtra("file"));
        }
        else
        {
            _callbackContext.error("");
        }
    }

    private void record() {
        final CordovaPlugin plugin = (CordovaPlugin) this;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = cordova.getActivity().getApplicationContext();
                Intent intent = new Intent(context, RecordActivity.class);
                
                cordova.startActivityForResult( plugin, intent, 0 );
            }
        });
    }
}
