package com.wuxiaolong.wochat.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.xmpp.XMPPService;

import org.jivesoftware.smackx.muc.MultiUserChat;


public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {
    private MultiUserChat mMultiUserChat = null;
    private String roomName = "", roomId = "";
    private EditText editText;
    private ListView listView;
//    private ChatRoomAdapter chatRoomAdapter = null;
    private android.os.Message msg;
    // private IDanmakuView mDanmakuView;
    private boolean isDanMu = false;
    private LinearLayout netFailedLayout;
    private TextView netFailedTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        int createRoom = XMPPService.getInstance().createRoom("15261589767", "123456", "roomId", "roomDesc");
        Log.d("wxl", "createRoom=" + createRoom);
        if (createRoom == 0 || createRoom == 1) {
            mMultiUserChat = XMPPService.getInstance().joinRoom("15261589767", "123456", "roomId");
        }
        initView();
    }

    public void initView() {

        listView = (ListView) findViewById(R.id.listView);
//        netFailedTxt = (TextView) findViewById(R.id.netFailedTxt);
//        netFailedLayout = (LinearLayout) findViewById(R.id.netFailedLayout);
        findViewById(R.id.send).setOnClickListener(this);
        editText = (EditText) findViewById(R.id.editText);
        // mDanmakuView = (DanmakuSurfaceView) findViewById(R.id.sv_danmaku);
        editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == KeyEvent.ACTION_DOWN
                        || actionId == EditorInfo.IME_ACTION_SEND) {
                    if (editText.getText().toString().length() > 0) {
//                        sendMessage(editText.getText().toString());
                    }
                }
                return true;
            }
        });

    }

//    public class ChatRoomAdapter extends BaseAdapter {
//
//        List<IMMessage> iMMessages;
//        ViewHolder viewHolder;
//        Activity mActivity;
//
//        public ChatRoomAdapter(Activity activity, List<IMMessage> iMMessages) {
//            this.mActivity = activity;
//            this.iMMessages = iMMessages;
//        }
//
//        public List<IMMessage> getiMMessages() {
//            return iMMessages;
//        }
//
//        public void setiMMessages(List<IMMessage> iMMessages) {
//            this.iMMessages = iMMessages;
//        }
//
//        @Override
//        public int getCount() {
//            return iMMessages.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return position;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @SuppressLint({"InflateParams", "NewApi"})
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                viewHolder = new ViewHolder();
//                convertView = LayoutInflater.from(mActivity).inflate(
//                        R.layout.chat_room_item, null);
//                viewHolder.leftChatContent = (TextView) convertView
//                        .findViewById(R.id.leftChatContent);
//                viewHolder.rightChatContent = (TextView) convertView
//                        .findViewById(R.id.rightChatContent);
//                viewHolder.leftUserImg = (ImageView) convertView
//                        .findViewById(R.id.leftUserImg);
//                viewHolder.rightUserImg = (ImageView) convertView
//                        .findViewById(R.id.rightUserImg);
//                viewHolder.leftLayout = (LinearLayout) convertView
//                        .findViewById(R.id.leftLayout);
//                viewHolder.rightLayout = (RelativeLayout) convertView
//                        .findViewById(R.id.rightLayout);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//            if ((AppUtils.phoneId(mActivity)).equals(iMMessages.get(position)
//                    .getMsg_from().toString())) {
//                viewHolder.rightLayout.setVisibility(View.VISIBLE);
//                viewHolder.leftLayout.setVisibility(View.GONE);
//                viewHolder.rightChatContent.setText(iMMessages.get(position)
//                        .getChat_content().toString());
//                Log.d("wxl", "getAvatar==" + iMMessages.get(position).getAvatar());
//                if (iMMessages.get(position).getAvatar() == null) {
//                    viewHolder.rightUserImg.setBackgroundResource(R.drawable.moren);
//                } else {
//                    viewHolder.rightUserImg.setBackground(iMMessages.get(position)
//                            .getAvatar());
//                }
//                viewHolder.rightUserImg.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
////                        Intent intent = new Intent(mActivity,
////                                FriendMsgActivity.class);
////                        intent.putExtra("form", iMMessages.get(position)
////                                .getMsg_from().toString());
////
////                        mActivity.startActivity(intent);
//                    }
//                });
//            } else {
//                viewHolder.rightLayout.setVisibility(View.GONE);
//                viewHolder.leftLayout.setVisibility(View.VISIBLE);
//                viewHolder.leftChatContent.setText(iMMessages.get(position)
//                        .getChat_content().toString());
//
//                if (iMMessages.get(position).getAvatar() == null) {
//                    viewHolder.leftUserImg.setBackgroundResource(R.drawable.moren);
//                } else {
//                    viewHolder.leftUserImg.setBackground((Drawable) iMMessages.get(
//                            position).getAvatar());
//                }
//                viewHolder.leftUserImg.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(mActivity,
//                                FriendMsgActivity.class);
//                        intent.putExtra("form", iMMessages.get(position)
//                                .getMsg_from().toString());
//                        mActivity.startActivity(intent);
//                    }
//                });
//            }
//            return convertView;
//        }
//
//        class ViewHolder {
//            TextView leftChatContent, rightChatContent;
//            ImageView leftUserImg, rightUserImg;
//            LinearLayout leftLayout;
//            RelativeLayout rightLayout;
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
