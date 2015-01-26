package com.xiaomolongstudio.wochat.ui;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.adapter.MyFragmentPagerAdapter;
import com.xiaomolongstudio.wochat.fragment.ChatRoomListFragment;
import com.xiaomolongstudio.wochat.fragment.FriendsFragment;
import com.xiaomolongstudio.wochat.fragment.MeFragment;
import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;
import com.xiaomolongstudio.wochat.xmpp.XMPPBroadcastReceiver.EventHandler;

public class MainActivity extends BaseActivity implements EventHandler {
	private XMPPService mXMPPService = null;
	private ViewPager mViewPager;
	private ArrayList<Fragment> fragmentsList;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private TextView me, chatRoom, friends;// 页卡头标
	private RadioGroup mRadioGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.bindXMPPService(AppConfig.MAIN_ACTION, mServiceConnection);
		setContentView(R.layout.activity_main);
		initView();
		initTextView();
		initViewPager();
	}

	private void initView() {

		mViewPager = (ViewPager) findViewById(R.id.mViewPager);
		mRadioGroup = (RadioGroup) findViewById(R.id.mRadioGroup);
	}

	/**
	 * 初始化头标
	 */
	private void initTextView() {
		me = (TextView) findViewById(R.id.me);
		chatRoom = (TextView) findViewById(R.id.chat_room);
		friends = (TextView) findViewById(R.id.friends);
		setTextViewSelected(0);
		chatRoom.setOnClickListener(new MyOnClickListener(0));
		friends.setOnClickListener(new MyOnClickListener(1));
		me.setOnClickListener(new MyOnClickListener(2));
	}

	private void initViewPager() {
		fragmentsList = new ArrayList<Fragment>();

		fragmentsList.add(new ChatRoomListFragment());
		fragmentsList.add(new FriendsFragment());
		fragmentsList.add(new MeFragment());

		mViewPager.setAdapter(new MyFragmentPagerAdapter(
				getSupportFragmentManager(), fragmentsList));
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mViewPager.setCurrentItem(index);
		}
	};

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		int two = one * 2;// 页卡1 -> 页卡3 偏移量

		@Override
		public void onPageSelected(int index) {
			currIndex = index;
			switch (index) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			}
			setTextViewSelected(index);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * 页卡选中颜色变化
	 * 
	 * @param index
	 */
	private void setTextViewSelected(int index) {
		for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
			if (index == i) {
				mRadioGroup.getChildAt(i).setSelected(true);
			} else {
				mRadioGroup.getChildAt(i).setSelected(false);
			}

		}
	}

	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXMPPService = ((XMPPService.XXBinder) service).getService();
			mXMPPService
					.registerConnectionStatusCallback(new IConnectionStatusCallback() {

						@Override
						public void connectionStatusChanged(int connectedState,
								String reason) {

							switch (connectedState) {
							case XMPPService.CONNECTED:
								break;
							case XMPPService.CONNECTING:
								break;
							case XMPPService.DISCONNECTED:
								Toast.makeText(MainActivity.this, reason,
										Toast.LENGTH_SHORT).show();
								break;

							default:
								break;
							}

						}
					});
			// 开始连接xmpp服务器

			if (!mXMPPService.isAuthenticated()) {
				String userName = PreferenceUtils.getPrefString(
						MainActivity.this, PreferenceConstants.USER_NAME, "");
				String userPassword = PreferenceUtils.getPrefString(
						MainActivity.this, PreferenceConstants.PASSWORD, "");
				if (!TextUtils.isEmpty(userName)
						&& !TextUtils.isEmpty(userPassword))
					mXMPPService.login(userName, userPassword);
			} else {
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXMPPService.unRegisterConnectionStatusCallback();
			mXMPPService = null;
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNetChange() {
		// TODO Auto-generated method stub

	}

}
