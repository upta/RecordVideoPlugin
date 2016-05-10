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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import com.afollestad.materialcamera.MaterialCamera;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(cordova.getActivity(), "Saved to: " + data.getDataString(), Toast.LENGTH_LONG).show();
            } else if(data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                e.printStackTrace();
                Toast.makeText(cordova.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        /*
        if (resultCode == Activity.RESULT_OK)
        {
            _callbackContext.success(intent.getDataString());
        }
        else
        {
            _callbackContext.error("");
        }*/
    }

    private void record() {
        /*final CordovaPlugin plugin = (CordovaPlugin) this;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                File saveDir = new File(Environment.getExternalStorageDirectory(), "Caasera");
                saveDir.mkdirs();

                new MaterialCamera(cordova.getActivity())
                        .saveDir(saveDir)
                        .showPortraitWarning(false)
                        .autoSubmit(true)
                        .allowRetry(false)
                        .videoFrameRate(120)
                        .videoPreferredHeight(720)
                        .countdownSeconds(30f)
                        .defaultToFrontFacing(true)
                        .start(1);
            }
        });*/
                cordova.setActivityResultCallback(this);
        
                //File saveDir = new File(Environment.getExternalStorageDirectory(), "Caasera");
                //saveDir.mkdirs();

                new MaterialCamera(cordova.getActivity())
                        //.saveDir(saveDir)
                        .showPortraitWarning(false)
                        .defaultToFrontFacing(false) 
                        .autoSubmit(true)
                        .allowRetry(false)
                        .videoFrameRate(120)
                        .videoPreferredHeight(720)
                        .countdownSeconds(6f)
                        .defaultToFrontFacing(true)
                        .start(1);
    }
}