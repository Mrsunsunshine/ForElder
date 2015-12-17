package org.heartwings.care;

import java.util.HashMap;
import java.util.Map;

import org.heartwings.care.util.CONSTANTS;
import org.heartwings.care.util.HttpUtil;
import org.heartwings.network.NetworkChecker;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private EditText phoneText;
	private Button getCodeBtn;
	private ProgressDialog getCodeProgress;
	private MyHanlder hanlder;

	public static String returnCode;

	private String _phone;

	public static final String INTENT_MSG_PHONE = "com.nwpu.heartwings.activities:msg:phone";
	public static final String INTENT_MSG_CODE = "com.nwpu.heartwings.activities:msg:code";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.actionbar_bg));
		actionBar.setTitle("ע��");

		phoneText = (EditText) findViewById(R.id.login_user_name);
		getCodeBtn = (Button) findViewById(R.id.get_code);
		getCodeProgress = new ProgressDialog(RegisterActivity.this);

		getCodeProgress.setTitle(null);
		getCodeProgress.setMessage(CONSTANTS.GETCODEINFO);
		getCodeProgress.setCancelable(true);

		hanlder = new MyHanlder(getCodeProgress, getBaseContext(), getCodeBtn);

		getCodeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				if (getCodeBtn.getTag() != null
						&& getCodeBtn.getTag().toString()
								.equals(CONSTANTS.RETURNCODE_TAG)) {
					Intent intent = new Intent(RegisterActivity.this,
							Register2Activity.class);

					intent.putExtra(INTENT_MSG_PHONE, _phone);
					intent.putExtra(INTENT_MSG_CODE, returnCode);
					startActivity(intent);
				}

				else {
					if (TextUtils.isEmpty(phoneText.getText())) {
						Log.e("Register", "Phone number is empty");
						return;
					}

					if (!NetworkChecker.isNetAvailable(RegisterActivity.this)) {
						Toast.makeText(RegisterActivity.this.getBaseContext(),
								CONSTANTS.NETWORKERR, Toast.LENGTH_SHORT)
								.show();
					} else {

						_phone = phoneText.getText().toString();

						getCodeProgress.show();

						final Map<String, String> rawParams = new HashMap<String, String>();
						rawParams.put(CONSTANTS.REGISTER_PHONE, phoneText
								.getText().toString());
						Log.i("Register", "Request code from "
								+ HttpUtil.BASEURL
								+ CONSTANTS.RETURN_CODE_SERVLET);
						new Thread(new Runnable() {
							@Override
							public void run() {
								String result = HttpUtil
										.postRequest(
												HttpUtil.BASEURL
														+ CONSTANTS.RETURN_CODE_SERVLET,
												rawParams);
								Message msg = new Message();
								msg.what = CONSTANTS.HTTPPOST_CODE;
								msg.obj = result;
								hanlder.sendMessage(msg);
							}
						}).start();
					}
				}

			}
		});
	}

	static class MyHanlder extends Handler {

		private ProgressDialog mProgressDialog;
		private Button mButton;
		private Context context;

		public MyHanlder(ProgressDialog mProgressDialog, Context context,
				Button button) {
			super();
			this.mProgressDialog = mProgressDialog;
			this.context = context;
			this.mButton = button;
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == CONSTANTS.HTTPPOST_CODE) {
				System.out.println(msg.obj);
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mProgressDialog.dismiss();

				if (msg.obj.toString().equals(CONSTANTS.LOGIN_ERR_TAG)) {

					Toast.makeText(context, CONSTANTS.RETURNCODERR,
							Toast.LENGTH_LONG).show();

				} else {

					org.json.JSONObject jsonObject;
					try {
						jsonObject = new JSONObject(msg.obj.toString());
						if (jsonObject.getString(CONSTANTS.CODE_VALIDVALUE)
								.equals("2")) {
							mButton.setText(CONSTANTS.NEXTSTEP);
							mButton.setTag(CONSTANTS.RETURNCODE_TAG);

							RegisterActivity.returnCode = jsonObject
									.getString(CONSTANTS.CODE_INFO);

						} else {
							Toast.makeText(context, CONSTANTS.RETURNCODERR2,
									Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						Toast.makeText(context, CONSTANTS.RETURNCODERR2,
								Toast.LENGTH_LONG).show();
					}

				}
			}

		}

	}
}
