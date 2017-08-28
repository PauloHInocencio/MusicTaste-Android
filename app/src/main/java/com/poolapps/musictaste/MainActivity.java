package com.poolapps.musictaste;

import android.support.v4.app.Fragment;

import com.poolapps.musictaste.fragments.MainFragment;
import com.poolapps.musictaste.utils.SingleFragmentActivity;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return MainFragment.newInstance();
    }
}
