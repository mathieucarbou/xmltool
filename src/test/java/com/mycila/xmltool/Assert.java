/**
 * Copyright (C) 2008 Mathieu Carbou <mathieu.carbou@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mycila.xmltool;

import static org.testng.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import static java.lang.String.format;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Assert {

    public static AssertException assertThrow(Class<? extends Throwable> exceptionClass) {
        return new AssertException(exceptionClass);
    }

    public static class AssertException {
        private final Class<? extends Throwable> exceptionClass;
        private String message;

        private AssertException(Class<? extends Throwable> exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        public AssertException withMessage(String message) {
            this.message = message;
            return this;
        }

        public void whenRunning(Code code) {
            try {
                code.run();
                fail(format("Should have thrown Exception class '%s'%s", exceptionClass.getName(), message == null ? "" : format(" with message '%s'", message)));
            } catch (Throwable throwable) {
                if (!exceptionClass.isAssignableFrom(throwable.getClass())) {
                    throw new AssertionError("Received bad exception class. Exception:\n" + asString(throwable) + "expected:<" + exceptionClass.getName() + "> but was:<" + throwable.getClass().getName() + ">");
                }
                if (message != null && !message.equals(throwable.getMessage())) {
                    throw new AssertionError("Received bad exception message. Exception:\n" + asString(throwable) + "expected:<" + message + "> but was:<" + throwable.getMessage() + ">");
                }
            }
        }

        private String asString(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return sw.toString();
        }
    }

    public static interface Code {
        void run() throws Throwable;
    }
}
