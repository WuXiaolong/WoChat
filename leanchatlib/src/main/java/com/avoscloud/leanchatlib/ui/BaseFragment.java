package com.avoscloud.leanchatlib.ui;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avoscloud.leanchatlib.R;

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
        setHasOptionsMenu(true);
    }

    public Toolbar initToolbar(int toolbarId, int title) {
        AppCompatActivity mAppCompatActivity = (AppCompatActivity) mActivity;
        Toolbar toolbar = (Toolbar) mAppCompatActivity.findViewById(toolbarId);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        mAppCompatActivity.setSupportActionBar(toolbar);
        toolbarTitle.setText(title);
        ActionBar actionBar = mAppCompatActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
    }

    public Toolbar initToolbar(int toolbarId, CharSequence title) {
        mAppCompatActivity = (AppCompatActivity) mActivity;
        Toolbar toolbar = (Toolbar) mAppCompatActivity.findViewById(toolbarId);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        mAppCompatActivity.setSupportActionBar(toolbar);
        toolbarTitle.setText(title);
        ActionBar actionBar = mAppCompatActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        return toolbar;
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
