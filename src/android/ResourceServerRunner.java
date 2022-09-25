package com.metacode.cordova;

import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ResourceServerRunner extends CordovaPlugin {
    static List<CallbackContext> runCallbackContexts = new ArrayList<>(1);
    static List<CallbackContext> exitCallbackContexts = new ArrayList<>(1);

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if ("start".equals(action)) {
            start(args, callbackContext);
            return true;
        } else if ("stop".equals(action)) {
            stop(args, callbackContext);
            return true;
        } else if ("redirect".equals(action)) {
            redirect(args, callbackContext);
        }
        return false;
    }

    private void start(final CordovaArgs args, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context applicationContext = cordova.getContext().getApplicationContext();
                    String contextPath = args.getString(0);
                    int contextPort = args.getInt(1);
                    String servePath = args.getString(2);
                    int servePort = args.getInt(3);
                    Intent intent = new Intent(applicationContext, ResourceService.class);
                    intent.putExtra("contextPath", contextPath);
                    intent.putExtra("servePath", servePath);
                    intent.putExtra("servePort", servePort);
                    intent.putExtra("contextPort", contextPort);
                    applicationContext.startService(intent);
                    runCallbackContexts.add(callbackContext);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void stop(final CordovaArgs args, final CallbackContext callbackContext) {
        exitCallbackContexts.add(callbackContext);
        Context applicationContext = cordova.getContext().getApplicationContext();
        Intent intent = new Intent(applicationContext, ResourceService.class);
        applicationContext.stopService(intent);
    }

    private void redirect(final CordovaArgs args, final CallbackContext callbackContext) {
        try {
            String url = args.getString(0);
            CordovaActivity activity = (CordovaActivity) cordova.getActivity();
            activity.loadUrl(url);
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }
}
