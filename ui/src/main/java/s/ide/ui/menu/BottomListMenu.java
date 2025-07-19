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

package s.ide.ui.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import s.ide.ui.R;

public class BottomListMenu {
    private final List<MenuItem> items;
    private OnMenuItemClickListener listener;

    public BottomListMenu() {
        this.items = new ArrayList<>();
    }

    public void addItem(int position, String text, @Nullable Drawable icon) {
        items.add(new MenuItem(position, text, icon));
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.listener = listener;
    }

    public void show(Context context) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.bottom_list_menu, null);
        ListView listView = view.findViewById(R.id.listView);

        MenuAdapter adapter = new MenuAdapter(context, items);
        listView.setAdapter(adapter);

        // Add a custom click listener for list items
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            if (listener != null) {
                listener.onMenuItemClick(position); // Notify the listener
            }
            bottomSheetDialog.dismiss(); // Close the bottom sheet after clicking
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(int position);
    }

    public static class MenuItem {
        int position;
        String text;
        Drawable icon;

        MenuItem(int position, String text, Drawable icon) {
            this.position = position;
            this.text = text;
            this.icon = icon;
        }
    }

}
