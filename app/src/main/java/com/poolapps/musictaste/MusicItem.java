package com.poolapps.musictaste;


import org.json.JSONException;
import org.json.JSONObject;

public class MusicItem {

    public String musicName;
    public String artistName;
    public String albumName;
    public String image_url;


    public static MusicItem create(JSONObject json) throws JSONException{

        MusicItem item = new MusicItem();
        item.musicName = json.getString("trackName");
        item.artistName = json.getString("artistName");
        item.albumName = json.getString("collectionName");
        item.image_url = json.getString("artworkUrl60");

        return item;
    }
}
