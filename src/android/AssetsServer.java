package com.metacode.cordova;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class AssetsServer extends ResourceServer {
    private final AssetManager assetManager;

    public AssetsServer(int port, AssetManager assetManager) {
        super(port);
        this.assetManager = assetManager;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public static class AssetsHandler extends ResourceHandler {
        @Override
        protected long getFileAvailable(String filePath, InputStream inputStream, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) throws Exception {
            String assetPath = filePath.replaceAll("^\\/", "");
            AssetsServer server = uriResource.initParameter(1, AssetsServer.class);
            AssetManager assetManager = server.getAssetManager();
            try {
                AssetFileDescriptor descriptor = assetManager.openFd(assetPath);
                return descriptor.getLength();
            } catch (Exception e) {
                if (inputStream != null) {
                    return inputStream.available();
                }
            }
            return 0;
        }

        @Override
        protected InputStream createFileInputStream(String filePath, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) throws Exception {
            String assetPath = filePath.replaceAll("^\\/", "");
            AssetsServer server = uriResource.initParameter(1, AssetsServer.class);
            AssetManager assetManager = server.getAssetManager();
            return assetManager.open(assetPath);
        }

        @Override
        protected boolean isFileExist(String filePath, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) throws IOException {
            String assetPath = filePath.replaceAll("^\\/", "");
            String folder;
            String name = assetPath;
            int index = assetPath.lastIndexOf('/');
            if (index >= 0) {
                folder = assetPath.substring(0, index);
                name = assetPath.substring(index + 1);
                index = name.lastIndexOf('?');
                if (index >= 0) {
                    name = name.substring(0, index);
                }
            } else {
                folder = "";
            }
            AssetsServer server = uriResource.initParameter(1, AssetsServer.class);
            AssetManager assetManager = server.getAssetManager();
            String[] paths = assetManager.list(folder);
            if (paths != null) {
                for (int j = 0; j < paths.length; ++j) {
                    if (paths[j].equals(name)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static class PluginInjectionIndexHandler extends ResourceHandler {
        protected Response injectCordovaJsToIndex(InputStream inputStream, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) throws IOException {
            final char[] buffer = new char[8192];
            final StringBuilder result = new StringBuilder();
            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                int charsRead;
                while ((charsRead = reader.read(buffer, 0, buffer.length)) > 0) {
                    result.append(buffer, 0, charsRead);
                }
                inputStream.close();
            }
            String htmlText = result.toString();
            int index = htmlText.lastIndexOf("</body>");
            if (index >= 0) {
                int servePort = uriResource.initParameter(2, Integer.class);
                String cordovaJs = String.format(Locale.getDefault(), "http://localhost:%d/assets/www/cordova.js", servePort);
                String injectText = htmlText.substring(0, index)
                        + String.format(Locale.getDefault(), "<script type=\"text/javascript\" src=\"%s\"></script>", cordovaJs)
                        + htmlText.substring(index);
                return ResourceServer.newFixedLengthResponse(Response.Status.OK, mimeTypes().get("html"), injectText);
            }
            return ResourceServer.newFixedLengthResponse(Response.Status.OK, mimeTypes().get("html"), htmlText);
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String resourcePath = uriResource.initParameter(0, String.class);
            String filePath = (resourcePath == null ? "" : resourcePath).concat("index.html");
            try {
                File file = new File(filePath);
                FileInputStream fis = new FileInputStream(file);
                return injectCordovaJsToIndex(fis, uriResource, urlParams, session);
            } catch (Exception e) {
                return ResourceServer.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, mimeTypes().get("json"), String.format(Locale.getDefault(), "{\"success\":false,\"message\":\"%s\"}", e));
            }
        }

        @Override
        public Response delete(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return ResourceServer.newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, mimeTypes().get("json"), String.format(Locale.getDefault(), "{\"success\":false,\"message\":\"%s\"}", "Not supported for android assets"));
        }
    }

    public void start(int timeout, boolean daemon, String resourcePath, int servePort) throws IOException {
        addRoute("/index", PluginInjectionIndexHandler.class, this.normalizePath(resourcePath), this, servePort);
        addRoute("/index.html", PluginInjectionIndexHandler.class, this.normalizePath(resourcePath), this, servePort);
        addRoute("/", PluginInjectionIndexHandler.class, this.normalizePath(resourcePath), this, servePort);
        addRoute("", PluginInjectionIndexHandler.class, this.normalizePath(resourcePath), this, servePort);
        super.start(timeout, daemon, resourcePath);
    }

    @Override
    public void serve(int timeout, boolean daemon, String resourcePath) throws IOException {
        addRoute("/assets(.*)", AssetsHandler.class, null, this);
        super.serve(timeout, daemon, resourcePath);
    }
}
