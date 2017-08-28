package com.poolapps.musictaste.model;


import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import static android.provider.BaseColumns._ID;
import static com.poolapps.musictaste.database.MusicContract.Music.COLUMN_ALBUM_IMAGE_URI;
import static com.poolapps.musictaste.database.MusicContract.Music.COLUMN_ALBUM_IMAGE_URL;
import static com.poolapps.musictaste.database.MusicContract.Music.COLUMN_ALBUM_NAME;
import static com.poolapps.musictaste.database.MusicContract.Music.COLUMN_ARTIST_NAME;
import static com.poolapps.musictaste.database.MusicContract.Music.COLUMN_IS_ON_LIKED;
import static com.poolapps.musictaste.database.MusicContract.Music.COLUMN_MUSIC_NAME;
import static com.poolapps.musictaste.database.MusicContract.Music.COLUMN_WEB_ID;

public class MusicItem implements Parcelable {

    private final static String TAG = MusicItem.class.getSimpleName();

    public Integer id;
    public Integer webId;
    public String musicName;
    public String artistName;
    public String albumName;
    public String albumImageUrl;
    public String albumImageUri;
    public boolean isOnLiked;


    public static MusicItem create(JSONObject json) throws JSONException{

        MusicItem item = new MusicItem();
        item.webId      = json.has("trackId") ? json.getInt("trackId") : null;
        item.musicName  = json.has("trackName")  ? json.getString("trackName") : null;
        item.artistName = json.has("artistName") ? json.getString("artistName") : null;
        item.albumName  = json.has("collectionName") ? json.getString("collectionName") : null;
        String url = json.has("artworkUrl60") ? json.getString("artworkUrl60") : null;

        if (item.webId == null || item.musicName == null || url == null) {
            return null;
        }

        String urlBody = url.substring(0, url.lastIndexOf("/") + 1);
        item.albumImageUrl = urlBody + "150x150.jpg";
        item.isOnLiked = false;

        return item;
    }

    public static MusicItem create(Cursor c) {

        MusicItem item = new MusicItem();
        item.id = c.getInt(c.getColumnIndex(_ID));
        item.webId = c.getInt(c.getColumnIndex(COLUMN_WEB_ID));
        item.musicName = c.getString(c.getColumnIndex(COLUMN_MUSIC_NAME));
        item.artistName = c.getString(c.getColumnIndex(COLUMN_ARTIST_NAME));
        item.albumName = c.getString(c.getColumnIndex(COLUMN_ALBUM_NAME));
        item.albumImageUrl = c.getString(c.getColumnIndex(COLUMN_ALBUM_IMAGE_URL));
        item.albumImageUri = c.getString(c.getColumnIndex(COLUMN_ALBUM_IMAGE_URI));
        item.isOnLiked = c.getInt(c.getColumnIndex(COLUMN_IS_ON_LIKED)) == 1;
        return item;
    }


    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(_ID, this.id);
        values.put(COLUMN_WEB_ID, this.webId);
        values.put(COLUMN_MUSIC_NAME, this.musicName);
        values.put(COLUMN_ARTIST_NAME, this.artistName);
        values.put(COLUMN_ALBUM_NAME, this.albumName);
        values.put(COLUMN_ALBUM_IMAGE_URL, this.albumImageUrl);
        values.put(COLUMN_ALBUM_IMAGE_URI, this.albumImageUri);
        values.put(COLUMN_IS_ON_LIKED, this.isOnLiked ? 1 : 0);
        return values;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeValue(this.webId);
        dest.writeString(this.musicName);
        dest.writeString(this.artistName);
        dest.writeString(this.albumName);
        dest.writeString(this.albumImageUrl);
        dest.writeString(this.albumImageUri);
        dest.writeByte(this.isOnLiked ? (byte) 1 : (byte) 0);
    }

    public MusicItem() {
    }

    protected MusicItem(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.webId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.musicName = in.readString();
        this.artistName = in.readString();
        this.albumName = in.readString();
        this.albumImageUrl = in.readString();
        this.albumImageUri = in.readString();
        this.isOnLiked = in.readByte() != 0;
    }

    public static final Parcelable.Creator<MusicItem> CREATOR = new Parcelable.Creator<MusicItem>() {
        @Override
        public MusicItem createFromParcel(Parcel source) {
            return new MusicItem(source);
        }

        @Override
        public MusicItem[] newArray(int size) {
            return new MusicItem[size];
        }
    };
}
