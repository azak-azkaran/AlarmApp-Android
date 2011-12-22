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

package org.alarmapp.util.checker;

import org.alarmapp.AlarmApp;
import org.alarmapp.util.LogEx;
import org.alarmapp.web.WebException;

import android.widget.TextView;

/**
 * @author frank
 * 
 */
public class UsernameUniqueTextViewChecker implements TextViewChecker {

	private boolean checkFailed = false;

	public boolean isValid(TextView v) {
		try {
			checkFailed = false;
			return AlarmApp.getWebClient()
					.checkUserName(v.getText().toString()).wasSuccessful();
		} catch (WebException e) {
			checkFailed = true;
			LogEx.exception(e);
			return false;
		}
	}

	public String getFormatDescription() {
		if (!checkFailed)
			return "Benutzername bereits vergeben";
		return "Überprüfen des Benutzernames fehlgeschlagen";
	}

}