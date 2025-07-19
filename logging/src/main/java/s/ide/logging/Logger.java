/*
 * Simple IDE
 * Repository: https://github.com/vxhjsd/Simple-IDE
 * Developer: vxhjsd <vxhjsd@gmail.com>
 *
 * Copyright (C) 2025  vxhjsd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package s.ide.logging;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.util.ArrayList;

public class Logger {

  private boolean mAttached;

  private LogViewModel model;

    public void attach(ViewModelStoreOwner activity) {
        model = new ViewModelProvider(activity).get(LogViewModel.class);
    mAttached = true;
  }

  public void d(String tag, String message) {
    if (!mAttached) {
      return;
    }
    add(new Log(tag, message));
  }

  public void e(String tag, String message) {
    if (!mAttached) {
      return;
    }
    Spannable messageSpan = new SpannableString(message);
    messageSpan.setSpan(
        new ForegroundColorSpan(0xffff0000),
        0,
        message.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    add(new Log(tag, messageSpan));
  }

  public void w(String tag, String message) {
    if (!mAttached) {
      return;
    }
    Spannable messageSpan = new SpannableString(message);
    messageSpan.setSpan(
        new ForegroundColorSpan(0xffff7043),
        0,
        message.length(),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    add(new Log(tag, messageSpan));
  }

  public void p(String tag, String message) {
    if (!mAttached) {
      return;
    }
    Spannable tagSpan = new SpannableString(tag);
    tagSpan.setSpan(
        new ForegroundColorSpan(0xFF0D47A1), 0, tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    add(new Log(tagSpan, message));
  }

  private void add(Log log) {
    ArrayList<Log> currentList = model.getLogs().getValue();
    if (currentList == null) {
      currentList = new ArrayList<>();
    }
    currentList.add(log);
    model.getLogs().postValue(currentList);
  }

  public void clear() {
    model.getLogs().setValue(new ArrayList<>());
  }

  public void i(String tag, String message) {
    if (!mAttached) {
      return;
    }
    Spannable messageSpan = new SpannableString(message);
    messageSpan.setSpan(
            new ForegroundColorSpan(0xFFFFD700),
            0,
            message.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    add(new Log(tag, messageSpan));
  }

}
