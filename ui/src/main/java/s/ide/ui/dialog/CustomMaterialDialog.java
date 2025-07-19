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

package s.ide.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import s.ide.ui.R;

public class CustomMaterialDialog {

    private final Context context;
    private final MaterialAlertDialogBuilder builder;
    private TextInputEditText textInputEditText;

    public CustomMaterialDialog(Context context) {
        this.context = context;
        builder = new MaterialAlertDialogBuilder(context);
    }

    public CustomMaterialDialog setTitle(int titleResId) {
        builder.setTitle(titleResId);
        return this;
    }

    public CustomMaterialDialog setMessage(int messageResId) {
        builder.setMessage(messageResId);
        return this;
    }

    public CustomMaterialDialog setMessage(String message) {
        builder.setMessage(message);
        return this;
    }

    public void setPositiveButton(int textResId, DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(textResId, listener);
    }

    public void setNegativeButton(int textResId, DialogInterface.OnClickListener listener) {
        builder.setNegativeButton(textResId, listener);
    }

    public void setInputEditTextHint(int hintResId) {
        View alert_layout = LayoutInflater.from(context).inflate(R.layout.input_dialog, null);
        textInputEditText = alert_layout.findViewById(R.id.textInputEditText);
        textInputEditText.setVisibility(View.VISIBLE);
        textInputEditText.setHint(hintResId);
        builder.setView(alert_layout);
    }

    public TextInputEditText getTextInputEditText() {
        return textInputEditText;
    }

    public void show() {
        builder.setCancelable(false);
        builder.create().show();
    }

}