package com.xiaomolongstudio.wochat.ui;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class ChatActivity extends Activity {
	EditText userName, password;
	Button btn_login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}


}
