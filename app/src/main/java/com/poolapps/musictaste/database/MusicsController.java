package com.poolapps.musictaste.database;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.poolapps.musictaste.model.MusicItem;

public class MusicsController {

    private static final String ACTION_CREATE = "create";
    private static final String ACTION_READ = "read";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";

    private static MusicControllerListener sListener;

    public interface MusicControllerListener {
        void onResultOfTheOperation(boolean result);
    }

    public static void setListener(MusicControllerListener listener){
        sListener = listener;
    }

    public static void addMusic(Context c, MusicItem item) {
        new CRUDTask(item, MusicContract.Music.CONTENT_URI, ACTION_CREATE).execute(c);
    }

    public static void updateMusic(Context c, MusicItem item) {
        Uri uri = ContentUris.withAppendedId(MusicContract.Music.CONTENT_URI, item.id);
        new CRUDTask(item, uri, ACTION_UPDATE).execute(c);
    }

    public static void deleteMusic(Context c, MusicItem item){
        Uri uri = ContentUris.withAppendedId(MusicContract.Music.CONTENT_URI, item.id);
        new CRUDTask(item, uri, ACTION_DELETE).execute(c);
    }

    public static void checkIfItemAlreadyExist(MusicItem item) {
        String selection = MusicContract.Music.COLUMN_WEB_ID + " = ?";
        String[] selectionArg = new String[]{item.webId.toString()};
        new CRUDTask(MusicContract.Music.CONTENT_URI, ACTION_READ, selection, selectionArg);
    }

    public static void deleteAllDisliked() {
        String selection = MusicContract.Music.COLUMN_IS_ON_LIKED + " = ?";
        String[] selectionArgs = new String[]{"0"};
        new CRUDTask(MusicContract.Music.CONTENT_URI, ACTION_DELETE, selection, selectionArgs);
    }

    private static class CRUDTask extends AsyncTask<Context, Void, Boolean> {

        private MusicItem mMusicItem;
        private Uri mUri;
        private String mAction;
        private String mSelection;
        private String[] mSelectionArgs;

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
        protected Boolean doInBackground(Context... contexts) {
            Context c = contexts[0];
            int count;

            if (mMusicItem != null) {
                switch (mAction) {
                    case ACTION_CREATE:
                        return insertItem(c);

                    case ACTION_UPDATE:
                        count = c.getContentResolver().update(mUri, mMusicItem.toValues(), null, null);
                        return count > 0;

                    case ACTION_DELETE:
                        count = c.getContentResolver().delete(mUri, null, null);
                        return count > 0;

                    default:
                        return false;
                }
            }
            else if (mSelection != null && mSelectionArgs != null) {
                switch (mAction) {
                    case ACTION_DELETE :
                        count = c.getContentResolver()
                                .delete(mUri, mSelection, mSelectionArgs);
                        return count > 0;

                    case ACTION_READ:
                        return read(c);

                    default:
                        return false;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            sListener.onResultOfTheOperation(result);
        }

        private boolean insertItem(Context context) {
           Uri uri = context.getContentResolver().insert(mUri, mMusicItem.toValues());
           return (uri != null && !uri.getLastPathSegment().equals("-1") ) ;
        }

        private boolean read(Context context) {
            boolean canRead = false;
            Cursor cursor = context
                    .getContentResolver()
                    .query(mUri, null, mSelection, mSelectionArgs, null);

            if (cursor != null) {
                canRead = cursor.getCount() > 0;
                cursor.close();
            }
            return canRead;
        }
    }

}
