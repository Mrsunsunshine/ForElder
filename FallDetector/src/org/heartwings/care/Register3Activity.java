package org.heartwings.care;

import java.util.HashMap;
import java.util.Map;

import org.heartwings.care.util.CONSTANTS;
import org.heartwings.care.util.DialogUtil;
import org.heartwings.care.util.HttpUtil;
import org.heartwings.network.NetworkChecker;
import org.heartwings.network.NetworkOperationHelper;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register3Activity extends Activity {
	private EditText nameEditText;
	private EditText pwdEditText;
	private EditText pwd2EditText;
	private Button registerBtn;

	private ProgressDialog register_ProgressDialog;

	private RegisterHandler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register3);

		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.actionbar_bg));
		actionBar.setTitle("注册");
		
		nameEditText = (EditText) findViewById(R.id.register_name);
		pwdEditText = (EditText) findViewById(R.id.pwd_one);
		pwd2EditText = (EditText) findViewById(R.id.pwd_two);

		registerBtn = (Button) findViewById(R.id.get_register);

		register_ProgressDialog = new ProgressDialog(this);
		register_ProgressDialog.setTitle(null);
		register_ProgressDialog.setMessage("正在注册...");
		register_ProgressDialog.setCancelable(true);

		handler = new RegisterHandler(register_ProgressDialog, this,
				NetworkOperationHelper.getNetworkOperationHelper()
						.getOperatorLooper());

		final String phone = getIntent().getStringExtra(
				Register2Activity.INTENT_PHONE);

		registerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				if (TextUtils.isEmpty(pwdEditText.getText())
						|| TextUtils.isEmpty(pwd2EditText.getText())
						|| TextUtils.isEmpty(nameEditText.getText())) {
					DialogUtil.showDialog(Register3Activity.this, "请填写完整");
					return;
				}

				if (!NetworkChecker.isNetAvailable(Register3Activity.this
						.getBaseContext())) {
					Toast.makeText(Register3Activity.this,
							CONSTANTS.NETWORKERR, Toast.LENGTH_SHORT).show();
					return;
				}

				final String pwd1 = pwdEditText.getText().toString();
				final String pwd2 = pwd2EditText.getText().toString();
				final String name = nameEditText.getText().toString();

				if (name.length() < 6) {

					DialogUtil.showDialog(Register3Activity.this, "用户名需3位字符以上");
					return;
				}
				if (!pwd1.equals(pwd2)) {
					DialogUtil.showDialog(Register3Activity.this, "两次密码不一致");
					pwdEditText.setText("");
					pwd2EditText.setText("");
					return;
				}

				if (pwd1.length() < 6 || pwd2.length() < 6) {
					DialogUtil.showDialog(Register3Activity.this, "密码长度需至少6位");
					pwdEditText.setText("");
					pwd2EditText.setText("");
					return;
				}

				register_ProgressDialog.show();

				new Thread(new Runnable() {

					@Override
					public void run() {

						Map<String, String> rawParams = new HashMap<String, String>();
						rawParams.put("phone", phone);
						rawParams.put("pwd", pwd1);
						rawParams.put("name", name);

						String result = HttpUtil.postRequest(HttpUtil.BASEURL
								+ "oldmanRegister", rawParams);
						Message message = new Message();
						message.what = 0x123;
						message.obj = result;

						handler.sendMessage(message);

					}
				}).start();
			}
		});

	}

	static class RegisterHandler extends Handler {

		private ProgressDialog mProgressDialog;
		private Context mContext;

		public RegisterHandler(ProgressDialog mProgressDialog,
				Context mContext, Looper looper) {
			super(looper);
			this.mProgressDialog = mProgressDialog;
			this.mContext = mContext;
		}

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == 0x123) {
				try {
					Thread.sleep(1250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				mProgressDialog.dismiss();
				if (msg.obj.toString().equals(CONSTANTS.LOGIN_ERR_TAG)) {
					DialogUtil.showDialog(mContext, "网络出错，请稍后重试");
				} else {
					// 前往登录。
					Intent intent = new Intent(mContext, MainActivity.class);
					mContext.startActivity(intent);
				}

			}

		}

	}

}
