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

import java.util.ArrayList;
import java.util.List;

import org.alarmapp.AlarmApp;
import org.alarmapp.R;
import org.alarmapp.util.Device;
import org.alarmapp.util.IntentUtil;
import org.alarmapp.util.LogEx;
import org.alarmapp.util.RingtoneUtil;
import org.alarmapp.util.adapter.BinderAdapter;
import org.alarmapp.util.adapter.IAdapterBinder;
import org.alarmapp.web.AuthWebClient;
import org.alarmapp.web.WebException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.c2dm.C2DMessaging;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */

	private class MenuEntry {
		public static final int COLOR_GREEN = 0;
		public static final int COLOR_RED = 1;
		public static final int COLOR_BLUE = 2;
		public static final int COLOR_YELLOW = 3;

		public MenuEntry(String title, String text, int color,
				Runnable clickHandler) {
			this.Title = title;
			this.Text = text;
			this.Color = color;
			this.ClickHandler = clickHandler;
		}

		public final int Color;
		public final String Title;
		public final String Text;
		public final Runnable ClickHandler;
	}

	private IAdapterBinder<MenuEntry> binder = new IAdapterBinder<MainActivity.MenuEntry>() {

		public View getView(MenuEntry item, View row, ViewGroup parent) {
			TextView title = (TextView) row.findViewById(R.id.tvTitle);
			TextView text = (TextView) row.findViewById(R.id.tvSubtitle);
			ImageView color = (ImageView) row.findViewById(R.id.ivStatus);

			title.setText(item.Title);
			text.setText(item.Text);
			color.setImageLevel(item.Color);
			return row;
		}
	};

	private ListView lvMainItems;

	private OnItemClickListener itemClick = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
			MenuEntry m = (MenuEntry) arg0.getItemAtPosition(pos);
			if (m != null && m.ClickHandler != null) {
				m.ClickHandler.run();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.log_out:
			new Thread(onLogoutClick).start();
			IntentUtil.displayLoginActivity(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private Runnable feedbackClick = new Runnable() {

		public void run() {
			IntentUtil.sendFeedbackEmailIntent(MainActivity.this);
		}
	};

	private Runnable infoClick = new Runnable() {

		public void run() {
			IntentUtil.displayInformationsActivity(MainActivity.this);

		}
	};

	private Runnable prefsClick = new Runnable() {

		public void run() {
			IntentUtil.displayPreferencesActivity(MainActivity.this);

		}
	};

	private Runnable alarmListClick = new Runnable() {

		public void run() {
			IntentUtil.displayAlarmListActivity(MainActivity.this);
		}
	};

	private Runnable alarmCreateClick = new Runnable() {
		public void run() {
			IntentUtil.displayAlarmCreateActivity(MainActivity.this);
		}
	};

	private Runnable onLogoutClick = new Runnable() {

		public void run() {
			C2DMessaging.unregister(MainActivity.this);
			AuthWebClient client = AlarmApp.getAuthWebClient();
			try {
				client.unregisterSmartphone(Device.id(AlarmApp.getInstance()));
			} catch (WebException e) {
				LogEx.exception(e);
				e.printStackTrace();
			}
			// AlarmApp.setUser(new AnonymusUserData());
			MainActivity.this.finish();
		}
	};

	private Runnable deleteRingtoneDir = new Runnable() {

		public void run() {
			RingtoneUtil.deleteRingtoneDir();
		}
	};

	private Runnable neeedHelpClick = new Runnable() {

		public void run() {
			IntentUtil.displayNeedhelpAcitivity(MainActivity.this);

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogEx.verbose("MainActivity");

		if (!isUserAvailable()) {
			IntentUtil.displayLoginActivity(this);
			this.finish();
			return;
		}

		setContentView(R.layout.main);
		getWindow().getDecorView().getRootView()
				.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

		LogEx.verbose("Ringtone is "
				+ AlarmApp.getPreferences().getString("alarm_ringtone", null));

		if (!RingtoneUtil.doesAlarmDirExsist()) {

			LogEx.verbose("Install new ring tones");
			RingtoneUtil.installSwissphoneQuattro();
			RingtoneUtil.installMotorolaBMD();
		} else {
			LogEx.verbose("Ringtones already installed!");
		}

		this.lvMainItems = (ListView) findViewById(R.id.lvMainItems);
		this.lvMainItems.setOnItemClickListener(itemClick);
		displayMenu();

	}

	private void displayMenu() {
		List<MenuEntry> entries = new ArrayList<MainActivity.MenuEntry>();
		entries.add(new MenuEntry("Letzte Einsätze",
				"Zeigt Ihnen eine Liste mit den letzten Einsätzen an.",
				MenuEntry.COLOR_BLUE, alarmListClick));
		entries.add(new MenuEntry("Informationen",
				"Zeigt Infos zu Ihrere Feuerwehr und Ihrem Benutzeraccount ",
				MenuEntry.COLOR_GREEN, infoClick));
		entries.add(new MenuEntry(getString(R.string.einstellungen_menu_title),
				getString(R.string.einstellungen_menu_desc),
				MenuEntry.COLOR_YELLOW, prefsClick));
		entries.add(new MenuEntry(getString(R.string.feedback_menu_title),
				getString(R.string.feedback_menu_desc), MenuEntry.COLOR_RED,
				feedbackClick));
		entries.add(new MenuEntry("Hilfe", "Ich brauche hilfe",
				MenuEntry.COLOR_BLUE, neeedHelpClick));

		// entries.add(new MenuEntry("Lösche", "Alarmtonverzeichnis",
		// MenuEntry.COLOR_BLUE, deleteRingtoneDir));
		// entries.add(new MenuEntry("Stopp", "Alarmton", MenuEntry.COLOR_BLUE,
		// stopSoundClick));

		this.lvMainItems.setAdapter(new BinderAdapter<MenuEntry>(this,
				R.layout.list_layout_main, binder, entries));
	}

	private boolean isUserAvailable() {
		return AlarmApp.getUser().isLoggedIn();
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}