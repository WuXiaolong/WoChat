package com.xiaomolongstudio.wochat.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.service.XMPPService;

public class AddFriendsAdapter extends BaseAdapter {

	ViewHolder viewHolder;
	List<Map<String, Object>> friendsList;
	XMPPService mXMPPService;
	Context context;

	public AddFriendsAdapter(Context context,
			List<Map<String, Object>> friendsList, XMPPService mXMPPService) {
		this.context = context;
		this.friendsList = friendsList;
		this.mXMPPService = mXMPPService;
	}

	@Override
	public int getCount() {
		return friendsList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.add_friends_item, null);
			viewHolder.nickName = (TextView) convertView
					.findViewById(R.id.nickName);
			viewHolder.ignore = (Button) convertView
					.findViewById(R.id.ignore);
			viewHolder.agree = (Button) convertView.findViewById(R.id.agree);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.nickName.setText(

		Html.fromHtml("<font color=#40659c>"
				+ friendsList.get(position).get("nickName").toString()
				+ "</font>" + "请求添加您为好友")

		);
		final String fromJid = friendsList.get(position).get("fromJid")
				.toString();
		viewHolder.agree.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mXMPPService.createEntry(fromJid, friendsList.get(position)
						.get("nickName").toString());
				mXMPPService.agree(fromJid);
				Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
				friendsList.remove(position);
				notifyDataSetChanged();

			}
		});
		viewHolder.ignore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mXMPPService.removeEntry(fromJid);
				Toast.makeText(context, "忽略成功", Toast.LENGTH_SHORT).show();
				friendsList.remove(position);
				notifyDataSetChanged();

			}
		});
		return convertView;

	}

	class ViewHolder {
		TextView nickName;
		Button agree, ignore;
	}

}
