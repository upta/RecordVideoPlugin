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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class RecordVideo extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        try {
            if (action.equals("record")) {
                record();
            }
            else {
                status = PluginResult.Status.INVALID_ACTION;
            }
            callbackContext.sendPluginResult(new PluginResult(status, result));
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        } catch (IOException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
        }
        return true;
    }

    private void record() {
        Intent intent = new Intent(this, RecordActivity.class);
        this.cordova.getActivity().startActivityForResult((CordovaPlugin) this, intent, 0);
    }
}
