/*
 * Copyright (C) 2022 The Pixel Experience Project
 *               2021-2022 crDroid Android Project
 *           (C) 2023 ArrowOS
 *           (C) 2023 The LibreMobileOS Foundation
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

package com.android.internal.util.custom;

import android.app.ActivityTaskManager;
import android.app.Application;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PixelPropsUtils {

    private static final String TAG = PixelPropsUtils.class.getSimpleName();
    private static final String DEVICE = "org.skylineui.device";

    private static final String PACKAGE_GMS = "com.google.android.gms";
    private static final ComponentName GMS_ADD_ACCOUNT_ACTIVITY = ComponentName.unflattenFromString(
            "com.google.android.gms/.auth.uiflows.minutemaid.MinuteMaidActivity");

    private static final boolean DEBUG = false;

    private static final Map<String, Object> propsToChangeGeneric;
    private static final Map<String, Object> propsToChangePixel7Pro;
    private static final Map<String, Object> propsToChangePixel6Pro;
    private static final Map<String, Object> propsToChangePixel5;
    private static final Map<String, Object> propsToChangePixelXL;
    private static final Map<String, ArrayList<String>> propsToKeep;

    private static final String[] packagesToChangePixel7Pro = {
            "com.google.android.apps.privacy.wildlife",
            "com.google.android.apps.wallpaper.pixel",
            "com.google.android.apps.wallpaper",
            "com.google.android.apps.subscriptions.red",
            "com.google.pixel.livewallpaper"
    };

    private static final String[] packagesToChangePixel6Pro = {
            "com.google.android.inputmethod.latin",
            "com.google.android.as",
            "com.google.android.wallpaper.effects",
            "com.google.android.apps.emojiwallpaper",
    };

    private static final String[] packagesToChangePixelXL = {
            "com.snapchat.android"
    };

    private static final String[] extraPackagesToChange = {
            "com.android.chrome",
            "com.breel.wallpapers20",
            "com.nhs.online.nhsonline",
            "com.netflix.mediaclient",
            "com.nothing.smartcenter"
    };

    private static final Map<String, Object> propsToChangeROG1;
    private static final String[] packagesToChangeROG1 = {
            "com.dts.freefireth",
            "com.dts.freefiremax",
            "com.madfingergames.legends"
    };

    private static final Map<String, Object> propsToChangeXP5;
    private static final String[] packagesToChangeXP5 = {
            "com.activision.callofduty.shooter",
            "com.tencent.tmgp.kr.codm",
            "com.garena.game.codm",
            "com.vng.codmvn"
    };

    private static final Map<String, Object> propsToChangeOP8P;
    private static final String[] packagesToChangeOP8P = {
            "com.tencent.ig",
            "com.pubg.imobile",
            "com.pubg.krmobile",
            "com.pubg.newstate",
            "com.vng.pubgmobile",
            "com.rekoo.pubgm",
            "com.tencent.tmgp.pubgmhd",
            "com.riotgames.league.wildrift",
            "com.riotgames.league.wildrifttw",
            "com.riotgames.league.wildriftvn",
            "com.netease.lztgglobal",
            "com.epicgames.fortnite",
            "com.epicgames.portal"
    };

    private static final String[] packagesToKeep = {
            "com.google.android.dialer",
            "com.google.android.euicc",
            "com.google.ar.core",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.kids",
            "com.google.android.apps.youtube.music",
            "com.google.android.apps.recorder",
            "com.google.android.apps.wearables.maestro.companion",
            "com.google.android.apps.tachyon",
            "com.google.android.apps.tycho"
    };

    private static final String[] customGoogleCameraPackages = {
            "com.google.android.MTCL83",
            "com.google.android.UltraCVM",
            "com.google.android.apps.cameralite"
    };

    // Codenames for currently supported Pixels by Google
    private static final String[] pixelCodenames = {
            "felix",
            "tangorpro",
            "lynx",
            "cheetah",
            "panther",
            "bluejay",
            "oriole",
            "raven",
            "barbet",
            "redfin",
            "bramble",
            "sunfish"
    };

    private static volatile boolean sIsGms, sIsFinsky, sIsPhotos;

    static {
        propsToKeep = new HashMap<>();
        propsToKeep.put("com.google.android.settings.intelligence", new ArrayList<>(Collections.singletonList("FINGERPRINT")));
        propsToChangeGeneric = new HashMap<>();
        propsToChangeGeneric.put("TYPE", "user");
        propsToChangeGeneric.put("TAGS", "release-keys");
        propsToChangePixel7Pro = new HashMap<>();
        propsToChangePixel7Pro.put("BRAND", "google");
        propsToChangePixel7Pro.put("MANUFACTURER", "Google");
        propsToChangePixel7Pro.put("DEVICE", "cheetah");
        propsToChangePixel7Pro.put("PRODUCT", "cheetah");
        propsToChangePixel7Pro.put("MODEL", "Pixel 7 Pro");
        propsToChangePixel7Pro.put("FINGERPRINT", "google/cheetah/cheetah:13/TQ3A.230705.001.A1/10217028:user/release-keys");
        propsToChangePixel6Pro = new HashMap<>();
        propsToChangePixel6Pro.put("BRAND", "google");
        propsToChangePixel6Pro.put("MANUFACTURER", "Google");
        propsToChangePixel6Pro.put("DEVICE", "raven");
        propsToChangePixel6Pro.put("PRODUCT", "raven");
        propsToChangePixel6Pro.put("MODEL", "Pixel 6 Pro");
        propsToChangePixel6Pro.put("FINGERPRINT", "google/raven/raven:13/TQ3A.230705.001.A1/10217028:user/release-keys");
        propsToChangePixel5 = new HashMap<>();
        propsToChangePixel5.put("BRAND", "google");
        propsToChangePixel5.put("MANUFACTURER", "Google");
        propsToChangePixel5.put("DEVICE", "redfin");
        propsToChangePixel5.put("PRODUCT", "redfin");
        propsToChangePixel5.put("MODEL", "Pixel 5");
        propsToChangePixel5.put("FINGERPRINT", "google/redfin/redfin:13/TQ3A.230705.001/10216780:user/release-keys");
        propsToChangePixelXL = new HashMap<>();
        propsToChangePixelXL.put("BRAND", "google");
        propsToChangePixelXL.put("MANUFACTURER", "Google");
        propsToChangePixelXL.put("DEVICE", "marlin");
        propsToChangePixelXL.put("PRODUCT", "marlin");
        propsToChangePixelXL.put("MODEL", "Pixel XL");
        propsToChangePixelXL.put("FINGERPRINT", "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys");
        propsToChangeROG1 = new HashMap<>();
        propsToChangeROG1.put("MODEL", "ASUS_Z01QD");
        propsToChangeROG1.put("MANUFACTURER", "asus");
        propsToChangeXP5 = new HashMap<>();
        propsToChangeXP5.put("MODEL", "SO-52A");
        propsToChangeOP8P = new HashMap<>();
        propsToChangeOP8P.put("MODEL", "IN2020");
        propsToChangeOP8P.put("MANUFACTURER", "OnePlus");
    }

    private static boolean isGoogleCameraPackage(String packageName) {
        return packageName.startsWith("com.google.android.GoogleCamera") ||
                Arrays.asList(customGoogleCameraPackages).contains(packageName);
    }

    public static void setProps(String packageName) {
        propsToChangeGeneric.forEach((k, v) -> setPropValue(k, v));
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        if (Arrays.asList(packagesToKeep).contains(packageName)) {
            return;
        }
        if (isGoogleCameraPackage(packageName)) {
            return;
        }

        Map<String, Object> propsToChange = new HashMap<>();
        if (packageName.equals(PACKAGE_GMS)
                || packageName.toLowerCase().contains("androidx.test")
                || packageName.equalsIgnoreCase("com.google.android.apps.restore")) {
            final String processName = Application.getProcessName();
            if (processName.toLowerCase().contains("unstable")
                    || processName.toLowerCase().contains("pixelmigrate")
                    || processName.toLowerCase().contains("instrumentation")) {
                sIsGms = true;

                final boolean was = isGmsAddAccountActivityOnTop();
                final TaskStackListener taskStackListener = new TaskStackListener() {
                    @Override
                    public void onTaskStackChanged() {
                        final boolean is = isGmsAddAccountActivityOnTop();
                        if (is ^ was) {
                            dlog("GmsAddAccountActivityOnTop is:" + is + " was:" + was + ", killing myself!");
                            // process will restart automatically later
                            Process.killProcess(Process.myPid());
                        }
                    }
                };
                try {
                    ActivityTaskManager.getService().registerTaskStackListener(taskStackListener);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to register task stack listener!", e);
                }
                if (was) return;

                dlog("Spoofing build for GMS");
                // Alter build parameters to pixel 2 for avoiding hardware attestation enforcement
                setBuildField("DEVICE", "walleye");
                setBuildField("FINGERPRINT", "google/walleye/walleye:8.1.0/OPM1.171019.011/4448085:user/release-keys");
                setBuildField("MODEL", "Pixel 2");
                setBuildField("PRODUCT", "walleye");
                setVersionField("DEVICE_INITIAL_SDK_INT", Build.VERSION_CODES.O);
            } else if (processName.toLowerCase().contains("persistent")
                    || processName.toLowerCase().contains("ui")
                    || processName.toLowerCase().contains("learning")) {
                propsToChange.putAll(propsToChangePixel6Pro);
            }
            return;
        }

        if (packageName.startsWith("com.google.")
                || packageName.startsWith("com.samsung.")
                || Arrays.asList(extraPackagesToChange).contains(packageName)) {

            boolean isPixelDevice = Arrays.asList(pixelCodenames).contains(SystemProperties.get(DEVICE));

            if (packageName.equals("com.android.vending")) {
                sIsFinsky = true;
            }

            if (packageName.equals("com.google.android.apps.photos")) {
                propsToChange.putAll(propsToChangePixelXL);
            } else if (isPixelDevice) {
                return;
            } else {
                if (Arrays.asList(packagesToChangePixel7Pro).contains(packageName)) {
                    propsToChange.putAll(propsToChangePixel7Pro);
                } else if (Arrays.asList(packagesToChangePixelXL).contains(packageName)) {
                    propsToChange.putAll(propsToChangePixelXL);
                } else if (Arrays.asList(packagesToChangePixel6Pro).contains(packageName)) {
                    propsToChange.putAll(propsToChangePixel6Pro);
                } else {
                    propsToChange.putAll(propsToChangePixel5);
                }
            }

            if (Arrays.asList(packagesToChangeROG1).contains(packageName)) {
                propsToChange.putAll(propsToChangeROG1);
            }

            if (Arrays.asList(packagesToChangeXP5).contains(packageName)) {
                propsToChange.putAll(propsToChangeXP5);
            }

            if (Arrays.asList(packagesToChangeOP8P).contains(packageName)) {
                propsToChange.putAll(propsToChangeOP8P);
            }

            if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                if (propsToKeep.containsKey(packageName) && propsToKeep.get(packageName).contains(key)) {
                    if (DEBUG) Log.d(TAG, "Not defining " + key + " prop for: " + packageName);
                    continue;
                }
                if (DEBUG) Log.d(TAG, "Defining " + key + " prop for: " + packageName);
                setPropValue(key, value);
            }
            // Set proper indexing fingerprint
            if (packageName.equals("com.google.android.settings.intelligence")) {
                setPropValue("FINGERPRINT", Build.VERSION.INCREMENTAL);
            }
        } else {
            // Define Props for GameProps
            if (Arrays.asList(packagesToChangeROG1).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeROG1.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeXP5).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeXP5.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            } else if (Arrays.asList(packagesToChangeOP8P).contains(packageName)) {
                if (DEBUG) Log.d(TAG, "Defining props for: " + packageName);
                for (Map.Entry<String, Object> prop : propsToChangeOP8P.entrySet()) {
                    String key = prop.getKey();
                    Object value = prop.getValue();
                    setPropValue(key, value);
                }
            }
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            if (DEBUG) Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }

    private static void setBuildField(String key, String value) {
        try {
            // Unlock
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);

            // Edit
            field.set(null, value);

            // Lock
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to spoof Build." + key, e);
        }
    }

    private static void setVersionField(String key, Object value) {
        try {
            // Unlock
            if (DEBUG) Log.d(TAG, "Defining version field " + key + " to " + value.toString());
            Field field = Build.VERSION.class.getDeclaredField(key);
            field.setAccessible(true);

            // Edit
            field.set(null, value);

            // Lock
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set version field " + key, e);
        }
    }

    private static boolean isGmsAddAccountActivityOnTop() {
        try {
            final ActivityTaskManager.RootTaskInfo focusedTask =
                    ActivityTaskManager.getService().getFocusedRootTaskInfo();
            return focusedTask != null && focusedTask.topActivity != null
                    && focusedTask.topActivity.equals(GMS_ADD_ACCOUNT_ACTIVITY);
        } catch (Exception e) {
            Log.e(TAG, "Unable to get top activity!", e);
        }
        return false;
    }

    public static boolean shouldBypassTaskPermission(Context context) {
        // GMS doesn't have MANAGE_ACTIVITY_TASKS permission
        final int callingUid = Binder.getCallingUid();
        final int gmsUid;
        try {
            gmsUid = context.getPackageManager().getApplicationInfo(PACKAGE_GMS, 0).uid;
            dlog("shouldBypassTaskPermission: gmsUid:" + gmsUid + " callingUid:" + callingUid);
        } catch (Exception e) {
            Log.e(TAG, "shouldBypassTaskPermission: unable to get gms uid", e);
            return false;
        }
        return gmsUid == callingUid;
    }

    private static boolean isCallerSafetyNet() {
        return sIsGms && Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("DroidGuard"));
    }

    public static void onEngineGetCertificateChain() {
        // Check stack for SafetyNet or Play Integrity
        if (isCallerSafetyNet() || sIsFinsky) {
            Log.i(TAG, "Blocked key attestation sIsGms=" + sIsGms + " sIsFinsky=" + sIsFinsky);
            throw new UnsupportedOperationException();
        }
    }

    public static void dlog(String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }

}
