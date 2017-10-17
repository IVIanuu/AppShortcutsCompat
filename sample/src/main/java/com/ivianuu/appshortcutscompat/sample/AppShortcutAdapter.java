/*
 * Copyright 2017 Manuel Wrage
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

package com.ivianuu.appshortcutscompat.sample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivianuu.appshortcutscompat.AppShortcut;

import java.util.List;

/**
 * @author Manuel Wrage (IVIanuu)
 */
class AppShortcutAdapter extends RecyclerView.Adapter<AppShortcutAdapter.AppShortcutViewHolder> {

    private final List<AppShortcut> shortcutInfos;

    AppShortcutAdapter(List<AppShortcut> shortcutInfos) {
        this.shortcutInfos = shortcutInfos;
    }

    @Override
    public AppShortcutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppShortcutViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_shortcut, parent, false));
    }

    @Override
    public void onBindViewHolder(final AppShortcutViewHolder holder, int position) {
        final AppShortcut appShortcut = shortcutInfos.get(position);
        holder.name.setText(appShortcut.getShortLabel());
        holder.icon.setImageDrawable(appShortcut.getIcon());
        holder.itemView.setOnClickListener(v -> v.getContext().startActivity(appShortcut.getIntent()));
    }

    @Override
    public int getItemCount() {
        return shortcutInfos.size();
    }

    static class AppShortcutViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView name;
        AppShortcutViewHolder(View itemView) {
            super(itemView);
            this.icon = itemView.findViewById(R.id.app_shortcut_icon);
            this.name = itemView.findViewById(R.id.app_shortcut_name);
        }
    }
}
