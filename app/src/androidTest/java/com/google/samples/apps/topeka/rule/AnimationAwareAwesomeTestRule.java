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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A base class for triple-A Rules that control animation scales on devices to avoid timing errors.
 */
public abstract class AnimationAwareAwesomeTestRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new AnimationAwareAwesomeStatement(base);
    }

    /**
     * Override to set up your specific animation aware rule. Disable animations for example.
     *
     * @throws Throwable if setup fails (which will disable {@code after()} method).
     */
    protected void before() throws Throwable {
        // do nothing
    }

    /**
     * Override to tear down your specific animation aware rule. Re-enable animations for example.
     *
     * @throws Throwable if tear down fails (which would affect future animation related tests).
     */
    protected void after() throws Throwable {
        // do nothing
    }

    private class AnimationAwareAwesomeStatement extends Statement {

        private Statement mBase;

        public AnimationAwareAwesomeStatement(Statement base) {
            mBase = base;
        }

        @Override
        public void evaluate() throws Throwable {
            before();
            try {
                mBase.evaluate();
            } finally {
                after();
            }
        }
    }

}
