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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Represents a app shortcut
 */
public final class AppShortcut {

    private final String id;

    private final Intent intent;
    private final ComponentName activity;

    private final CharSequence shortLabel;
    private final CharSequence longLabel;
    private final CharSequence disabledMessage;

    private final Drawable icon;

    AppShortcut(@NonNull String id,
                @NonNull Intent intent,
                @NonNull ComponentName activity,
                @NonNull CharSequence shortLabel,
                @NonNull CharSequence longLabel,
                @NonNull CharSequence disabledMessage,
                @NonNull Drawable icon) {
        this.id = id;
        this.intent = intent;
        this.activity = activity;
        this.shortLabel = shortLabel;
        this.longLabel = longLabel;
        this.disabledMessage = disabledMessage;
        this.icon = icon;
    }

    /**
     * Returns the id
     */
    @NonNull
    public String getId() {
        return id;
    }

    /**
     * Returns the intent
     */
    @NonNull
    public Intent getIntent() {
        return intent;
    }

    /**
     * Returns the activity component
     */
    @NonNull
    public ComponentName getActivity() {
        return activity;
    }

    /**
     * Returns the short label
     */
    @NonNull
    public CharSequence getShortLabel() {
        return shortLabel;
    }

    /**
     * Returns the long label
     */
    @NonNull
    public CharSequence getLongLabel() {
        return longLabel;
    }

    /**
     * Returns the disabled message
     */
    @NonNull
    public CharSequence getDisabledMessage() {
        return disabledMessage;
    }

    /**
     * Returns the short label
     */
    @NonNull
    public Drawable getIcon() {
        return icon;
    }
}
