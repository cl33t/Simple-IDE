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

import android.annotation.SuppressLint;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

  public LogAdapter() {}
  
  private final AsyncListDiffer<Log> mDiffer = new AsyncListDiffer<>(this, DIFF_CALLBACK);
  
  public void submitList(List<Log> newData) {
        mDiffer.submitList(newData);
  }
  
  public static final DiffUtil.ItemCallback<Log> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
      @Override
      public boolean areItemsTheSame(@NonNull Log oldLog, @NonNull Log newLog) {
          return oldLog.getMessage().equals(newLog.getMessage());
      }

      @SuppressLint("DiffUtilEquals")
      @Override
      public boolean areContentsTheSame(@NonNull Log oldLog, @NonNull Log newLog) {
          return oldLog.getMessage().equals(newLog.getMessage());
      }
  };
  
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(new FrameLayout(parent.getContext()));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Log log = mDiffer.getCurrentList().get(position);
    SpannableStringBuilder sb = new SpannableStringBuilder();
    sb.append("");
    sb.append(log.getTag());
    sb.append(": ");
    sb.append("");
    sb.append(log.getMessage());
    holder.mText.setText(sb);
  }

  @Override
  public int getItemCount() {
    return mDiffer.getCurrentList().size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public TextView mText;

    public ViewHolder(View view) {
      super(view);

      mText = new TextView(view.getContext());
      ((ViewGroup) view).addView(mText);
    }
  }
}
