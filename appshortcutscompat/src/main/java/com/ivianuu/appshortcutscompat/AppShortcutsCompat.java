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

package com.ivianuu.appshortcutscompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ivianuu.appshortcutscompat.Preconditions.checkNotNull;

/**
 * Entry point to retrieve shortcuts
 */
public final class AppShortcutsCompat {

    private AppShortcutsCompat() {
        // no instances
    }

    /**
     * Returns a list of app shortcuts for the package
     */
    @NonNull
    public static List<AppShortcut> getAppShortcuts(@NonNull Context context,
                                                    @NonNull String packageName) throws Exception {
        checkNotNull(context, "context == null");
        checkNotNull(packageName, "packageName == null");

        List<AppShortcut> shortcuts = new ArrayList<>();

        Resources resources = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY)
                .getResources();

        Map<ComponentName, Integer> resMap = AppShortcutPackageParser.parse(context, packageName);
        for (Map.Entry<ComponentName, Integer> entry : resMap.entrySet()) {
            shortcuts.addAll(AppShortcutParser.parse(context, resources, packageName, entry.getKey(), entry.getValue()));
        }

        return shortcuts;
    }
}
