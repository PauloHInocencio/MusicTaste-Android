package com.poolapps.musictaste.database;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.poolapps.musictaste.model.MusicItem;

public class MusicsController {

    private static final String ACTION_CREATE = "create";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";


    public static void insertMusic(Context context, MusicItem item) {
        new CRUDTask(item, MusicContract.Music.CONTENT_URI, ACTION_CREATE).execute(context);
    }

    public static void updateMusic(Context context, MusicItem item) {
        Uri uri = ContentUris.withAppendedId(MusicContract.Music.CONTENT_URI, item.id);
        new CRUDTask(item, uri, ACTION_UPDATE).execute(context);
    }

    public static void deleteMusic(Context context, MusicItem item){
        Uri uri = ContentUris.withAppendedId(MusicContract.Music.CONTENT_URI, item.id);
        new CRUDTask(item, uri, ACTION_DELETE).execute(context);
    }



    public static void deleteAllDisliked() {
        String selection = MusicContract.Music.COLUMN_IS_ON_LIKED + " = ?";
        String[] selectionArgs = new String[]{"0"};
        new CRUDTask(MusicContract.Music.CONTENT_URI, ACTION_DELETE, selection, selectionArgs);
    }

    private static class CRUDTask extends AsyncTask<Context, Void, Void> {

        private MusicItem mMusicItem;
        private Uri mUri = null;
        private String mAction = null;
        private String mSelection = null;
        private String[] mSelectionArgs = null;

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


            if (mMusicItem != null) {
                switch (mAction) {
                    case ACTION_CREATE:
                        if (!alreadyExist(c)) {
                            c.getContentResolver().insert(mUri, mMusicItem.toValues());
                        }
                        break;

                    case ACTION_UPDATE:
                        if (alreadyExist(c)){
                            c.getContentResolver().update(mUri, mMusicItem.toValues(), null, null);
                        }
                        break;

                    case ACTION_DELETE:
                        c.getContentResolver().delete(mUri, mSelection, mSelectionArgs);
                        break;

                }
            }

            return null;
        }




        private boolean alreadyExist(Context context) {
            boolean exist = false;
            if (mMusicItem != null) {
                String selection = MusicContract.Music.COLUMN_WEB_ID + " = ?";
                String[] selectionArgs = new String[]{mMusicItem.webId.toString()};

                Cursor cursor = context
                        .getContentResolver()
                        .query(MusicContract.Music.CONTENT_URI, null, selection, selectionArgs, null);

                if (cursor != null) {
                    exist = cursor.getCount() > 0;
                    cursor.close();
                }
            }
            return exist;
        }


    }

}
