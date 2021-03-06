package cn.edu.cqu.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.edu.cqu.mobilesafe.service.AddressService;
import cn.edu.cqu.mobilesafe.service.CallSmsSafeService;
import cn.edu.cqu.mobilesafe.service.WatchDogService;
import cn.edu.cqu.mobilesafe.ui.SettingClickView;
import cn.edu.cqu.mobilesafe.ui.SettingItemView;
import cn.edu.cqu.mobilesafe.utils.MD5Utils;
import cn.edu.cqu.mobilesafe.utils.ServiceUtils;

public class SettingActivity extends Activity {

	private SettingItemView siv_update, siv_address, siv_callsmsm_safe,
			scv_wathdog;
	private SettingClickView scv_changebg;
	private SharedPreferences sp;
	private Intent intent_address, callsmsIntent, watchdogIntent;
	private AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		sp = getSharedPreferences("config", MODE_PRIVATE);
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		siv_address = (SettingItemView) findViewById(R.id.siv_address);
		siv_callsmsm_safe = (SettingItemView) findViewById(R.id.siv_callsmsm_safe);
		scv_wathdog = (SettingItemView) findViewById(R.id.scv_wathdog);

		// 设置改变背景
		scv_changebg = (SettingClickView) findViewById(R.id.scv_changebg);
		scv_changebg.setTitle("归属地提示框风格");
		final String items[] = { "半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿" };
		int which = sp.getInt("which", 0);
		scv_changebg.setDesc(items[which]);
		scv_changebg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				int dd = sp.getInt("which", 0);
				// 弹出一个单选框
				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setTitle("归属地风格");
				builder.setSingleChoiceItems(items, dd,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 保存选中的参数
								Editor editor = sp.edit();
								editor.putInt("which", which);
								scv_changebg.setDesc(items[which]);
								editor.commit();
							}
						});
				builder.setNegativeButton("取消", null);
				builder.show();
			}
		});

		boolean update = sp.getBoolean("update", false);
		if (update) {
			// 自动升级已经开启
			siv_update.setChecked(true);
		} else {
			// 自动升级已经开启
			siv_update.setChecked(false);
		}
		siv_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Editor editor = sp.edit();
				// 判断是否选中
				if (siv_update.isChecked()) {
					// 已经打开升级
					siv_update.setChecked(false);
					editor.putBoolean("update", false);
				} else {
					// 关闭升级
					siv_update.setChecked(true);
					editor.putBoolean("update", true);
				}
				editor.commit();
			}
		});
		intent_address = new Intent(this, AddressService.class);
		boolean serviceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"cn.edu.cqu.mobilesafe.service.AddressService");
		if (serviceRunning) {
			// 服务已经开启
			siv_address.setChecked(true);
		} else {
			siv_address.setChecked(false);
		}
		siv_address.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// 判断是否选中
				if (siv_address.isChecked()) {
					// 变为费选中状态，打开归属地
					siv_address.setChecked(false);
					// 关闭服务
					stopService(intent_address);
				} else {
					// 关闭显示归属地
					siv_address.setChecked(true);
					// 打开服务
					startService(intent_address);
				}
			}
		});

		// 黑名单拦截状态
		callsmsIntent = new Intent(SettingActivity.this,
				CallSmsSafeService.class);

		siv_callsmsm_safe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// 判断是否选中
				if (siv_callsmsm_safe.isChecked()) {
					// 变为非选中状态
					siv_callsmsm_safe.setChecked(false);
					// 关闭服务
					stopService(callsmsIntent);
				} else {
					// 选中状态
					siv_callsmsm_safe.setChecked(true);
					// 打开服务
					startService(callsmsIntent);
				}
			}
		});

		// 看门狗状态设置
		watchdogIntent = new Intent(SettingActivity.this, WatchDogService.class);
		boolean watchdogRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"cn.edu.cqu.mobilesafe.service.WatchDogService");
		if (watchdogRunning) {
			// 服务已经开启
			scv_wathdog.setChecked(true);
		} else {
			scv_wathdog.setChecked(false);
		}
		scv_wathdog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean watchdog = sp.getBoolean("fristsetwatchdog", true);
				if (watchdog) {
					// 弹出设置密码界面，并将watchdog设为true
					showSetPwdDialog();
					Editor editor = sp.edit();
					editor.putBoolean("fristsetwatchdog", false);
					editor.commit();
				} else {
					// 判断是否选中
					if (scv_wathdog.isChecked()) {
						// 变为非选中状态
						scv_wathdog.setChecked(false);
						// 关闭服务
						stopService(watchdogIntent);
					} else {
						// 选中状态
						scv_wathdog.setChecked(true);
						// 打开服务
						startService(watchdogIntent);
					}
				}
			}
		});
	}

	private void showSetPwdDialog() {
		AlertDialog.Builder builder = new Builder(SettingActivity.this);
		// 自定一的布局文件
		View view = View.inflate(SettingActivity.this, R.layout.dialog_setup_password, null);
		final EditText ev_setup_pwd = (EditText) view.findViewById(R.id.ev_setup_pwd);
		final EditText ev_setup_confirm = (EditText) view.findViewById(R.id.ev_comfirm_pwd);
		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 把对话框取消
				dialog.dismiss();
			}
		});
		btn_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 取出密码
				String password = ev_setup_pwd.getText().toString().trim();
				String password_confirm = ev_setup_confirm.getText().toString().trim();
				if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password_confirm)) {
					Toast.makeText(SettingActivity.this, "密码为空", Toast.LENGTH_SHORT).show();
					return;
				}
				// 判断是否一致
				if (password.equals(password_confirm)) {
					// 一直的话，就保存密码，把对话框消掉，进入防盗页面
					Editor editor = sp.edit();
					editor.putString("watchdogpassword", MD5Utils.md5Password(password));
					editor.commit();
					dialog.dismiss();
					Toast.makeText(SettingActivity.this, "密码设置成功", Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(SettingActivity.this, "密码不一致", Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});
		builder.setView(view);
		dialog = builder.show();
	}
	@Override
	protected void onResume() {
		super.onResume();
		boolean serviceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"cn.edu.cqu.mobilesafe.service.AddressService");
		if (serviceRunning) {
			// 服务已经开启
			siv_address.setChecked(true);
		} else {
			siv_address.setChecked(false);
		}
		boolean isCallSmsmServiceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"cn.edu.cqu.mobilesafe.service.CallSmsSafeService");
		if (isCallSmsmServiceRunning) {
			// 服务已经开启
			siv_callsmsm_safe.setChecked(true);
		} else {
			siv_callsmsm_safe.setChecked(false);
		}
	}
}
