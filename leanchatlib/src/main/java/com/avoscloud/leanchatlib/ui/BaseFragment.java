package com.avoscloud.leanchatlib.ui;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    public Activity mActivity;
    AppCompatActivity mAppCompatActivity;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = getActivity();
        ButterKnife.bind(this, view);
    }


    public void showToast(String content) {
        Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show();
    }

    protected static boolean filterException(Exception e) {
        if (e != null) {
            return false;
        } else {
            return true;
        }
    }
}
