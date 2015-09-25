package com.wuxiaolong.wochat.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.adapter.ContactsAdapter;
import com.wuxiaolong.wochat.mvp.model.ContactsModel;
import com.wuxiaolong.wochat.mvp.presenter.ContactsPresenter;
import com.wuxiaolong.wochat.mvp.view.ContactsView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment implements ContactsView {

    ContactsPresenter mContactsPresenter;

    @Bind(R.id.mRecyclerView)
    RecyclerView mRecyclerView;
    Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mActivity = getActivity();
        mContactsPresenter = new ContactsPresenter();
        mContactsPresenter.attachView(ContactsFragment.this);
        mContactsPresenter.getRoster();
        Log.d("wxl", "getRoster");
    }

    @Override
    public void rosterList(List<ContactsModel> rosterList) {
        ContactsAdapter contactsAdapter = new ContactsAdapter(getActivity(), rosterList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(contactsAdapter);
    }

    @Override
    public void progressShow() {

    }

    @Override
    public void progressDismiss() {

    }
}
