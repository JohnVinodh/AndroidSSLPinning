package com.jarvis.sslpinning.utilities;

import java.net.PortUnreachableException;

public class HttpsServiceMetaData {
    public static Integer timeoutIntervalForRequest = 6; // Declared in Seconds
    public static Integer timeoutIntervalForResource = 6; // Declared in Seconds
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_HEAD = "HEAD";
    public static boolean enableKeepAlive = false;
    public static final String HTTP_RESPONSE_TYPE_TEXT = "text"; //	application/text , text/plain
    public static final String HTTP_RESPONSE_TYPE_JSON = "json"; //	application/json
    public static final String HTTP_RESPONSE_TYPE_DOCUMENT = "document"; //	application/xml , text/xml
    public static final String HTTP_RESPONSE_TYPE_RAWDATA = "rawdata"; //	anything apart from above
    public static boolean EXPECT_100_CONTINUE = true;
    public static  boolean IS_GZIP_ENABLED = false;
    public static int threadCount = 1;
}
