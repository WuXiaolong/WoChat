package com.xiaomolongstudio.wochat.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.ui.ChatRoomActivity;

public class ChatRoomListFragment extends Fragment {
	private View mView;
	private Activity mThis;

	@SuppressLint("InlinedApi")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.chat_room_list, container, false);
		mThis = getActivity();
		initView();
		return mView;
	}

	private void initView() {
		mView.findViewById(R.id.layout1).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// mListView.setItemChecked(position, true);
						Intent intent = new Intent(mThis,
								ChatRoomActivity.class);
						intent.putExtra("flag", "314447894");
						startActivity(intent);
					}
				});

	}

	List<Map<String, Object>> chatRoomListData;

	private void chatRoomListData() {
		chatRoomListData = new ArrayList<Map<String, Object>>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", "剩者为王");
		map.put("info", "google 1");
		chatRoomListData.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "G2");
		map.put("info", "google 2");
		chatRoomListData.add(map);

		map = new HashMap<String, Object>();
		map.put("title", "G3");
		map.put("info", "google 3");
		chatRoomListData.add(map);

	}
}
