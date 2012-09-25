/*
 * Copyright (C) 2011 AlarmApp.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.alarmapp.model.AlarmedUser;
import org.alarmapp.model.classes.AlarmedUserData;
import org.alarmapp.util.IntentUtil;
import org.alarmapp.util.LogEx;
import org.alarmapp.util.RingtoneUtil;
import org.alarmapp.web.WebException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author Administrator
 * 
 */
public class NeedHelpActivity extends Activity {

	Button btNeedHelp;

	private final BroadcastReceiver alarmStatusChangedBroadCastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final AlarmedUser changedUser = AlarmedUserData.create(intent
					.getExtras());

			runOnUiThread(new Runnable() {

				public void run() {
					refreshNeedHelpStatus(changedUser);
				}
			});
		}
	};

	private void refreshNeedHelpStatus(AlarmedUser changedUser) {
		final AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle("Hilferuf angenommen");
		d.setMessage(changedUser.getFullName()
				+ " hat Ihren Hilferuf angenommen... ");

		d.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				d.dismiss();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		LogEx.verbose("NeedHelpActivity");

		if (!isUserAvailable()) {
			IntentUtil.displayLoginActivity(this);
			this.finish();
			return;
		}

		setContentView(R.layout.need_help_activity);
		View rootView = getWindow().getDecorView().getRootView();

		rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		btNeedHelp = (Button) findViewById(R.id.btNeedHelp);
		btNeedHelp.setOnClickListener(onNeedHelpClick);

		if (!RingtoneUtil.doesAlarmDirExsist()) {

			LogEx.verbose("Install new ring tones");
			RingtoneUtil.installSwissphoneQuattro();
			RingtoneUtil.installMotorolaBMD();
		} else {
			LogEx.verbose("Ringtones already installed!");
		}

	}

	protected void onResume() {
		super.onResume();
		Broadcasts.registerForAlarmstatusChangedBroadcast(this,
				alarmStatusChangedBroadCastReceiver);
	};

	ProgressDialog progress;

	Runnable performHelpCall = new Runnable() {

		public void run() {
			try {
				LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				Location loc = lm
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				String message = "Hilfebedürftiger: "
						+ AlarmApp.getUser().getFullName();

				if (loc != null) {
					message += "Position: " + loc.getLatitude() + "/"
							+ loc.getLongitude();
				}

				AlarmApp.getAuthWebClient().performAlarm("help", "Hilferuf",
						message);

				// Todo: Capture all Broadcast events either AlarmStatusChange
				// or Alarmed
			} catch (WebException e) {
				LogEx.exception(e);
			} finally {
				if (NeedHelpActivity.this.progress != null) {
					NeedHelpActivity.this.progress.dismiss();
				}
			}
		}
	};

	private OnClickListener onNeedHelpClick = new OnClickListener() {

		public void onClick(View arg0) {
			LogEx.info("Pressed the need help button.");
			progress = ProgressDialog.show(NeedHelpActivity.this,
					"Hilferuf absetzen", "Bitte warten...");
			new Thread(performHelpCall).start();
		}
	};

	private boolean isUserAvailable() {
		return AlarmApp.getUser().isLoggedIn();
	}

}
