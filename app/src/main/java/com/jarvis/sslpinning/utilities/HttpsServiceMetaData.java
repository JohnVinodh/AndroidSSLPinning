package com.jarvis.sslpinning.utilities;

public class HttpsServiceMetaData {
    public static Integer timeoutIntervalForRequest = 6; // Declared in Seconds
    public static Integer timeoutIntervalForResource = 6; // Declared in Seconds
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_HEAD = "HEAD";
    public static boolean enableKeepAlive = true;
    public static final String HTTP_RESPONSE_TYPE_TEXT = "text"; //	application/text , text/plain
    public static final String HTTP_RESPONSE_TYPE_JSON = "json"; //	application/json
    public static final String HTTP_RESPONSE_TYPE_DOCUMENT = "document"; //	application/xml , text/xml
    public static final String HTTP_RESPONSE_TYPE_RAWDATA = "rawdata"; //	anything apart from above
}
