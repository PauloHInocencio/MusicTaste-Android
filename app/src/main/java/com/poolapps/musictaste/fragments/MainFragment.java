package com.poolapps.musictaste.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.poolapps.musictaste.R;

public class MainFragment extends Fragment {


    private final static int CODE_REQUEST_EXTERNAL_STORAGE_PERMISSION = 0;
    private static final String STATE_CURRENT_FRAGMENT = "current_fragment";


    private BottomNavigationView mNavigation;
    private Fragment mCurrentFragment;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkReadExternalStoragePermission();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.main_fragment, container, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Match");

        mNavigation = (BottomNavigationView) v.findViewById(R.id.bottom_navigation);
        mNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Fragment fragment = null;
                String title = "";
                switch (item.getItemId()){
                    case R.id.action_match:
                        title = "Match";
                        mCurrentFragment = MatchMusicsFragment.newInstance();
                        break;

                    case R.id.action_liked:
                        title = "Liked";
                        mCurrentFragment = LikedMusicsFragment.newInstance();
                        break;
                }

                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.home_fragment_container, mCurrentFragment);
                transaction.commit();
                return true;
            }
        });

        if (savedInstanceState != null) {
            mCurrentFragment = getChildFragmentManager().getFragment(savedInstanceState, STATE_CURRENT_FRAGMENT);
        } else {
            mCurrentFragment = MatchMusicsFragment.newInstance();
        }


        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment_container, mCurrentFragment);
        transaction.commit();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getChildFragmentManager().putFragment(outState, STATE_CURRENT_FRAGMENT, mCurrentFragment);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_REQUEST_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Ready to store images.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(),
                            "Permission to store images was denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkReadExternalStoragePermission() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showMessageOKCancel("O app armazena imagens dos albúns curtidos localmente." +
                                    " Necessário permitir acesso de armazenamento externo.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                            CODE_REQUEST_EXTERNAL_STORAGE_PERMISSION);
                                }
                            });
                    return;
                }

                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        CODE_REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}


