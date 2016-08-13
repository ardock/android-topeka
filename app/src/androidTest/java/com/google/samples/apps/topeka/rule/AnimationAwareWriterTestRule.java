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
 * A test rule that will fail a test if animations are enabled and cannot be temporarily disabled.
 */
public class AnimationAwareWriterTestRule extends AnimationAwareAwesomeTestRule {

    private boolean mDisabled;

    @Override
    protected void before() throws Throwable {
        if (AnimationAwareWriter.isWritePermissionDenied()) {
            AnimationAwareReader.checkForDisabledAnimationsAndTransitions();
        } else if (AnimationAwareReader.isAnyAnimationEnabled()) {
            mDisabled = AnimationAwareWriter.TryToDisableAnimationsAndTransitions();
        }
    }

    @Override
    protected void after() throws Throwable {
        if (mDisabled) {
            AnimationAwareWriter.TryToEnableAnimationsAndTransitions();
        }
    }
}
