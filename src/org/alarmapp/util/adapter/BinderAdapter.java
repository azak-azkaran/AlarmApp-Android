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

package org.alarmapp.util.adapter;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class BinderAdapter<T> extends AbstractArrayAdapter<T> {

	IAdapterBinder<T> binder;

	public BinderAdapter(Activity activity, int layoutId,
			IAdapterBinder<T> binder, List<T> items) {
		super(activity, layoutId, items);
		this.binder = binder;
	}

	@Override
	public View getView(T item, View convertView, ViewGroup parent) {
		return binder.getView(item, convertView, parent);
	}

}
