/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.topeka.rule;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A helper class that can set animation scales reflectively to avoid timing errors executing tests.
 */
class AnimationAwareWriter extends AnimationAwareReader {

    private static final String TAG = "AnimationAwareWriter";

    /**
     * Package manager command to grant permissions.
     */
    private static final String PM_GRANT_CMD = "pm grant ";

    /**
     * Constant permission name.
     */
    private static final String SET_ANIMATION_SCALE = "android.permission.SET_ANIMATION_SCALE";

    /**
     * Enabled scale value.
     */
    private static final float ENABLED = 1.0f;

    /**
     * Disabled scale value.
     */
    private static final float DISABLED = 0.0f;

    /**
     * Adb shell command to set a global setting.
     */
    private static final String SETTINGS_PUT_GLOBAL_CMD = "adb shell settings put global ";

    /**
     * Disables animations and transitions reflectively. Requires SET_ANIMATION_SCALE permission.
     *
     * @return True if animations are successfully disabled. False if write permission is denied.
     * @throws DisableAnimationScalesFailedException if an error occurred.
     */
    public static boolean tryToDisableAnimationsAndTransitions()
            throws DisableAnimationScalesFailedException {

        if (isWritePermissionDenied()) {
            Log.w(TAG, "Cannot disable animations. Requires " + SET_ANIMATION_SCALE + " granted.");
            return false;
        } else if (reflectivelySetAnimationScalesTo(DISABLED)) {
            return true;
        } else {
            throw new DisableAnimationScalesFailedException();
        }
    }

    /**
     * Enables animations and transitions reflectively. Requires SET_ANIMATION_SCALE permission.
     *
     * @return True if animations are successfully enabled. False if write permission is denied.
     * @throws EnableAnimationScalesFailedException if an error occurred.
     */
    public static boolean tryToEnableAnimationsAndTransitions()
            throws EnableAnimationScalesFailedException {

        if (isWritePermissionDenied()) {
            Log.w(TAG, "Cannot enable animations. Requires " + SET_ANIMATION_SCALE + " granted.");
            return false;
        } else if (reflectivelySetAnimationScalesTo(ENABLED)) {
            return true;
        } else {
            throw new EnableAnimationScalesFailedException();
        }
    }

    /**
     * Gets animation and transition scales reflectively. Requires SET_ANIMATION_SCALE permission.
     *
     * @return The current animation scales if successfully, null if required permission is denied.
     * @throws GetAnimationScalesFailedException if an error occurred.
     */
    public static float[] tryToGetAnimationsAndTransitions()
            throws GetAnimationScalesFailedException {

        if (isWritePermissionDenied()) {
            Log.w(TAG, "Cannot get animations. Requires " + SET_ANIMATION_SCALE + " granted.");
            return null;
        } else {
            float[] animationScales = reflectivelyGetAnimationScales();
            if (animationScales != null) {
                return animationScales;
            } else {
                throw new GetAnimationScalesFailedException();
            }
        }
    }

    /**
     * Sets animation and transition scales reflectively. Requires SET_ANIMATION_SCALE permission.
     *
     * @param scales The animation and transition scales to be set.
     * @return True if animations are successfully set. False if write permission is denied.
     * @throws SetAnimationScalesFailedException if an error occurred.
     */
    public static boolean tryToSetAnimationsAndTransitions(float[] scales)
            throws SetAnimationScalesFailedException {

        // Permission is not granted for M emulators. Testing workaround via global settings.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            grantSetAnimationScalePermissionForM();
            // TODO: This is not generic, depends on length and names, move to Wonder or Reader.
            // adb shell settings put global window_animation_scale 0
            setAnimationScaleGlobalSetting(Settings.Global.WINDOW_ANIMATION_SCALE, scales[0]);
            // adb shell settings put global transition_animation_scale 0
            setAnimationScaleGlobalSetting(Settings.Global.TRANSITION_ANIMATION_SCALE, scales[1]);
            // adb shell settings put global animator_duration_scale 0
            setAnimationScaleGlobalSetting(Settings.Global.ANIMATOR_DURATION_SCALE, scales[2]);
        }

