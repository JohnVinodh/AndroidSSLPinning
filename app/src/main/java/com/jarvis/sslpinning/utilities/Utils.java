package com.jarvis.sslpinning.utilities;

/**
 * Created by KH2195 on 10/4/2016.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by johnvinodhtalluri on 31/07/16.
 */
public class Utils {
    private Context mContext = null;
    private static String TAG = Utils.class.getSimpleName();
    public Utils(Context pContext) {
        mContext = pContext;
    }

    /**
     * convert input stream to string
     *
     * @param inputStream Inputstream from server
     * @return the string
     */
    public static String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String res = "";

        while ((line = bufferedReader.readLine()) != null) {
            res += line;
        }

        inputStream.close();
        return res;
    }

    public static void adjustFontScale(Configuration configuration,Context context,int screenwidth)   {
        //configuration.fontScale =1.0f;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        //metrics.scaledDensity = configuration.fontScale * metrics.density;
        configuration.fontScale = (float)((metrics.widthPixels/metrics.density)/screenwidth);

        metrics.scaledDensity = configuration.fontScale * metrics.density;
        context.getResources().updateConfiguration(configuration, metrics);
    }

    public static byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public InputStream getInputStreamForVirtualFile(Uri uri, String mimeTypeFilter,Context context)
            throws IOException {

        ContentResolver resolver = context.getContentResolver();

        String[] openableMimeTypes = resolver.getStreamTypes(uri, mimeTypeFilter);

        if (openableMimeTypes == null ||
                openableMimeTypes.length < 1) {
            throw new FileNotFoundException();
        }

        return resolver
                .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)
                .createInputStream();
    }

    public static void safeCloseStream(Closeable fs) {
        if (fs != null) {
            try {
                fs.close();
            } catch (Exception e) {

                Log.e(TAG, Log.getStackTraceString(e));
            }
            fs = null;
        }
    }
}
