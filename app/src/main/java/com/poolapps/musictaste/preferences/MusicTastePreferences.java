package com.poolapps.musictaste.preferences;


import android.content.Context;
import android.preference.PreferenceManager;

public class MusicTastePreferences {
    private static final String PREF_SEARCH_QUERY = "search_query";


    public static void setSearchQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static String getSearchQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }
}
