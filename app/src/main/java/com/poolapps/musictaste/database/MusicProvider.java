package com.poolapps.musictaste.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MusicProvider extends ContentProvider {


    private static final int CODE_ALL_MUSICS = 0;
    private static final int CODE_MUSIC_ID = 1;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        URI_MATCHER.addURI(MusicContract.AUTHORITY, MusicContract.Music.PATH, CODE_ALL_MUSICS);
        URI_MATCHER.addURI(MusicContract.AUTHORITY, MusicContract.Music.PATH + "/#", CODE_MUSIC_ID);
    }

    private DBHelper dbHelper;


    @Override
    public boolean onCreate() {
        dbHelper = DBHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        sortOrder = (sortOrder == null ? MusicContract.Music._ID : sortOrder);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        final int code = URI_MATCHER.match(uri);
        switch (code){
            case CODE_ALL_MUSICS:
                cursor = db.query(MusicContract.Music.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_MUSIC_ID:
                if (selection == null){
                    selection = BaseColumns._ID + " = " + uri.getLastPathSegment();
                } else {
                    throw new IllegalArgumentException("selection must be null when" +
                            "specifying ID as part of uri");
                }

                cursor = db.query(MusicContract.Music.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        long id;
        final int code = URI_MATCHER.match(uri);
        switch (code){
            case CODE_ALL_MUSICS:
                id = dbHelper
                        .getWritableDatabase()
                        .insert(MusicContract.Music.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int rowCont;

        final int code = URI_MATCHER.match(uri);
        switch (code){
            case CODE_ALL_MUSICS:
                rowCont = dbHelper
                        .getWritableDatabase()
                        .delete(MusicContract.Music.TABLE_NAME, selection, selectionArgs);
                break;

            case CODE_MUSIC_ID:
                if (selection == null && selectionArgs == null){
                    selection = BaseColumns._ID + " = ?";
                    selectionArgs = new String[]{uri.getLastPathSegment()};
                    rowCont = dbHelper
                            .getWritableDatabase()
                            .delete(MusicContract.Music.TABLE_NAME, selection, selectionArgs);
                } else {
                    throw new IllegalArgumentException("Selection must be null when"
                            + "specifying ID as part of uri.");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid uri: " + uri);
        }
        return rowCont;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String whereClause, String[] whereArgs) {

        int rowCount;

        final int code = URI_MATCHER.match(uri);
        switch (code){
            case CODE_ALL_MUSICS:
                rowCount = dbHelper
                        .getWritableDatabase()
                        .update(MusicContract.Music.TABLE_NAME, values, whereClause, whereArgs);
                break;

            case CODE_MUSIC_ID:
                if (whereClause == null && whereArgs == null){
                    whereClause = BaseColumns._ID + " = ?";
                    whereArgs = new String[]{ uri.getLastPathSegment() };
                } else {
                    throw new IllegalArgumentException("whereClause must be null when " +
                            "specifying ID as part of uri.");
                }

                rowCount = dbHelper
                        .getWritableDatabase()
                        .update(MusicContract.Music.TABLE_NAME, values, whereClause, whereArgs);

                break;
            default:
                throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        return rowCount;
    }

}
