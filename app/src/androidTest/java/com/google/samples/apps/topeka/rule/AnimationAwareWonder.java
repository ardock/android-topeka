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

/**
 * A helper class that can disable and restore animation scales to avoid timing errors during tests.
 */
public class AnimationAwareWonder extends AnimationAwareWriter {

    /**
     * Supported scales length.
     */
    private static final int SUPPORTED_SCALES_LENGTH = 3;

    /**
     * Get animation scales via settings.
     */
    private static final boolean NOT_REFLECTIVELY = false;

    /**
     * Get animation scale via reflections.
     */
    private static final boolean REFLECTIVELY = true;

    /**
     * Retrieves and disables the three animation scales. Requires SET_ANIMATION_SCALE permission.
     * <p/>
     * In the order: WINDOW_ANIMATION_SCALE, TRANSITION_ANIMATION_SCALE and ANIMATOR_DURATION_SCALE.
     * <p/>
     * If new scales are added, throws an exception. Use AnimationAwareWriter methods in that case.
     *
     * @return The current animation scales if have been successfully disabled, null otherwise.
     * @throws RetrieveAndDisableAnimationsFailedException if an error occurred or new scales added.
     */
    public static float[] tryToRetrieveAndDisableAnimationsAndTransitions()
            throws RetrieveAndDisableAnimationsFailedException {

        // Use reflection to get animation scales. Requires SET_ANIMATION_SCALE permission.
        float[] currentScales = getAnimationScales(REFLECTIVELY);

        // If SET_ANIMATION_SCALE permission denied, get the scales using settings.
        if (currentScales == null) {
            currentScales = getAnimationScales(NOT_REFLECTIVELY);
        }

        // If all scales already disabled, do nothing.
        if (allScalesDisabled(currentScales)) {
            return null;
        }

        final float[] disabledScales = createDisabledScalesArray(SUPPORTED_SCALES_LENGTH);

        // If user scales length is supported, try to restore user scales.
        if (hasSupportedLength(currentScales) && tryToSetAnimationsAndTransitions(disabledScales)) {
            return currentScales;
        } else {
            throw new RetrieveAndDisableAnimationsFailedException();
        }
    }

    /**
     * Returns the current animation scales via reflection or settings.
     *
     * @param reflectively If True, use reflection. Use settings otherwise.
     * @return The current animation scales.
     */
    public static float[] getAnimationScales(boolean reflectively) {
        if (reflectively) {
            return tryToGetAnimationsAndTransitions();
        } else {
            return getAnimationScales();
        }
    }

    /**
     * Restores and enables the three animation scales. Requires SET_ANIMATION_SCALE permission.
     * <p/>
     * In the order: WINDOW_ANIMATION_SCALE, TRANSITION_ANIMATION_SCALE and ANIMATOR_DURATION_SCALE.
     * <p/>
     * If new scales are added, throws an exception. Use AnimationAwareWriter methods in that case.
     *
     * @param userScales The animation and transition scales to be restored, or null for no-op.
     * @return True if animations are successfully restored and enabled. False otherwise.
     * @throws RestoreAndEnableAnimationsFailedException if an error occurred or new scales added.
     */
    public static boolean tryToRestoreAndEnableAnimationsAndTransitions(float[] userScales)
            throws RestoreAndEnableAnimationsFailedException {

        // If userScales are missing or all scales already disabled, abort.
        if (userScales == null || allScalesDisabled(userScales)) {
            return false;
        }

        // If user scales length is supported, try to restore user scales.
        if (hasSupportedLength(userScales) && tryToSetAnimationsAndTransitions(userScales)) {
            return true;
        } else {
            throw new RestoreAndEnableAnimationsFailedException();
        }
    }

    /**
     * Returns whether the number of scales is actually supported.
     * <p/>
     * If the length is not supported, there is the option to extend or use AnimationAwareWriter.
     *
     * @param scales The array of scales.
     * @return True if the length of the array is supported. False otherwise.
     */
    private static boolean hasSupportedLength(float[] scales) {
        return scales.length == SUPPORTED_SCALES_LENGTH;
    }

    /**
     * Error that's being thrown when animations are enabled and cannot be retrieved and disabled.
     */
    private static class RetrieveAndDisableAnimationsFailedException extends RuntimeException {
        public RetrieveAndDisableAnimationsFailedException() {
            super("Failed to retrieve animation scales and disable animations and transitions.");
        }
    }

    /**
     * Error that's being thrown when animations were retrieved and cannot be restored and enabled.
     */
    private static class RestoreAndEnableAnimationsFailedException extends RuntimeException {
        public RestoreAndEnableAnimationsFailedException() {
            super("Failed to restore and enable previously retrieved animations and transitions.");
        }
    }

}
