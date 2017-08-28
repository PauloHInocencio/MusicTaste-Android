package com.poolapps.musictaste.server;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ImageLoader<T> extends HandlerThread  {

    private static final String TAG = ImageLoader.class.getSimpleName();
    private static final String LOCAL_CACHE_DIR_NAME = "MusicTasteAlbums";
    private static final int MESSAGE_GET_IMAGE_FROM_WEB = 0;
    private static final int MESSAGE_GET_IMAGE_FROM_PHONE = 1;

    private Handler mResponseHandler;
    private Handler mRequestHandler;
    private LruCache<String, Bitmap> mCache;
    private ConcurrentMap<T, String> mMapOfTargetsWaitingForBitmap = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, String> mMapOfUrlsWithoutTarget = new ConcurrentHashMap<>();
    private ImageLoaderListener<T> mListener;
    private  File mLocalCacheDir;


    public interface ImageLoaderListener<T> {
        void imageHasBeenDownloaded(T targetWaitingForBitmap, Bitmap bitmapDownloaded);
        void imageWasNotDownloaded(T targetWaitingForBitmap, String errorMessage);
    }


    public ImageLoader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        setupLocalCache();
        setupMemoryCache();
    }

    public void setImageLoaderListener(ImageLoaderListener<T> listener) {
        mListener = listener;
    }


    public void getBitmapFromWeb(String urlOfTheBitmap) {

        final Bitmap bitmapOnCache = getBitmapFromMemoryCache(urlOfTheBitmap);
        if (bitmapOnCache != null){
            mMapOfUrlsWithoutTarget.remove(urlOfTheBitmap.hashCode());
            mListener.imageHasBeenDownloaded(null, bitmapOnCache);
            return;
        }

        mMapOfUrlsWithoutTarget.put(urlOfTheBitmap.hashCode(), urlOfTheBitmap);
        mRequestHandler.obtainMessage(MESSAGE_GET_IMAGE_FROM_WEB, urlOfTheBitmap.hashCode())
                    .sendToTarget();

    }

    @NonNull
    public void getBitmapFromWeb(@NonNull  T targetWaitingForBitmap, String urlOfTheBitmap) {
        if (urlOfTheBitmap == null) {
            mMapOfTargetsWaitingForBitmap.remove(targetWaitingForBitmap);
            return;
        }

        final Bitmap bitmapOnCache = getBitmapFromMemoryCache(urlOfTheBitmap);
        if (bitmapOnCache != null){
            mMapOfTargetsWaitingForBitmap.remove(targetWaitingForBitmap);
            mListener.imageHasBeenDownloaded(targetWaitingForBitmap, bitmapOnCache);
            return;
        }

        mMapOfTargetsWaitingForBitmap.put(targetWaitingForBitmap, urlOfTheBitmap);
        mRequestHandler.obtainMessage(MESSAGE_GET_IMAGE_FROM_WEB, targetWaitingForBitmap)
                    .sendToTarget();

    }

    public void getImageFromPhone(String uriOfTheBitmap) {

    }

    public void getBitmapFromPhone(T targetWaitingForBitmap, String uriOfTheBitmap) {

    }

    public void clearQueueOfWaitingTargets() {
        mRequestHandler.removeMessages(MESSAGE_GET_IMAGE_FROM_WEB);
        mRequestHandler.removeMessages(MESSAGE_GET_IMAGE_FROM_PHONE);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MESSAGE_GET_IMAGE_FROM_WEB:
                        T target;
                        if (msg.obj != null){
                            if (msg.obj instanceof Integer) {
                                requestDownloadBitmap(null, (Integer) msg.obj);
                            } else {
                                target = (T) msg.obj;
                                requestDownloadBitmap(target, null);
                            }
                        }
                        break;
                }
            }
        };
    }

    private void requestDownloadBitmap(final T targetWaitingForBitmap, final Integer urlIndex) {
        try {
            final String urlOfTheImageRequested;
            if (targetWaitingForBitmap != null ){
                urlOfTheImageRequested = mMapOfTargetsWaitingForBitmap.get(targetWaitingForBitmap);
            } else {
                urlOfTheImageRequested = mMapOfUrlsWithoutTarget.get(urlIndex);
            }

            final Bitmap bitmapDownloaded = downloadBitmap(urlOfTheImageRequested);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {

                    String oldURL= targetWaitingForBitmap != null
                                ? mMapOfTargetsWaitingForBitmap.get(targetWaitingForBitmap)
                                : mMapOfUrlsWithoutTarget.get(urlIndex);

                    if (oldURL != null && urlOfTheImageRequested != null && oldURL.equals(urlOfTheImageRequested)) {
                        if (targetWaitingForBitmap != null) {
                            mMapOfTargetsWaitingForBitmap.remove(targetWaitingForBitmap);
                        }

                        if (urlIndex != null) {
                            mMapOfUrlsWithoutTarget.remove(urlIndex);
                        }

                        addBitmapToMemoryCache(urlOfTheImageRequested, bitmapDownloaded);
                        mListener.imageHasBeenDownloaded(targetWaitingForBitmap, bitmapDownloaded);
                    }
                }
            });
        } catch (IOException ioe) {
            mListener.imageWasNotDownloaded(targetWaitingForBitmap, ioe.getMessage());
        }
    }

    private Bitmap downloadBitmap(String url) throws IOException {
        if (url == null){
            return null;
        }

        byte[] bitmapBytes = getImageBytes(url);
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
    }


    private byte[] getImageBytes(String urlSpec) throws IOException {

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " + urlSpec);
            }

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private void setupLocalCache() {
        mLocalCacheDir = new File(Environment.
                        getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LOCAL_CACHE_DIR_NAME);

        if (!mLocalCacheDir.exists()){
            if (!mLocalCacheDir.mkdirs()) {
                Log.d(LOCAL_CACHE_DIR_NAME, "Oops! Failed create " + LOCAL_CACHE_DIR_NAME + " directory");
            }
        }
    }


    private Uri addBitmapToLocalCache(Bitmap bitmap) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(mLocalCacheDir.getPath() + File.separator + "ALBUM_IMAGE_" + timeStamp + ".png");
        Boolean error = false;

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (out != null){
                    out.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Uri.fromFile(file);
    }

    private Bitmap getBitmapFromLocalCache(String imageUri) {
        Uri uri = Uri.parse(imageUri);
        Bitmap bitmap = null;
        try {
            File file = new File(uri.getPath());
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


    private static Boolean removeImageFromLocalCache(String imageUri) {
        Uri uri = Uri.parse(imageUri);
        File file = new File(uri.getPath());
        return file.exists() && file.delete();
    }


    private void setupMemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize =  maxMemory / 4;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than number of items.
                return super.sizeOf(key, value) / 1024;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null) {
            mCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return mCache.get(key);
    }



}
