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

        final float[] currentScales = getAnimationScales();

        if (allScalesDisabled(currentScales)) {
            return null;
        }

        final float[] disabledScales = new float[]{DISABLED, DISABLED, DISABLED};

        if (currentScales.length == 3 && tryToSetAnimationsAndTransitions(disabledScales)) {
            return currentScales;
        } else {
            throw new RetrieveAndDisableAnimationsFailedException();
        }
    }

    /**
     * Restores and enables the three animation scales. Requires SET_ANIMATION_SCALE permission.
     * <p/>
     * In the order: WINDOW_ANIMATION_SCALE, TRANSITION_ANIMATION_SCALE and ANIMATOR_DURATION_SCALE.
     * <p/>
     * If new scales are added, throws an exception. Use AnimationAwareWriter methods in that case.
     *
     * @param savedScales The animation and transition scales to be restored, or null for no-op.
     * @return true if animations are successfully restored and enabled, false otherwise.
     * @throws RestoreAndEnableAnimationsFailedException if an error occurred or new scales added.
     */
    public static boolean tryToRestoreAndEnableAnimationsAndTransitions(float[] savedScales)
            throws RestoreAndEnableAnimationsFailedException {

        if (savedScales == null || allScalesDisabled(savedScales)) {
            return false;
        } else if (savedScales.length == 3 && tryToSetAnimationsAndTransitions(savedScales)) {
            return true;
        } else {
            throw new RestoreAndEnableAnimationsFailedException();
        }
    }

    private static boolean allScalesDisabled(float[] scales) {
        for (float scale : scales) {
            if (scale != DISABLED) {
                return false;
            }
        }
        return true;
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
