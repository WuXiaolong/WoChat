package com.xiaomolongstudio.wochat.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.model.Contact;
import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.PingYinUtil;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;

@SuppressLint("DefaultLocale")
public class ContactsFragment extends Fragment {
	private View mView;
	private Activity mThis;
	private ListView mListView;
	private static List<Contact> friendsList = new ArrayList<Contact>();

	@SuppressLint("InlinedApi")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		bindXMPPService(AppConfig.CHATROOM_ACTION, mServiceConnection);
		mView = inflater.inflate(R.layout.contacts, container, false);
		mThis = getActivity();
		mListView = (ListView) mView.findViewById(R.id.listview);
		return mView;
	}

	class ContactAdapter extends BaseAdapter implements SectionIndexer {
		private Context mContext;

		@SuppressWarnings("unchecked")
		public ContactAdapter(Context mContext) {
			this.mContext = mContext;
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

		@SuppressLint("NewApi")
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.contact_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tvCatalog = (TextView) convertView
						.findViewById(R.id.contactitem_catalog);
				viewHolder.ivAvatar = (ImageView) convertView
						.findViewById(R.id.contactitem_avatar_iv);
				viewHolder.nick = (TextView) convertView
						.findViewById(R.id.contactitem_nick);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			String catalog = converterToFirstSpell(
					friendsList.get(position).getContactNick().toString())
					.substring(0, 1);

			if (position == 0) {
				viewHolder.tvCatalog.setVisibility(View.VISIBLE);
				viewHolder.tvCatalog.setText(catalog);
			} else {
				String lastCatalog = converterToFirstSpell(
						friendsList.get(position - 1).getContactNick()
								.toString()).substring(0, 1);
				if (catalog.equals(lastCatalog)) {
					viewHolder.tvCatalog.setVisibility(View.GONE);
				} else {
					viewHolder.tvCatalog.setVisibility(View.VISIBLE);
					viewHolder.tvCatalog.setText(catalog);
				}
			}

			if (friendsList.get(position).getAvatar() != null) {
				viewHolder.ivAvatar.setBackground(friendsList.get(position)
						.getAvatar());
			} else {
				viewHolder.ivAvatar.setImageResource(R.drawable.ic_launcher);
			}
			viewHolder.nick.setText(friendsList.get(position).getContactNick()
					.toString());
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Intent intent = new Intent(MyFriendsActivity.this,
					// FriendMsgActivity.class);
					// intent.putExtra("form",
					// friendsList.get(position).getJid()
					// .split("@")[0]);
					// intent.putExtra("isFriend", true);
					// startActivity(intent);
				}
			});
			return convertView;
		}

		class ViewHolder {
			LinearLayout contactitem_layout1, contactitem_layout2,
					contactitem_layout3;
			TextView tvCatalog;// 目录
			ImageView ivAvatar;// 头像
			TextView nick;// 昵称
		}

		class HeaderViewHolder {
			TextView contact_header_title;
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = 0; i < friendsList.size(); i++) {
				String l = converterToFirstSpell(
						friendsList.get(i).getContactNick().toString())
						.substring(0, 1);
				char firstChar = l.toUpperCase().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			return null;
		}

	}

	/**
	 * 汉字转换位汉语拼音首字母，英文字符不变
	 * 
	 * @param chines
	 *            汉字
	 * @return 拼音
	 */
	public static String converterToFirstSpell(String chines) {
		String pinyinName = "";
		char[] nameChar = chines.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < nameChar.length; i++) {
			if (nameChar[i] > 128) {
				try {
					pinyinName += PinyinHelper.toHanyuPinyinStringArray(
							nameChar[i], defaultFormat)[0].charAt(0);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyinName += nameChar[i];
			}
		}
		return pinyinName;
	}

	private void initRoster() {
		if (mXMPPService != null) {
			Collection<RosterEntry> entries = mXMPPService.roster()
					.getEntries();
			friendsList.removeAll(friendsList);
			for (Iterator<RosterEntry> entry = entries.iterator(); entry
					.hasNext();) {
				RosterEntry rosterEntry = entry.next(); // 获取所有好友
				// 互为好友
				if (rosterEntry.getType() == ItemType.both) {
					Contact contact = new Contact();
					contact.setJid(rosterEntry.getUser());
					if (rosterEntry.getName() == null) {
						contact.setContactNick(rosterEntry.getUser().split("@")[0]);
					} else {
						contact.setContactNick(rosterEntry.getName());
					}
					contact.setAvatar(mXMPPService.getUserImage(rosterEntry
							.getUser().split("@")[0]));
					friendsList.add(contact);
				}

			}

			Comparator<Contact> comparator = new Comparator<Contact>() {

				public int compare(Contact lhs, Contact rhs) {
					String str1 = PingYinUtil.getPingYin((String) lhs
							.getContactNick());
					String str2 = PingYinUtil.getPingYin((String) rhs
							.getContactNick());
					return str1.compareTo(str2);
				}
			};
			// 排序(实现了中英文混排)
			Collections.sort(friendsList, comparator);
			mListView.setAdapter(new ContactAdapter(getActivity()));
		}

	}

	public void onDestroy() {
		unbindXMPPService();

		super.onDestroy();
	}

	/**
	 * 解绑服务
	 */
	private void unbindXMPPService() {
		try {
			getActivity().unbindService(mServiceConnection);
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * 绑定服务
	 */
	public void bindXMPPService(String action,
			ServiceConnection serviceConnection) {
		Intent mServiceIntent = new Intent(getActivity(), XMPPService.class);
		mServiceIntent.setAction(action);
		getActivity().bindService(mServiceIntent, serviceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private XMPPService mXMPPService;
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
								Log.i("wxl", "CONNECTED");
								break;
							case XMPPService.CONNECTING:
								Log.i("wxl", "CONNECTING");

								break;
							case XMPPService.DISCONNECTED:
								// Toast.makeText(ChatRoomActivity.this, "用户掉线",
								// Toast.LENGTH_LONG).show();
								break;

							default:
								break;
							}

						}
					});
			// 开始连接xmpp服务器
			if (!mXMPPService.isAuthenticated()) {
				String userName = PreferenceUtils.getPrefString(getActivity(),
						PreferenceConstants.USER_NAME, "");
				String userPassword = PreferenceUtils.getPrefString(
						getActivity(), PreferenceConstants.PASSWORD, "");
				if (!TextUtils.isEmpty(userName)
						&& !TextUtils.isEmpty(userPassword))
					mXMPPService.login(userName, userPassword);
			}
			Log.i("wxl", "isAuthenticated=" + mXMPPService.isAuthenticated());
			initRoster();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXMPPService.unRegisterConnectionStatusCallback();
			mXMPPService = null;
		}

	};
}
