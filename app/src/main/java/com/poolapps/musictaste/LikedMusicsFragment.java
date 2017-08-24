package com.poolapps.musictaste;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class LikedMusicsFragment extends Fragment {

    public static LikedMusicsFragment newInstance() {
        return new LikedMusicsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.liked_fragment, container, false);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkInternetConnection();
    }


    private void checkInternetConnection() {
        boolean isConnected = Utilities.checkInternetConnection(getContext());
        if (isConnected) {
            //Toast.makeText(getContext(), "Está conectado", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(getContext(), "Não está conectado", Toast.LENGTH_SHORT).show();
        }
    }
}
