package com.metacode.cordova;

import android.text.TextUtils;
import android.util.Base64;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceServer extends RouterNanoHTTPD {
    public static Response newFixedLengthResponse(Response.IStatus status, String mimeType, InputStream data, long totalBytes) {
        Response response = NanoHTTPD.newFixedLengthResponse(status, mimeType, data, totalBytes);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Max-Age", "3628800");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
        response.addHeader("Access-Control-Allow-Headers", "Authorization");
        return response;
    }

    public static Response newFixedLengthResponse(Response.IStatus status, String mimeType, String txt) {
        ContentType contentType = new ContentType(mimeType);
        if (txt == null) {
            return ResourceServer.newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
        } else {
            byte[] bytes;
            try {
                CharsetEncoder newEncoder = Charset.forName(contentType.getEncoding()).newEncoder();
                if (!newEncoder.canEncode(txt)) {
                    contentType = contentType.tryUTF8();
                }
                bytes = txt.getBytes(contentType.getEncoding());
            } catch (UnsupportedEncodingException e) {
                bytes = new byte[0];
            }
            return ResourceServer.newFixedLengthResponse(status, contentType.getContentTypeHeader(), new ByteArrayInputStream(bytes), bytes.length);
        }
    }

    static public String getPathExtension(String filePath) {
        String extension;
        int index = filePath.lastIndexOf('.');
        if (index > 0) {
            extension = filePath.substring(index + 1);
        } else {
            extension = "";
        }
        index = filePath.lastIndexOf('?');
        if (index >= 0) {
            extension = extension.substring(0, index);
        }
        return extension;
    }

    public ResourceServer(int port) {
        super(port);
        mimeTypes().put("json", "application/json");
        mimeTypes().put("ico", "image/x-icon");
        mimeTypes().put("mp3", "audio/mpeg");
        mimeTypes().put("ogg", "audio/ogg");
        mimeTypes().put("wav", "audio/vnd.wav");
        mimeTypes().put("aac", "audio/aac");
        mimeTypes().put("m4a", "audio/m4a");
        mimeTypes().put("gz", "application/x-gzip");
        mimeTypes().put("tar", "application/x-tar");
        mimeTypes().put("mpg", "video/mpeg");
        mimeTypes().put("mpeg", "video/mpeg");
        mimeTypes().put("ra", "audio/x-pn-realaudio");
        mimeTypes().put("ram", "audio/x-pn-realaudio");
        mimeTypes().put("au", "audio/basic");
        mimeTypes().put("rtf", "application/rtf");
        mimeTypes().put("avi", "video/x-msvideo");
        mimeTypes().put("bin", "application/octet-stream");
        mimeTypes().put("bmp", "image/bmp");
        mimeTypes().put("bz", "application/x-bzip");
        mimeTypes().put("csv", "text/csv");
        mimeTypes().put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypes().put("eot", "application/vnd.ms-fontobject");
        mimeTypes().put("jar", "application/java-archive");
        mimeTypes().put("mid", "audio/midi");
        mimeTypes().put("midi", "audio/midi");
        mimeTypes().put("otf", "font/otf");
        mimeTypes().put("ppt", "application/vnd.ms-powerpoint");
        mimeTypes().put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mimeTypes().put("rar", "application/x-rar-compressed");
        mimeTypes().put("tif", "image/tiff");
        mimeTypes().put("tiff", "image/tiff");
        mimeTypes().put("ttf", "font/ttf");
        mimeTypes().put("weba", "audio/webm");
        mimeTypes().put("webm", "video/webm");
        mimeTypes().put("xhtml", "application/xhtml+xml");
        mimeTypes().put("xls", "application/vnd.ms-excel");
        mimeTypes().put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeTypes().put("woff2", "font/woff2");
        mimeTypes().put("woff", "font/woff");
        mimeTypes().put("7z", "application/x-7z-compressed");
        this.addMappings();
    }

    public static class ResourceHandler implements UriResponder {
        protected String getFilePathFromUri(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            Pattern pattern = Pattern.compile(uriResource.getUri());
            Matcher matcher = pattern.matcher(session.getUri());
            List<String> strings = new ArrayList<>(1);
            if (matcher.find()) {
                String n = matcher.group(1);
                if (n != null) {
                    if (n.equals("/") || n.equals("")) {
                        strings.add("index.html");
                    } else {
                        strings.add(n);
                    }
                }
            }
            if (strings.size() > 0) {
                String resourcePath = uriResource.initParameter(0, String.class);
                return (resourcePath == null ? "" : resourcePath).concat(strings.get(0).replaceAll("^\\/", ""));
            }
            return null;
        }

        protected InputStream createFileInputStream(String filePath, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) throws Exception {
            File file = new File(filePath);
            return new FileInputStream(file);
        }

        protected long getFileAvailable(String filePath, InputStream inputStream, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) throws Exception {
            return new File(filePath).length();
        }

        protected boolean isFileExist(String filePath, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) throws Exception {
            return new File(filePath).exists();
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String filePath = getFilePathFromUri(uriResource, urlParams, session);
            try {
                if (isFileExist(filePath, uriResource, urlParams, session)) {
                    try {
                        String extension = getPathExtension(filePath);
                        InputStream fis = createFileInputStream(filePath, uriResource, urlParams, session);
                        String range = session.getHeaders().get("Range");
                        if (range == null && session.getHeaders().containsKey("range")) {
                            range = session.getHeaders().get("range");
                        }
                        long nLen = getFileAvailable(filePath, fis, uriResource, urlParams, session);
                        long nStart = 0;
                        long nEnd = nLen - 1;
                        if (range != null) {
                            String[] positions = (range.split("=")[1]).split("-");
                            String strStart = positions[0].trim();
                            if (positions.length > 2) {
                                String strEnd = positions[2].trim();
                                nEnd = Long.parseLong(strEnd);
                            }
                            nStart = Long.parseLong(strStart);
                        }
                        if (range != null) {
                            fis.skip(nStart);
                        }
                        Response response = ResourceServer.newFixedLengthResponse(
                                range == null ? Response.Status.OK : Response.Status.PARTIAL_CONTENT,
                                mimeTypes().get(extension),
                                fis, nEnd - nStart + 1);
                        if (range != null) {
                            response.addHeader("Content-Range", String.format(Locale.getDefault(), "bytes %d-%d/%d", nStart, nEnd, nLen));
                        }
                        response.addHeader("Accept-Ranges", "bytes");
                        return response;
                    } catch (Exception e) {
                        return ResourceServer.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, mimeTypes().get("json"), String.format(Locale.getDefault(), "{\"success\":false,\"message\":\"%s\"}", e));
                    }
                } else {
                    return ResourceServer.newFixedLengthResponse(Response.Status.NOT_FOUND, mimeTypes().get("json"), "{\"success\":false,\"message\":\"Resource not found\"}");
                }
            } catch (Exception e) {
                return ResourceServer.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, mimeTypes().get("json"), String.format(Locale.getDefault(), "{\"success\":false,\"message\":\"%s\"}", e));
            }
        }

        @Override
        public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return get(uriResource, urlParams, session);
        }

        @Override
        public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return get(uriResource, urlParams, session);
        }

        @Override
        public Response delete(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String filePath = getFilePathFromUri(uriResource, urlParams, session);
            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    return newFixedLengthResponse(Response.Status.OK, mimeTypes().get("json"), "{\"success\":true}");
                } else {
                    return ResourceServer.newFixedLengthResponse(Response.Status.FORBIDDEN, mimeTypes().get("json"), "{\"success\":false,\"message\":\"Not allowed\"}");
                }
            } else {
                return ResourceServer.newFixedLengthResponse(Response.Status.NOT_FOUND, mimeTypes().get("json"), "{\"success\":false,\"message\":\"Resource not found\"}");
            }
        }

        @Override
        public Response other(String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            if (method.equalsIgnoreCase("HEAD")) {
                String filePath = getFilePathFromUri(uriResource, urlParams, session);
                try {
                    if (isFileExist(filePath, uriResource, urlParams, session)) {
                        return ResourceServer.newFixedLengthResponse(Response.Status.OK, null, "");
                    } else {
                        return ResourceServer.newFixedLengthResponse(Response.Status.NOT_FOUND, null, "");
                    }
                } catch (Exception e) {
                    return ResourceServer.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, null, "");
                }
            } else {
                return ResourceServer.newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, mimeTypes().get("json"), "{\"success\":false,\"message\":\"Method Not Allowed\"}");
            }
        }
    }

    public static class VersionHandler extends DefaultHandler {
        @Override
        public String getText() {
            return "{\"success\":true,\"data\":\"0.1.0\"}";
        }

        @Override
        public String getMimeType() {
            return mimeTypes().get("json");
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return ResourceServer.newFixedLengthResponse(this.getStatus(), this.getMimeType(), this.getText());
        }
    }

    public static class IconHandler implements UriResponder {
        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            byte[] bytes = Base64.decode("AAABAAEAEBAAAAEAGABoAwAAFgAAACgAAAAQAAAAIAAAAAEAGAAAAAAAAAMAACMuAAAjLgAAAAAAAAAAAACkZxWkZxajZRSiYxChYAyeXgedXASdWwKcWgGcWgGcWgGcWgGcWgGcWgGcWgGcWgGmaxunaxumahmkZxajZBCgYAueXgedXQWdWwGcWgGcWwKdXASdXAWdWwKcWgGcWgGpcCSpcCOobyGlaRijZROjZBCfXwmaWAOdXAOdWwKbWAKVTgCUTgCaVwGdWwKcWgGsdiysdi2pcSWxfTaueC+hYxClaBexezOcWwKbWACfXw/Rrn7XuI6mah2ZVQCdWwKwezSwfDembiHVt43RsIOcXASzgDvjy6ydWwKcWwLo1Lnv38rr2MDx4s6hYg6bWACyfzyzgD2tdy/o1bvp1LqgYg7Iom778eWpbyGfXwjNqXafXxChYRX+9+2+kFOVTwC0hEOzgT+6jVD47N337N2ueTHexKL///vCmF+bWQCaVwWbVwCdWgr3697DmWCUTgC2iEmxgD3OrH/059fy49DProHp1r3x4s3exaOfYQ2laBelZxbFm2P/+fKxezSYUwC4jU+zhEHn1LngyKnWuJH57uDo1LnLpnX16dmsdiyiZRLkza//+vPOqnmdWgGfXge7jlO8kVb369zProDDnGb/+/TbwZ2zgkD47uDDmmOgYxDHn2v47d+qcSWfXwmiYw66kVbMqnz57uHBmmO8klb47d7OrX+ufDbr2cHdw6GvezaueTPs2sLgx6agYw+maxq9lF3izK/x4s63i0/RsYb47d7AmGG1hkfVt5D+9+7269v369z47d//+fC9kVambB3Enm3QsYjNrYG/l2DKp3jOrYG9lFy9lFzAmGHRsojSs4nRsYbProHOrH25i06ueDLHpXfEoXHEoG/GonLDnGnAmWPCm2fBmmW/lmG6j1a5jVK3iU21hkezgkG0hEKzg0PHpXbIpnrJp3rHpXbHpHXGonPFn27EnmvDnGjCm2fBmWS/lmC9klq8kFa5i0+1iEnGo3PIpXjJpnnIpnnIpXjIpHbGonPFoG/EnmzDnGjBmWS/lmC8k1m7j1W5jFC3ikwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", Base64.DEFAULT);
            return ResourceServer.newFixedLengthResponse(Response.Status.OK, mimeTypes().get("ico"), new ByteArrayInputStream(bytes), bytes.length);
        }

        @Override
        public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return other(session.getMethod().name(), uriResource, urlParams, session);
        }

        @Override
        public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return other(session.getMethod().name(), uriResource, urlParams, session);
        }

        @Override
        public Response delete(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return other(session.getMethod().name(), uriResource, urlParams, session);
        }

        @Override
        public Response other(String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            if (method.equalsIgnoreCase("HEAD")) {
                return ResourceServer.newFixedLengthResponse(Response.Status.OK, null, "");
            } else {
                return ResourceServer.newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, mimeTypes().get("json"), "{\"success\":false,\"message\":\"Method Not Allowed\"}");
            }
        }
    }

    public static class ListHandler implements UriResponder {
        protected String getFilePathFromUri(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            Matcher matcher = Pattern.compile(uriResource.getUri()).matcher(session.getUri());
            List<String> strings = new ArrayList<>(1);
            if (matcher.find()) {
                strings.add(matcher.group(1));
            }
            if (strings.size() > 0) {
                String resourcePath = uriResource.initParameter(0, String.class);
                return resourcePath.concat(strings.get(0).replaceAll("^\\/", ""));
            }
            return null;
        }

        @Override
        public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            String filePath = getFilePathFromUri(uriResource, urlParams, session);
            if (filePath == null) {
                return ResourceServer.newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeTypes().get("json"), "{\"success\":true,\"data\":\"Invalid Path\"}");
            }
            File file = new File(filePath);
            if (file.exists()) {
                if (file.isFile()) {
                    String info = String.format(Locale.getDefault(), "{\"size\":%d,\"name\":\"%s\",\"isFile\":true}", file.length(), file.getName());
                    return ResourceServer.newFixedLengthResponse(Response.Status.OK, mimeTypes().get("json"), "{\"success\":true,\"data\":" + info + "}");
                } else {
                    String type = null;
                    String qs = session.getQueryParameterString();
                    if (qs != null && qs.length() > 0) {
                        String[] pairs = qs.split("&");
                        for (String pair : pairs) {
                            String[] kv = pair.split("=");
                            if (kv.length > 1 && kv[0].equals("type")) {
                                type = kv[1];
                                break;
                            }
                        }
                    }
                    File[] files = file.listFiles();
                    if (files == null) {
                        files = new File[0];
                    }
                    List<File> filterFiles;
                    if (type != null) {
                        filterFiles = new ArrayList<>(files.length);
                        for (File f : files) {
                            String name = f.getName();
                            String extension = getPathExtension(name);
                            if (extension.equalsIgnoreCase(type)) {
                                filterFiles.add(f);
                            }
                        }
                    } else {
                        filterFiles = null;
                    }
                    List<String> builder = new ArrayList<>(files.length);
                    if (filterFiles != null) {
                        for (File f : filterFiles) {
                            builder.add(String.format(Locale.getDefault(), "{\"size\":%d,\"name\":\"%s\",\"isFile\":%s}", f.length(), f.getName(), f.isFile() ? "true" : "false"));
                        }
                    } else {
                        for (File f : files) {
                            builder.add(String.format(Locale.getDefault(), "{\"size\":%d,\"name\":\"%s\",\"isFile\":%s}", f.length(), f.getName(), f.isFile() ? "true" : "false"));
                        }
                    }
                    String info = String.format(Locale.getDefault(), "{\"size\":%d,\"type\":%s,\"name\":\"%s\",\"isFile\":false,\"children\":[%s]}", file.length(), type == null ? "null" : "\"" + type + "\"", file.getName(), TextUtils.join(",", builder));
                    return ResourceServer.newFixedLengthResponse(Response.Status.OK, mimeTypes().get("json"), "{\"success\":true,\"data\":" + info + "}");
                }
            } else {
                return ResourceServer.newFixedLengthResponse(Response.Status.NOT_FOUND, mimeTypes().get("json"), "{\"success\":false,\"message\":\"Resource not found\"}");
            }
        }

        @Override
        public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return other(session.getMethod().name(), uriResource, urlParams, session);
        }

        @Override
        public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return other(session.getMethod().name(), uriResource, urlParams, session);
        }

        @Override
        public Response delete(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            return other(session.getMethod().name(), uriResource, urlParams, session);
        }

        @Override
        public Response other(String method, UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            if (method.equalsIgnoreCase("HEAD")) {
                String filePath = getFilePathFromUri(uriResource, urlParams, session);
                if (filePath == null) {
                    return ResourceServer.newFixedLengthResponse(Response.Status.BAD_REQUEST, null, "");
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        return ResourceServer.newFixedLengthResponse(Response.Status.OK, null, "");
                    } else {
                        return ResourceServer.newFixedLengthResponse(Response.Status.NOT_FOUND, null, "");
                    }
                }
            } else {
                return ResourceServer.newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, mimeTypes().get("json"), "{\"success\":false,\"message\":\"Method Not Allowed\"}");
            }
        }
    }

    protected String normalizePath(String resourcePath) {
        return (resourcePath.charAt(resourcePath.length() - 1) == '/' ? resourcePath : resourcePath.concat("/")).replaceAll("\\\\", "/");
    }

    public void start(int timeout, boolean daemon, String resourcePath) throws IOException {
        addRoute("/(.*)", ResourceHandler.class, this.normalizePath(resourcePath));
        start(timeout, daemon);
    }

    public void serve(int timeout, boolean daemon, String resourcePath) throws IOException {
        addRoute("/resource(.*)", ResourceHandler.class, this.normalizePath(resourcePath));
        addRoute("/list(.*)", ListHandler.class, this.normalizePath(resourcePath));
        addRoute("/", VersionHandler.class);
        addRoute("/resource", ResourceHandler.class, resourcePath);
        addRoute("/version", VersionHandler.class);
        addRoute("/favicon.ico", IconHandler.class);
        super.start(timeout, daemon);
    }
}
