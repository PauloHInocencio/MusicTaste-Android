package com.poolapps.musictaste;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment {

    private BottomNavigationView mNavigation;


    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                Fragment fragment = null;
                String title = "";
                switch (item.getItemId()){
                    case R.id.action_match:
                        title = "Match";
                        fragment = MatchMusicsFragment.newInstance();
                        break;

                    case R.id.action_liked:
                        title = "Liked";
                        fragment = LikedMusicsFragment.newInstance();
                        break;
                }

                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.home_fragment_container, fragment);
                transaction.commit();
                return true;
            }
        });

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.home_fragment_container, MatchMusicsFragment.newInstance());
        transaction.commit();

        return v;
    }
}
