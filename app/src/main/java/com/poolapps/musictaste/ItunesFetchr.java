package com.poolapps.musictaste;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ItunesFetchr {

    private static final String TAG = ItunesFetchr.class.getSimpleName();

    private static final Uri ENDPOINT = Uri
            .parse("https://itunes.apple.com/search")
            .buildUpon()
            .appendQueryParameter("media", "music")
            .appendQueryParameter("entity", "musicTrack")
            .appendQueryParameter("attribute", "songTerm")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {

        //create URL
        URL url = new URL(urlSpec);

        //create a connection object pointed at the URL.
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Connecting to the endpoint.
            InputStream in = connection.getInputStream();

            // check connection response
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " + urlSpec);
            }
            // get all bytes from the connection response.
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();

            // return bytes from the connection response.
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }


    public List<MusicItem> searchMusic(String musicName){
        String url = ENDPOINT.buildUpon()
                .appendQueryParameter("term", musicName)
                .build()
                .toString();
        return downloadMusicItems(url);
    }


    private List<MusicItem> downloadMusicItems(String url) {
        List<MusicItem> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    private void parseItems(List<MusicItem> items, JSONObject jsonBody)
            throws IOException, JSONException  {

        JSONArray resultsJsonArray = jsonBody.getJSONArray("results");

        for (int i = 0; i < resultsJsonArray.length(); i++){

            MusicItem item = MusicItem.create(resultsJsonArray.getJSONObject(i));
            items.add(item);
        }
    }

}


