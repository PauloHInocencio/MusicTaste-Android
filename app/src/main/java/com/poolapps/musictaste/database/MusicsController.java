package com.poolapps.musictaste.database;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.poolapps.musictaste.model.MusicItem;

import java.util.ArrayList;
import java.util.List;

public class MusicsController {

    private static final String ACTION_CREATE = "create";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";

    private static Callback mCallback;

    public interface Callback {
        void finishOperation(String action, int totalOperations);
    }

    public static void insertMusic(Context context, MusicItem item, Callback callback) {
        mCallback = callback;
        new CRUDTask(item, MusicContract.Music.CONTENT_URI, ACTION_CREATE).execute(context);
    }

    public static void insertMusic(Context context, List<MusicItem> items, Callback callback) {
        mCallback = callback;
        new CRUDTask(MusicContract.Music.CONTENT_URI, ACTION_CREATE, items).execute(context);
    }

    public static void updateMusic(Context context, MusicItem item, Callback callback) {
        mCallback = callback;
        Uri uri = ContentUris.withAppendedId(MusicContract.Music.CONTENT_URI, item.id);
        new CRUDTask(item, uri, ACTION_UPDATE).execute(context);
    }

    public static void deleteMusic(Context context, MusicItem item, Callback callback){
        mCallback = callback;
        Uri uri = ContentUris.withAppendedId(MusicContract.Music.CONTENT_URI, item.id);
        new CRUDTask(item, uri, ACTION_DELETE).execute(context);
    }



    public static void deleteAllDisliked(Context context) {
        String selection = MusicContract.Music.COLUMN_IS_ON_LIKED + " = ?";
        String[] selectionArgs = new String[]{"0"};
        new CRUDTask(MusicContract.Music.CONTENT_URI, ACTION_DELETE, selection, selectionArgs).execute(context);
    }

    private static class CRUDTask extends AsyncTask<Context, Void, Void> {

        private MusicItem mMusicItem;
        private List<MusicItem> mItems = new ArrayList<>();
        private Uri mUri = null;
        private String mAction = null;
        private String mSelection = null;
        private String[] mSelectionArgs = null;

        private int totalOperations;


        CRUDTask(Uri uri, String action, List<MusicItem> items) {
            this(null, uri, action);
            mItems = items;

        }

        CRUDTask(MusicItem item, Uri uri, String action) {
            mMusicItem = item;
            mUri = uri;
            mAction = action;
        }

        CRUDTask(Uri uri, String action, String selection, String[] selectionArgs) {
            this(null, uri, action);
            mSelection = selection;
            mSelectionArgs = selectionArgs;
        }


        @Override
        protected Void doInBackground(Context... contexts) {
            Context c = contexts[0];
            totalOperations = 0;
            switch (mAction) {
                case ACTION_CREATE:
                    Uri uri;
                    if (mMusicItem != null && !alreadyExist(c, mMusicItem)) {
                       uri = c.getContentResolver().insert(mUri, mMusicItem.toValues());
                       if (uri != null) {
                           totalOperations = uri.getLastPathSegment().equals("-1") ? 0 : 1;
                       }
                    }
                    else if (mItems.size() > 0) {
                        for (MusicItem item : mItems)  {
                            if (!alreadyExist(c, item)) {
                                uri = c.getContentResolver().insert(mUri, item.toValues());
                                if (uri != null) {
                                    totalOperations += uri.getLastPathSegment().equals("-1") ? 0 : 1;
                                }
                            }
                        }
                    }
                    break;

                case ACTION_UPDATE:
                    if (mMusicItem != null && alreadyExist(c, mMusicItem)){
                        totalOperations = c.getContentResolver().update(mUri, mMusicItem.toValues(), null, null);
                    }
                    break;

                case ACTION_DELETE:
                    totalOperations = c.getContentResolver().delete(mUri, mSelection, mSelectionArgs);
                    break;

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mCallback == null) return;
            mCallback.finishOperation(mAction, totalOperations);
        }

        private boolean alreadyExist(Context context, MusicItem item) {
            boolean exist = false;
            String selection = MusicContract.Music.COLUMN_WEB_ID + " = ?";
            String[] selectionArgs = new String[]{item.webId.toString()};

            Cursor cursor = context
                    .getContentResolver()
                    .query(MusicContract.Music.CONTENT_URI, null, selection, selectionArgs, null);

            if (cursor != null) {
                exist = cursor.getCount() > 0;
                cursor.close();
            }

            return exist;
        }


    }

}
