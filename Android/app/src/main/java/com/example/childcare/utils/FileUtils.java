package com.example.childcare.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static String getPath(Context context, Uri uri) {
        Log.d(TAG, "Original URI: " + uri.toString());

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            Log.e(TAG, "Cursor is null!");
            return null;
        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();

        Log.d(TAG, "Resolved path: " + path);
        return path;
    }
}