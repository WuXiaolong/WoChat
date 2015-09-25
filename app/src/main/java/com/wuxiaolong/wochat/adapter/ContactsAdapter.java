package com.wuxiaolong.wochat.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.mvp.model.ContactsModel;

import java.util.List;

/**
 * Created by WuXiaolong on 2015/9/25.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    List<ContactsModel> mRosterList;
    Activity mActivity;

    public ContactsAdapter(Activity activity, List<ContactsModel> rosterList) {
        this.mRosterList = rosterList;
        this.mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.userNickName.setText(mRosterList.get(position).getUserNickName());
        holder.userAvatar.setImageDrawable(mRosterList.get(position).getUserAvatar());
    }

    @Override
    public int getItemCount() {
        return mRosterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNickName;
        ImageView userAvatar;

        public ViewHolder(View itemView) {
            super(itemView);
            userNickName = (TextView) itemView.findViewById(R.id.userNickName);
            userAvatar = (ImageView) itemView.findViewById(R.id.userAvatar);
        }
    }
}
