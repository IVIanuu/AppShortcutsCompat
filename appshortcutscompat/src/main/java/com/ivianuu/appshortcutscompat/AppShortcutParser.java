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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Parses app shortcuts
 */
final class AppShortcutParser {
    
    private static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String TAG_SHORTCUTS = "shortcuts";
    private static final String TAG_SHORTCUT = "shortcut";
    private static final String ATTRIBUTE_SHORTCUT_ICON = "icon";
    private static final String ATTRIBUTE_SHORTCUT_DISABLED_MESSAGE = "shortcutDisabledMessage";
    private static final String ATTRIBUTE_SHORTCUT_ID = "shortcutId";
    private static final String ATTRIBUTE_SHORTCUT_LONG_LABEL = "shortcutLongLabel";
    private static final String ATTRIBUTE_SHORTCUT_SHORT_LABEL = "shortcutShortLabel";
    private static final String TAG_INTENT = "intent";
    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_DATA = "data";
    private static final String ATTRIBUTE_TARGET_CLASS = "targetClass";
    private static final String ATTRIBUTE_TARGET_PACKAGE = "targetPackage";

    private AppShortcutParser() {
        // no instances
    }

    /**
     * Returns the parsed app shortcuts
     */
    @NonNull
    static List<AppShortcut> parse(Context context,
                                   Resources resources,
                                   String packageName,
                                   ComponentName componentName,
                                   int resId) throws Exception {
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

        List<AppShortcut> appShortcuts = new ArrayList<>();

        XmlPullParser parser = resources.getXml(resId);

        parser.next();
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, TAG_SHORTCUTS);
        while (parser.next() != XmlPullParser.END_TAG) {
            parser.require(XmlPullParser.START_TAG, null, TAG_SHORTCUT);
            String id = getAttribute(parser, ATTRIBUTE_SHORTCUT_ID);
            CharSequence shortLabel = getCharSequence(resources, parser, ATTRIBUTE_SHORTCUT_SHORT_LABEL);
            CharSequence longLabel = getCharSequence(resources, parser, ATTRIBUTE_SHORTCUT_LONG_LABEL);
            CharSequence disabledMessage = getCharSequence(resources, parser, ATTRIBUTE_SHORTCUT_DISABLED_MESSAGE);
            Drawable icon = resources.getDrawable(getResourceAttribute(parser, ATTRIBUTE_SHORTCUT_ICON));
            Intent activity = null;
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        if (TAG_INTENT.equals(parser.getName())) {
                            Intent intent = parseIntent(parser, componentName);
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                            Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                            if (activity == null) {
                                activity = intent;
                            }
                            depth--;
                        }
                        break;
                }
            }
            if (activity == null) continue;
            if (id == null) {
                //noinspection ConstantConditions
                id = activity.getComponent().toString() + "_shortcut" + appShortcuts.size();
            }
            if (shortLabel == null) {
                shortLabel = "";
            }
            if (longLabel == null) {
                longLabel = "";
            }
            if (disabledMessage == null) {
                disabledMessage = "";
            }

            if (isComponentExported(componentName, packageInfo)
                    && isComponentExported(activity.getComponent(), packageInfo)) {
                appShortcuts.add(new AppShortcut(
                        id,
                        activity,
                        componentName,
                        shortLabel,
                        longLabel,
                        disabledMessage,
                        icon));
            }
        }

        return appShortcuts;
    }

    private static boolean isComponentExported(ComponentName componentName, PackageInfo packageInfo) {
        ActivityInfo[] activities = packageInfo.activities;
        for (ActivityInfo activity : activities) {
            if (componentName.getClassName().equals(activity.name)) {
                return activity.exported;
            }
        }
        return false;
    }

    private static Intent parseIntent(
            XmlPullParser parser, ComponentName defaultComponent) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, TAG_INTENT);
        String action = getAttribute(parser, ATTRIBUTE_ACTION);
        String data = getAttribute(parser, ATTRIBUTE_DATA);
        String targetClass = getAttribute(parser, ATTRIBUTE_TARGET_CLASS);
        String targetPackage = getAttribute(parser, ATTRIBUTE_TARGET_PACKAGE);
        ComponentName component;
        if (targetClass == null || targetPackage == null) {
            component = defaultComponent;
        } else {
            component = new ComponentName(targetPackage, targetClass);
        }
        Intent intent = new Intent();
        intent.setComponent(component);
        if (action != null) {
            intent.setAction(action);
        } else {
            intent.setAction(Intent.ACTION_MAIN);
        }
        if (data != null) {
            intent.setData(Uri.parse(data));
        }
        skip(parser);
        return intent;
    }

    private static CharSequence getCharSequence(Resources resources, XmlPullParser parser, String attr) {
        int resId = getResourceAttribute(parser, attr);
        if (resId == 0) return null;
        if (resId == -1) return getAttribute(parser, attr);
        return resources.getString(resId);
    }

    private static int getResourceAttribute(XmlPullParser parser, String attr) {
        String value = getAttribute(parser, attr);
        if (value == null) return 0;
        if (!value.startsWith("@")) return -1;
        return Integer.parseInt(value.substring(1));
    }

    private static String getAttribute(XmlPullParser parser, String attr) {
        return parser.getAttributeValue(NAMESPACE_ANDROID, attr);
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}