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

package org.alarmapp;

import org.alarmapp.model.Alarm;
import org.alarmapp.model.AlarmedUser;
import org.alarmapp.util.Ensure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Broadcasts {
	public static String ALARM_RECEIVED = "org.alarmapp.alarm_received";
	public static String ALARMSTATUS_CHANGED = "org.alarmapp.alarm_status_changed";
	public static String C2DM_REGISTERED = "org.alarmapp.c2dm_registered";
	public static String SMARTPHONE_CREATED = "org.alarmapp.smartphone_created";

	public static void sendC2DMRegisteredBroadcast(Context ctxt,
			String registrationId) {
		Intent intent = new Intent(C2DM_REGISTERED);
		intent.putExtra("registration_id", registrationId);
		ctxt.sendBroadcast(intent);
	}

	public static void sendSmartphoneCreatedBroadcast(Context ctxt) {
		Intent intent = new Intent(SMARTPHONE_CREATED);
		ctxt.sendBroadcast(intent);
	}

	public static void sendAlarmstatusChangedBroadcast(Context ctxt,
			AlarmedUser changedUser) {
		Ensure.notNull(changedUser);

		Intent intent = new Intent(ALARMSTATUS_CHANGED);
		intent.putExtras(changedUser.getBundle());
		ctxt.sendBroadcast(intent);
	}

	public static void sendAlarmReceivedBroadcast(Context ctxt, Alarm a) {
		Ensure.notNull(a);
		Ensure.notNull(ctxt);

		Intent i = new Intent(ALARM_RECEIVED);
		i.putExtras(a.getBundle());

		ctxt.sendBroadcast(i);

	}

	public static void registerForAlarmReceivedBroadcast(Context ctxt,
			BroadcastReceiver receiver) {
		ctxt.registerReceiver(receiver, new IntentFilter(ALARM_RECEIVED));
	}

	public static void registerForAlarmstatusChangedBroadcast(Context ctxt,
			BroadcastReceiver recceiver) {
		ctxt.registerReceiver(recceiver, new IntentFilter(ALARMSTATUS_CHANGED));
	}

	public static void registerForSmartphoneCreatedBroadcast(Context ctxt,
			BroadcastReceiver receiver) {
		ctxt.registerReceiver(receiver, new IntentFilter(SMARTPHONE_CREATED));
	}

	public static void registerForC2DMRegisteredBroadcast(Context ctxt,
			BroadcastReceiver receiver) {
		ctxt.registerReceiver(receiver, new IntentFilter(C2DM_REGISTERED));
	}
}
