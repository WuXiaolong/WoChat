package com.wuxiaolong.wochat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeType;
import com.alibaba.mobileim.tribe.IYWTribeService;
import com.alibaba.mobileim.tribe.YWTribeCreationParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class TribeFragment extends Fragment {

    private IYWTribeService mTribeService;
    private YWIMKit mIMKit;
    ListView mListView;
    List<Map<String, String>> mTribeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tribe, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.listView);
        mTribeService = getTribeService();
        getAllTribesFromServer();
//        createTribe(YWTribeType.CHATTING_GROUP);
    }

    public IYWTribeService getTribeService() {
        if (mTribeService == null) {
            mIMKit = LoginSampleHelper.getInstance().getIMKit();
            mTribeService = mIMKit.getTribeService();
        }

        return mTribeService;
    }

    private void getAllTribesFromServer() {
        mTribeService.getAllTribesFromServer(new IWxCallback() {
            @Override
            public void onSuccess(Object... arg0) {
                // 返回值为列表
                Log.e("wxl", "群===" + (ArrayList<YWTribe>) arg0[0]);
                List<YWTribe> ywTribe = (ArrayList<YWTribe>) arg0[0];
                Map<String, String> tribeMap = new HashMap<String, String>();
                List<Map<String, String>> tribeList = new ArrayList<>();
                for (YWTribe tribe : ywTribe) {
                    if (tribe.getTribeType() == YWTribeType.CHATTING_GROUP) {
                        Log.e("wxl", "群===" + tribe.getTribeName());
                        tribeMap.put("tribeId", String.valueOf(tribe.getTribeId()));
                        tribeMap.put("tribeName", tribe.getTribeName());
                        tribeMap.put("tribeNotice", tribe.getTribeNotice());
                        tribeList.add(tribeMap);
                    } else {
                    }
                }
                SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), tribeList, R.layout.tribe_item, new String[]{"tribeName", "tribeNotice"},
                        new int[]{R.id.tribeName, R.id.tribeNotice});
                mListView.setAdapter(simpleAdapter);
            }

            @Override
            public void onError(int code, String info) {
//                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onProgress(int progress) {

            }
        });
    }

    private void createTribe(final YWTribeType type) {
        List<String> users = new ArrayList<>();
        users.add(mIMKit.getIMCore().getLoginUserId());
        YWTribeCreationParam param = new YWTribeCreationParam();
        param.setTribeType(type);
        param.setTribeName("剩者为王③群");
        param.setNotice("Android技术交流群，QQ群：370527306 ");
        param.setUsers(users);
        mTribeService.createTribe(new IWxCallback() {
            @Override
            public void onSuccess(Object... result) {
                if (result != null && result.length > 0) {
                    YWTribe tribe = (YWTribe) result[0];
                    if (type.equals(YWTribeType.CHATTING_TRIBE)) {
                        Toast.makeText(getActivity(), "创建群组成功！",
                                Toast.LENGTH_SHORT).show();
                    } else {
//                        Notification.showToastMsg(EditTribeInfoActivity.this, "创建讨论组成功！");
                    }
                }
            }

            @Override
            public void onError(int code, String info) {
                Toast.makeText(getActivity(), "创建讨论组失败，code = " + code + ", info = " + info,
                        Toast.LENGTH_SHORT).show();
                Log.e("wxl", "创建讨论组失败，code = " + code + ", info = " + info);
//                Notification.showToastMsg(EditTribeInfoActivity.this, "创建讨论组失败，code = " + code + ", info = " + info);
            }

            @Override
            public void onProgress(int progress) {

            }
        }, param);
    }
}
