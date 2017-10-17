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

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ivianuu.appshortcutscompat.AppShortcut;
import com.ivianuu.appshortcutscompat.AppShortcutsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> launchableApps = getPackageManager().queryIntentActivities(intent, 0);

            List<AppShortcut> shortcutInfos = new ArrayList<>();
            for (ResolveInfo resolveInfo : launchableApps) {
                String packageName = resolveInfo.activityInfo.packageName;
                shortcutInfos.addAll(AppShortcutsCompat.getAppShortcuts(this, packageName));
            }

            RecyclerView recyclerView = findViewById(R.id.list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            AppShortcutAdapter shortcutAdapter = new AppShortcutAdapter(shortcutInfos);
            recyclerView.setAdapter(shortcutAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
