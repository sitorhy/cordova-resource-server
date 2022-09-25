package com.metacode.cordova;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.apache.cordova.CallbackContext;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class ResourceService extends Service {
    private AssetsServer contextServer;
    private AssetsServer resourceServer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String contextPath = intent.getStringExtra("contextPath");
        String servePath = intent.getStringExtra("servePath");
        int contextPort = intent.getIntExtra("contextPort", -1);
        int servePort = intent.getIntExtra("servePort", -1);
        try {
            if (servePort > 0) {
                resourceServer = new AssetsServer(servePort, this.getAssets());
                resourceServer.serve(NanoHTTPD.SOCKET_READ_TIMEOUT, false, servePath);
            }
            if (contextPort > 0) {
                resourceServer = new AssetsServer(contextPort, this.getAssets());
                if (servePort > 0) {
                    resourceServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false, contextPath, servePort);
                } else {
                    resourceServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false, contextPath);
                }
            }
            for (CallbackContext context : ResourceServerRunner.runCallbackContexts) {
                context.success();
            }
        } catch (IOException e) {
            e.printStackTrace();
            for (CallbackContext context : ResourceServerRunner.runCallbackContexts) {
                context.error(e.getMessage());
            }
        } finally {
            ResourceServerRunner.runCallbackContexts.clear();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (resourceServer != null) {
            resourceServer.stop();
            resourceServer = null;
        }
        if (contextServer != null) {
            contextServer.stop();
            contextServer = null;
        }
        for (CallbackContext context : ResourceServerRunner.exitCallbackContexts) {
            context.success();
        }
        ResourceServerRunner.exitCallbackContexts.clear();
    }
}
