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

import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A helper class that can set animation scales reflectively to avoid timing errors executing tests.
 */
class AnimationAwareWriter extends AnimationAwareReader {

    private static final String TAG = "AnimationAwareWriter";
    private static final String SET_ANIMATION_SCALE = "android.permission.SET_ANIMATION_SCALE";
    private static final float DISABLED = 0.0f;
    private static final float ENABLED = 1.0f;

    /**
     * Disables animations and transitions. Requires SET_ANIMATION_SCALE permission granted.
     *
     * @return true if animations are successfully disabled, false if write permission is denied.
     * @throws DisableAnimationsFailedException if an error occurred.
     */
    public static boolean TryToDisableAnimationsAndTransitions()
            throws DisableAnimationsFailedException {

        if (isWritePermissionDenied()) {
            Log.w(TAG, "Cannot disable animations due to missing permission: SET_ANIMATION_SCALE.");
            return false;
        } else if (reflectivelySetAnimationScalesTo(DISABLED)) {
            return true;
        } else {
            throw new DisableAnimationsFailedException();
        }
    }

    /**
     * Enables animations and transitions. Sets the animation scales to the default values.
     *
     * @return true if animations are successfully enabled, false if write permission is denied.
     * @throws EnableAnimationsFailedException if an error occurred.
     */
    public static boolean TryToEnableAnimationsAndTransitions()
            throws EnableAnimationsFailedException {

        if (isWritePermissionDenied()) {
            Log.w(TAG, "Cannot enable animations due to missing permission: SET_ANIMATION_SCALE.");
            return false;
        } else if (reflectivelySetAnimationScalesTo(ENABLED)) {
            return true;
        } else {
            throw new EnableAnimationsFailedException();
        }
    }

    /**
     * Determines whether you have been denied the permission to set animation scales.
     *
     * @return true if the permission to set animation scales have been denied, false otherwise.
     */
    public static boolean isWritePermissionDenied() {
        return PackageManager.PERMISSION_DENIED == InstrumentationRegistry.getTargetContext()
                .checkCallingOrSelfPermission(SET_ANIMATION_SCALE);
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static boolean reflectivelySetAnimationScalesTo(float animationScale) {
        try {
            final Object windowManagerObject = reflectivelyGetWindowManagerObject();
            float[] currentScales = reflectivelyGetAnimationScales(windowManagerObject);
            for (int i = 0; i < currentScales.length; i++) {
                currentScales[i] = animationScale;
            }
            reflectivelySetAnimationScales(windowManagerObject, currentScales);
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
    private static class DisableAnimationsFailedException extends RuntimeException {
        public DisableAnimationsFailedException() {
            super("Failed to disable animations and transitions to properly execute this test.");
        }
    }

    /**
     * Error that's being thrown when animations have been disabled and cannot be re-enabled.
     */
    private static class EnableAnimationsFailedException extends RuntimeException {
        public EnableAnimationsFailedException() {
            super("Failed to re-enable animations and transitions after to execute this test.");
        }
    }
}
