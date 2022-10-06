package org.apache.cordova;

import android.net.Uri;
import android.util.Log;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaResourceApi.OpenForReadResult;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Zip extends CordovaPlugin {

    private static final String LOG_TAG = "Zip";

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("unzip".equals(action)) {
            unzip(args, callbackContext);
            return true;
        }
        return false;
    }

    private void unzip(final CordovaArgs args, final CallbackContext callbackContext) {
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                unzipSync(args, callbackContext);
            }
        });
    }

    // Can't use DataInputStream because it has the wrong endian-ness.
    private static int readInt(InputStream is) throws IOException {
        int a = is.read();
        int b = is.read();
        int c = is.read();
        int d = is.read();
        return a | b << 8 | c << 16 | d << 24;
    }

    private static String getFileNameFromExtraData(FileHeader fileHeader) {
        if (fileHeader.getExtraDataRecords() != null) {
            for (ExtraDataRecord extraDataRecord : fileHeader.getExtraDataRecords()) {
                long identifier = extraDataRecord.getHeader();
                if (identifier == 0x7075) {
                    byte[] bytes = extraDataRecord.getData();
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    byte version = buffer.get();
                    assert (version == 1);
                    int crc32 = buffer.getInt();
                    return new String(bytes, 5, buffer.remaining(), StandardCharsets.UTF_8);
                }
            }
        }
        return fileHeader.getFileName();
    }

    private void unzipSync(CordovaArgs args, CallbackContext callbackContext) {
        try {
            String zipFileName = args.getString(0);
            String outputDirectory = args.getString(1);

            // Since Cordova 3.3.0 and release of File plugins, files are accessed via cdvfile://
            // Accept a path or a URI for the source zip.
            Uri zipUri = getUriForArg(zipFileName);
            Uri outputUri = getUriForArg(outputDirectory);

            CordovaResourceApi resourceApi = webView.getResourceApi();

            File tempFile = resourceApi.mapUriToFile(zipUri);
            if (tempFile == null || !tempFile.exists()) {
                String errorMessage = "Zip file does not exist";
                callbackContext.error(errorMessage);
                Log.e(LOG_TAG, errorMessage);
                return;
            }

            File outputDir = resourceApi.mapUriToFile(outputUri);
            outputDirectory = outputDir.getAbsolutePath();
            outputDirectory += outputDirectory.endsWith(File.separator) ? "" : File.separator;
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                String errorMessage = "Could not create output directory";
                callbackContext.error(errorMessage);
                Log.e(LOG_TAG, errorMessage);
                return;
            }

            ProgressEvent progress = new ProgressEvent();

            ZipFile uncompressFile = new ZipFile(tempFile.getAbsoluteFile());
            List<FileHeader> fileHeaderList = uncompressFile.getFileHeaders();
            progress.setTotal(fileHeaderList.size());
            for (int i = 0; i < fileHeaderList.size(); i++) {
                FileHeader fileHeader = fileHeaderList.get(i);
                uncompressFile.extractFile(fileHeader, outputDirectory, getFileNameFromExtraData(fileHeader));
                progress.setLoaded(i + 1);
                updateProgress(callbackContext, progress);
            }

            // final progress = 100%
            progress.setLoaded(fileHeaderList.size());
            updateProgress(callbackContext, progress);
            callbackContext.success();
        } catch (Exception e) {
            String errorMessage = "An error occurred while unzipping.";
            callbackContext.error(errorMessage);
            Log.e(LOG_TAG, errorMessage, e);
        }
    }

    private void updateProgress(CallbackContext callbackContext, ProgressEvent progress) throws JSONException {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, progress.toJSONObject());
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    private Uri getUriForArg(String arg) {
        CordovaResourceApi resourceApi = webView.getResourceApi();
        Uri tmpTarget = Uri.parse(arg);
        return resourceApi.remapUri(
                tmpTarget.getScheme() != null ? tmpTarget : Uri.fromFile(new File(arg)));
    }

    private static class ProgressEvent {
        private long loaded;
        private long total;

        public long getLoaded() {
            return loaded;
        }

        public void setLoaded(long loaded) {
            this.loaded = loaded;
        }

        public void addLoaded(long add) {
            this.loaded += add;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public JSONObject toJSONObject() throws JSONException {
            return new JSONObject(
                    "{loaded:" + loaded +
                            ",total:" + total + "}");
        }
    }
}
