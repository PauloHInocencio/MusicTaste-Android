package com.poolapps.musictaste.database;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class MusicContract {

    public static final String AUTHORITY = "com.poolapps.musictaste.musicprovider";
    public static final Uri AUTHORITY_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .build();


    public interface Music extends BaseColumns {

        String PATH = "music";
        Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);

        String TABLE_NAME = PATH;
        String COLUMN_WEB_ID = "web_id";
        String COLUMN_MUSIC_NAME = "music_name";
        String COLUMN_ARTIST_NAME = "artist_name";
        String COLUMN_ALBUM_NAME = "album_name";
        String COLUMN_ALBUM_IMAGE_URL = "album_image_url";
        String COLUMN_ALBUM_IMAGE_URI = "album_image_uri";
        String COLUMN_IS_ON_LIKED = "is_on_liked";

        String SQL_CREATE =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_WEB_ID + " INTEGER NOT NULL, " +
                        COLUMN_MUSIC_NAME   + " TEXT NOT NULL, " +
                        COLUMN_ARTIST_NAME  + " TEXT, " +
                        COLUMN_ALBUM_NAME  + " TEXT," +
                        COLUMN_ALBUM_IMAGE_URL  + " TEXT NOT NULL, " +
                        COLUMN_ALBUM_IMAGE_URI  + " TEXT," +
                        COLUMN_IS_ON_LIKED  + " INTEGER" +
                        ");";

        String SQL_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