        // Test exception on M devices.
        if (isWritePermissionDenied()) {
            Log.w(TAG, "Cannot set animations. Requires " + SET_ANIMATION_SCALE + " granted.");
            return false;
        } else if (reflectivelySetAnimationScales(scales)) {
            return true;
        } else {
            throw new SetAnimationScalesFailedException();
        }
    }

    /**
     * Try to grant permission for M+ devices using package manager.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void grantSetAnimationScalePermissionForM() {
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                PM_GRANT_CMD + InstrumentationRegistry.getTargetContext().getPackageName()
                        + " " + SET_ANIMATION_SCALE);
    }

    /**
     * Try to set an animation scale value via settings.
     *
     * @param name  The name of the scale to be set.
     * @param value The value to be set.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setAnimationScaleGlobalSetting(String name, float value) {
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                SETTINGS_PUT_GLOBAL_CMD + name + " " + value);
    }

    /**
     * Determines whether the permission to set animation scales has been denied to the user.
     *
     * @return True if the permission to set animation scales have been denied, false otherwise.
     */
    public static boolean isWritePermissionDenied() {
        return PackageManager.PERMISSION_DENIED == InstrumentationRegistry.getTargetContext()
                .checkCallingOrSelfPermission(SET_ANIMATION_SCALE);
    }

    /**
     * Determines whether all the scales received as parameter are disabled.
     *
     * @param scales The scales to be checked.
     * @return True if all the scales are disabled. False otherwise.
     */
    public static boolean allScalesDisabled(float[] scales) {
        for (float scale : scales) {
            if (scale != DISABLED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an array of length dimension containing disabled scales.
     *
     * @param length The length of the array.
     * @return The disabled scales array.
     */
    public static float[] createDisabledScalesArray(int length) {
        float[] scales = new float[length];
        for (int i = 0; i < length; i++) {
            scales[i] = DISABLED;
        }
        return scales;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static float[] reflectivelyGetAnimationScales() {
        try {
            final Object windowManagerObject = reflectivelyGetWindowManagerObject();
            float[] animationScales = reflectivelyGetAnimationScales(windowManagerObject);
            Log.d(TAG, "All animation scales get reflectively.");
            return animationScales;
        } catch (ClassNotFoundException cnfe) {
            Log.w(TAG, "Cannot get animation scales reflectively.", cnfe);
        } catch (NoSuchMethodException mnfe) {
            Log.w(TAG, "Cannot get animation scales reflectively.", mnfe);
        } catch (SecurityException se) {
            Log.w(TAG, "Cannot get animation scales reflectively.", se);
        } catch (InvocationTargetException ite) {
            Log.w(TAG, "Cannot get animation scales reflectively.", ite);
        } catch (IllegalAccessException iae) {
            Log.w(TAG, "Cannot get animation scales reflectively.", iae);
        } catch (RuntimeException re) {
            Log.w(TAG, "Cannot get animation scales reflectively.", re);
        }
        return null;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static boolean reflectivelySetAnimationScales(float[] animationScales) {
        try {
            final Object windowManagerObject = reflectivelyGetWindowManagerObject();
            reflectivelySetAnimationScales(windowManagerObject, animationScales);
            Log.d(TAG, "All animation scales set reflectively.");
            return true;
        } catch (ClassNotFoundException cnfe) {
            Log.w(TAG, "Cannot set animation scales reflectively.", cnfe);
        } catch (NoSuchMethodException mnfe) {
            Log.w(TAG, "Cannot set animation scales reflectively.", mnfe);
        } catch (SecurityException se) {
            Log.w(TAG, "Cannot set animation scales reflectively.", se);
        } catch (InvocationTargetException ite) {
            Log.w(TAG, "Cannot set animation scales reflectively.", ite);
            throw new SetAnimationScalesFailedException(getRootCauseMessage(ite)); // TEST API 23...
        } catch (IllegalAccessException iae) {
            Log.w(TAG, "Cannot set animation scales reflectively.", iae);
        } catch (RuntimeException re) {
            Log.w(TAG, "Cannot set animation scales reflectively.", re);
        }
        return false;
    }

    /**
     * Gets recursively the cause message wrapped in the throwable received as parameter.
     *
     * @param throwable The throwable.
     * @return The root cause message or null if no message was provided at creation time.
     */
    private static String getRootCauseMessage(Throwable throwable) {
        if (throwable.getCause() != null) {
            return getRootCauseMessage(throwable.getCause());
        }
        return throwable.getMessage();
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static boolean reflectivelySetAnimationScalesTo(float animationScale) {
        try {
            final Object windowManagerObject = reflectivelyGetWindowManagerObject();
            float[] animationScales = reflectivelyGetAnimationScales(windowManagerObject);
            for (int i = 0; i < animationScales.length; i++) {
                animationScales[i] = animationScale;
            }
            reflectivelySetAnimationScales(windowManagerObject, animationScales);
            Log.d(TAG, "All animation scales set to " + animationScale + " reflectively.");
            return true;
        } catch (ClassNotFoundException cnfe) {
            Log.w(TAG, "Cannot set animation scales to " + animationScale + " reflectively.", cnfe);
        } catch (NoSuchMethodException mnfe) {
            Log.w(TAG, "Cannot set animation scales to " + animationScale + " reflectively.", mnfe);
        } catch (SecurityException se) {
            Log.w(TAG, "Cannot set animation scales to " + animationScale + " reflectively.", se);
        } catch (InvocationTargetException ite) {
            Log.w(TAG, "Cannot set animation scales to " + animationScale + " reflectively.", ite);
        } catch (IllegalAccessException iae) {
            Log.w(TAG, "Cannot set animation scales to " + animationScale + " reflectively.", iae);
        } catch (RuntimeException re) {
            Log.w(TAG, "Cannot set animation scales to " + animationScale + " reflectively.", re);
        }
        return false;
    }

    private static Object reflectivelyGetWindowManagerObject()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        Class<?> windowManagerStubClazz = Class.forName("android.view.IWindowManager$Stub");
        Method asInterface = windowManagerStubClazz.getDeclaredMethod("asInterface", IBinder.class);

        Class<?> serviceManagerClazz = Class.forName("android.os.ServiceManager");
        Method getService = serviceManagerClazz.getDeclaredMethod("getService", String.class);
        IBinder windowManagerBinder = (IBinder) getService.invoke(null, "window");

        return asInterface.invoke(null, windowManagerBinder);
    }

    private static float[] reflectivelyGetAnimationScales(Object windowManagerObject)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        Class<?> windowManagerClazz = Class.forName("android.view.IWindowManager");
        final Method getScalesMethod = windowManagerClazz.getDeclaredMethod("getAnimationScales");

        return (float[]) getScalesMethod.invoke(windowManagerObject);
    }

    private static void reflectivelySetAnimationScales(Object windowManagerObject, float[] scales)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        Class<?> windowManagerClazz = Class.forName("android.view.IWindowManager");
        final Method setScalesMethod = windowManagerClazz.getDeclaredMethod("setAnimationScales",
                float[].class);

        setScalesMethod.invoke(windowManagerObject, new Object[]{scales});
    }

    /**
     * Error that's being thrown when your system has animations enabled and cannot be disabled.
     */
    private static class DisableAnimationScalesFailedException extends RuntimeException {
        public DisableAnimationScalesFailedException() {
            super("Failed to disable animations and transitions to properly execute this test.");
        }
    }

    /**
     * Error that's being thrown when animations have been disabled and cannot be re-enabled.
     */
    private static class EnableAnimationScalesFailedException extends RuntimeException {
        public EnableAnimationScalesFailedException() {
            super("Failed to re-enable animations and transitions after to execute this test.");
        }
    }

    /**
     * Error that's being thrown when animation scales cannot be retrieved.
     */
    private static class GetAnimationScalesFailedException extends RuntimeException {
        public GetAnimationScalesFailedException() {
            super("Failed to retrieve animations and transitions.");
        }
    }

    /**
     * Error that's being thrown when animation scales have been changed and cannot be restored.
     */
    private static class SetAnimationScalesFailedException extends RuntimeException {
        public SetAnimationScalesFailedException() {
            super("Failed to restore animations and transitions after to execute this test.");
        }

        public SetAnimationScalesFailedException(String msg) {
            super("Failed to restore animations and transitions after to execute this test.\n" +
                    msg);
        }
    }

    /**
     * Error that's being thrown when SET_ANIMATION_SCALE permission is denied.
     */
    public static class WritePermissionDeniedException extends RuntimeException {
        public WritePermissionDeniedException() {
            super("To properly execute this test, disable animations and transitions " +
                    "on your device or grant " + SET_ANIMATION_SCALE + " permission to debug.\n" +
                    "For more info check: http://goo.gl/qVu1yV");
        }
    }

}
