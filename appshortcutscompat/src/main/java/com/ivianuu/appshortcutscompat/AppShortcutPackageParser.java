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

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Parses shortcut packages
 */
@SuppressLint("PrivateApi")
final class AppShortcutPackageParser {

    private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";
    private static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String TAG_MANIFEST = "manifest";
    private static final String TAG_APPLICATION = "application";
    private static final String TAG_ACTIVITY = "activity";
    private static final String TAG_ACTIVITY_ALIAS = "activity-alias";
    private static final String TAG_META_DATA = "meta-data";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_RESOURCE = "resource";
    private static final String META_APP_SHORTCUTS = "android.app.shortcuts";

    private AppShortcutPackageParser() {
        // no instances
    }

    /**
     * Returns parsed shortcuts xml map
     */
    @NonNull
    static HashMap<ComponentName, Integer> parse(Context context, String packageName) throws Exception {
        Resources resources = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY)
                .getResources();
        AssetManager assets = resources.getAssets();

        ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES);

        Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
        int cookie = (int) addAssetPath.invoke(assets, info.publicSourceDir);
        if (cookie == 0) {
            throw new RuntimeException("Failed adding asset path: " + info.publicSourceDir);
        }

        return parseManifest(assets, cookie, packageName);
    }

    private static HashMap<ComponentName, Integer> parseManifest(AssetManager assets,
                                                                 int cookie,
                                                                 String packageName) throws IOException, XmlPullParserException {
        HashMap<ComponentName, Integer> map = new HashMap<>();

        XmlPullParser parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);

        parser.next();
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, TAG_MANIFEST);
        while (parser.next() == XmlPullParser.START_TAG) {
            if (TAG_APPLICATION.equals(parser.getName())) {
                break;
            } else {
                // ignore any tag before <application />
                skip(parser);
            }
        }
        if (TAG_APPLICATION.equals(parser.getName())) {
            while (parser.next() == XmlPullParser.START_TAG) {
                if (TAG_ACTIVITY.equals(parser.getName())
                        || TAG_ACTIVITY_ALIAS.equals(parser.getName())) {
                    parseActivity(parser, packageName, map);
                } else {
                    skip(parser);
                }
            }
        } else {
            throw new IllegalStateException();
        }

        return map;
    }

    private static void parseActivity(
            XmlPullParser parser, String packageName, HashMap<ComponentName, Integer> map) throws IOException, XmlPullParserException {
        String activityName = getAttribute(parser, ATTR_NAME);
        if (activityName == null) {
            skip(parser);
            return;
        }
        ComponentName componentName = new ComponentName(packageName, activityName);
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    if (TAG_META_DATA.equals(parser.getName())) {
                        parseMeta(parser, componentName, map);
                        depth--;
                    }
                    break;
            }
        }
    }

    private static void parseMeta(
            XmlPullParser parser, ComponentName componentName, HashMap<ComponentName, Integer> map) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, TAG_META_DATA);
        String metaName = getAttribute(parser, ATTR_NAME);
        if (META_APP_SHORTCUTS.equals(metaName)) {
            try {
                int resId = Integer.parseInt(getAttribute(parser, ATTR_RESOURCE).substring(1));
                map.put(componentName, resId);
            } catch (NumberFormatException ignore) {

            }
        }
        skip(parser);
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