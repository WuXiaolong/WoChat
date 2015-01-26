package com.xiaomolongstudio.wochat.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomolongstudio.wochat.R;

public class MeFragment extends Fragment {
	private View mView;
	private Activity mThis;

	@SuppressLint("InlinedApi")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.me, container, false);
		mThis = getActivity();

		return mView;
	}

}
