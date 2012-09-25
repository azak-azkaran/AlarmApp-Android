/*
 * Copyright (C) 2011-2012 AlarmApp.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.alarmapp.activities;

import org.alarmapp.AlarmApp;
import org.alarmapp.Broadcasts;
import org.alarmapp.R;
import org.alarmapp.model.User;
import org.alarmapp.util.ActivityUtil;
import org.alarmapp.util.IntentUtil;
import org.alarmapp.util.LogEx;
import org.alarmapp.web.FireDepartmentMissingException;
import org.alarmapp.web.HttpWebClient;
import org.alarmapp.web.WebClient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.c2dm.C2DMessaging;

public class LoginActivity extends Activity {

	private EditText etEmail;
	private EditText etPassword;
	private Button btLogin;
	private Button btCreateAccount;
	private ProgressBar pbLogin;
	private TextView tvLoginProgress;
	private TextView tvProgressStep;
	private User user;

	private boolean isPushServiceBroadcastRegistered = false;
	private boolean isSmartphoneCreatedBroadcastRegistered = false;

	private Runnable displayProgress(final String nextStep) {
		return new Runnable() {
			public void run() {
				tvProgressStep.setText(nextStep);
				pbLogin.incrementProgressBy(1);
			}
		};
	}

	private final BroadcastReceiver pushServiceRegistered = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			runOnUiThread(displayProgress("Smartphone registrieren"));

			LogEx.info("Registration is "
					+ intent.getStringExtra("registration_id"));

			unregisterReceiver(pushServiceRegistered);
			isPushServiceBroadcastRegistered = false;
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		if (isPushServiceBroadcastRegistered) {
			unregisterReceiver(pushServiceRegistered);
			isPushServiceBroadcastRegistered = false;
		}
		if (isSmartphoneCreatedBroadcastRegistered) {
			unregisterReceiver(smartphoneCreated);
			isSmartphoneCreatedBroadcastRegistered = false;
		}
	};

	private final BroadcastReceiver smartphoneCreated = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onSuccessfulLogin();
		}
	};

	OnClickListener onLoginClickListener = new OnClickListener() {
		public void onClick(View v) {
			LogEx.verbose("Login key down");
			btLogin.setOnClickListener(null);

			hideKeyboad();

			final String email = LoginActivity.this.etEmail.getText()
					.toString();
			final String password = LoginActivity.this.etPassword.getText()
					.toString();

			setVisibility(View.VISIBLE);
			new Thread(new Runnable() {

				public void run() {
					try {
						WebClient client = new HttpWebClient();
						user = client.login(email, password);
						AlarmApp.setUser(user);

						runOnUiThread(displayProgress("Push-Dienst starten"));

						Broadcasts.registerForC2DMRegisteredBroadcast(
								LoginActivity.this, pushServiceRegistered);
						isPushServiceBroadcastRegistered = true;
						Broadcasts.registerForSmartphoneCreatedBroadcast(
								LoginActivity.this, smartphoneCreated);
						isSmartphoneCreatedBroadcastRegistered = true;

						LogEx.info("C2DMessaging.Register");
						C2DMessaging.register(LoginActivity.this,
								"f.englert@gmail.com");
					} catch (final FireDepartmentMissingException e) {
						LogEx.exception(
								"Der Benutzer ist in keiner Feuerwehr. ", e);
						LoginActivity.this.displayError(e.getMessage());
					} catch (final Exception e) {
						LogEx.exception(
								"Der Login des Benutzers schlug fehl. ", e);

						LoginActivity.this.displayError(e.getMessage());
					}
				}
			}).start();
		}
	};

	OnClickListener onCreateClick = new OnClickListener() {

		public void onClick(View v) {
			IntentUtil.displayAccountCreateActivity(LoginActivity.this);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.login);

		this.btLogin = (Button) findViewById(R.id.btLogin);
		this.btCreateAccount = (Button) findViewById(R.id.btCreateAccount);
		this.etEmail = (EditText) findViewById(R.id.etEmail);
		this.etPassword = (EditText) findViewById(R.id.etPassword);
		this.pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
		this.tvLoginProgress = (TextView) findViewById(R.id.tvLoginProgress);
		this.tvProgressStep = (TextView) findViewById(R.id.tvProgressStep);

		this.pbLogin.setMax(3);
		this.etPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);

		setVisibility(View.INVISIBLE);

		this.btLogin.setOnClickListener(onLoginClickListener);
		this.btCreateAccount.setOnClickListener(onCreateClick);
	}

	protected void hideKeyboad() {
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(this.etPassword.getWindowToken(), 0);
	}

	private void setVisibility(final int visibility) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				LoginActivity.this.pbLogin.setVisibility(visibility);
				LoginActivity.this.tvLoginProgress.setVisibility(visibility);
				LoginActivity.this.tvProgressStep.setVisibility(visibility);
			}
		});
	}

	private void onSuccessfulLogin() {
		AlarmApp.setUser(user);
		ActivityUtil.displayToast(this, "Hallo " + user.getFirstName(), 10);

		btLogin.setOnClickListener(onLoginClickListener);
		setVisibility(View.INVISIBLE);

		IntentUtil.displayMainActivity(this);
		this.finish();
	}

	private void displayError(final String errorMessage) {
		ActivityUtil
				.showAlertDialog(this, "Login Fehlgeschlagen", errorMessage);

		btLogin.setOnClickListener(onLoginClickListener);
		setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogEx.info("Key Down!");
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
