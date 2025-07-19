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

package s.ide.ui.preference;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashSet;
import java.util.Set;

public class MaterialMultiSelectListPreference extends MultiSelectListPreference {

    public MaterialMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(getTitle());

        final Set<String> values = new HashSet<>(getValues());
        final boolean[] checkedItems = new boolean[getEntries().length];

        for (int i = 0; i < getEntryValues().length; i++) {
            checkedItems[i] = values.contains(getEntryValues()[i].toString());
        }

        builder.setMultiChoiceItems(getEntries(), checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                values.add(getEntryValues()[which].toString());
            } else {
                values.remove(getEntryValues()[which].toString());
            }
        });

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            if (callChangeListener(values)) {
                setValues(values);

                // Save the values to SharedPreferences
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                        .putStringSet(getKey(), values)
                        .apply();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}
