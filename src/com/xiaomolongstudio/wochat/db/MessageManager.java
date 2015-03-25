package com.xiaomolongstudio.wochat.db;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.jivesoftware.smackx.packet.VCard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.xiaomolongstudio.wochat.db.SQLiteTemplate.RowMapper;
import com.xiaomolongstudio.wochat.utils.FormatTools;
import com.xiaomolongstudio.wochat.utils.IMMessage;

/**
 * 
 消息历史记录，
 * 
 * @author shimiso
 */
public class MessageManager {
	private static MessageManager messageManager = null;
	private static DBManager manager = null;

	private MessageManager(Context context) {
		manager = DBManager.getInstance(context, "wochat");
	}

	public static MessageManager getInstance(Context context) {

		if (messageManager == null) {
			messageManager = new MessageManager(context);
		}

		return messageManager;
	}

	/**
	 * 
	 * 保存消息.
	 * 
	 * @param msg
	 * @author shimiso
	 * @update 2012-5-16 下午3:23:15
	 */
	public long saveIMMessage(IMMessage iMMessage) {
		SQLiteTemplate sqLiteTemplate = SQLiteTemplate.getInstance(manager,
				false);
		ContentValues contentValues = new ContentValues();
		contentValues.put(IMMessage.CHAT_CONTENT, iMMessage.getChatContent());
		contentValues.put(IMMessage.MSG_FROM, iMMessage.getMsgFrom());
		contentValues.put(IMMessage.MSG_TIME, iMMessage.getMsgTime());
		contentValues.put(IMMessage.AVATAR, iMMessage.getAvatar());
		contentValues.put(IMMessage.ROOMID, iMMessage.getRoomId());
		return sqLiteTemplate.insert("im_msg_his", contentValues);
	}

	/**
	 * 
	 * 更新状态.
	 * 
	 * @param status
	 * @author shimiso
	 * @update 2012-5-16 下午3:22:44
	 */
	public void updateStatus(String id, Integer status) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", status);
		st.updateById("im_msg_his", id, contentValues);
	}

	/**
	 * 
	 * 查找与某人的聊天记录聊天记录
	 * 
	 * @param pageNum
	 *            第几页
	 * @param pageSize
	 *            要查的记录条数
	 * @return
	 * @author shimiso
	 * @update 2012-7-2 上午9:31:04
	 */
	public List<IMMessage> getMessageListByFrom(String fromUser, int pageNum,
			int pageSize) {
		int fromIndex = (pageNum - 1) * pageSize;
		SQLiteTemplate sqLiteTemplate = SQLiteTemplate.getInstance(manager,
				false);
		List<IMMessage> list = sqLiteTemplate
				.queryForList(
						new RowMapper<IMMessage>() {
							@Override
							public IMMessage mapRow(Cursor cursor, int index) {
								IMMessage msg = new IMMessage();
								msg.setChatContent(cursor.getString(cursor
										.getColumnIndex(IMMessage.CHAT_CONTENT)));
								msg.setMsgFrom(cursor.getString(cursor
										.getColumnIndex(IMMessage.MSG_FROM)));
								msg.setRoomId(cursor.getString(cursor
										.getColumnIndex(IMMessage.ROOMID)));
								msg.setMsgTime(cursor.getString(cursor
										.getColumnIndex(IMMessage.MSG_TIME)));
								// 第一步，从数据库中读取出相应数据，并保存在字节数组中
								byte[] avatar = cursor.getBlob(cursor
										.getColumnIndex(IMMessage.AVATAR));
								// 第二步，调用BitmapFactory的解码方法decodeByteArray把字节数组转换为Bitmap对象
								// 第三步，调用BitmapDrawable构造函数生成一个BitmapDrawable对象，该对象继承Drawable对象，所以在需要处直接使用该对象即可

								msg.setDrawableAvatar(getDrawableAvatar(avatar));
								return msg;
							}
						},
						"select * from im_msg_his where msg_from=? order by msg_time desc limit ? , ? ",
						new String[] { "" + fromUser, "" + fromIndex,
								"" + pageSize });
		return list;

	}

	public List<IMMessage> getMessageListByroomId(String roomId, int pageNum,
			int pageSize) {
		int fromIndex = (pageNum - 1) * pageSize;
		SQLiteTemplate sqLiteTemplate = SQLiteTemplate.getInstance(manager,
				false);
		List<IMMessage> list = sqLiteTemplate
				.queryForList(
						new RowMapper<IMMessage>() {
							@Override
							public IMMessage mapRow(Cursor cursor, int index) {
								IMMessage msg = new IMMessage();
								msg.setChatContent(cursor.getString(cursor
										.getColumnIndex(IMMessage.CHAT_CONTENT)));
								msg.setMsgFrom(cursor.getString(cursor
										.getColumnIndex(IMMessage.MSG_FROM)));
								msg.setRoomId(cursor.getString(cursor
										.getColumnIndex(IMMessage.ROOMID)));
								msg.setMsgTime(cursor.getString(cursor
										.getColumnIndex(IMMessage.MSG_TIME)));
								// 第一步，从数据库中读取出相应数据，并保存在字节数组中
								byte[] avatar = cursor.getBlob(cursor
										.getColumnIndex(IMMessage.AVATAR));
								// 第二步，调用BitmapFactory的解码方法decodeByteArray把字节数组转换为Bitmap对象
								// 第三步，调用BitmapDrawable构造函数生成一个BitmapDrawable对象，该对象继承Drawable对象，所以在需要处直接使用该对象即可

								msg.setDrawableAvatar(getDrawableAvatar(avatar));
								return msg;
							}
						},
						"select * from im_msg_his where roomId=? order by msg_time desc limit ? , ? ",
						new String[] { "" + roomId, "" + fromIndex,
								"" + pageSize });
		return list;

	}

	public Drawable getDrawableAvatar(byte[] avatar) {
		ByteArrayInputStream bais = null;

		if (avatar == null)
			return null;
		bais = new ByteArrayInputStream(avatar);
		return FormatTools.getInstance().InputStream2Drawable(bais);
	}

	/**
	 * 
	 * 查找与某人的聊天记录总数
	 * 
	 * @return
	 * @author shimiso
	 * @update 2012-7-2 上午9:31:04
	 */
	public int getChatCountWithSb(String fromUser) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st
				.getCount(
						"select _id,content,msg_from msg_type  from im_msg_his where msg_from=?",
						new String[] { "" + fromUser });

	}

	/**
	 * 删除与某人的聊天记录 author shimiso
	 * 
	 * @param fromUser
	 */
	public int delChatHisWithSb(String fromUser) {
		SQLiteTemplate st = SQLiteTemplate.getInstance(manager, false);
		return st.deleteByCondition("im_msg_his", "msg_from=?",
				new String[] { "" + fromUser });
	}

}
