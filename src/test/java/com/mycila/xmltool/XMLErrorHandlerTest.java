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

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;

import static com.mycila.xmltool.Assert.Code;
import static com.mycila.xmltool.Assert.assertThrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLErrorHandlerTest {

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void handle_events() throws Exception {
        XMLErrorHandler handler = new XMLErrorHandler();

        assertFalse(handler.hasError());
        assertFalse(handler.hasWarning());

        handler.error(new TransformerException(new IllegalArgumentException("TransformerException")));
        handler.error(new SAXParseException("SAXParseException", null));
        handler.fatalError(new SAXParseException("SAXParseException-Fatal", null));
        handler.fatalError(new TransformerException("TransformerException-Fatal"));
        handler.warning(new SAXParseException("SAXParseException-Warn", null));
        handler.warning(new TransformerException("TransformerException-Warn"));

        assertTrue(handler.hasError());
        assertTrue(handler.hasWarning());

        assertEquals(handler.getErrors().length, 4);
        assertEquals(handler.getWarnings().length, 2);

        assertEquals(handler.getErrorMessages()[0], "java.lang.IllegalArgumentException: TransformerException");
        assertEquals(handler.getWarningMessages()[1], "TransformerException-Warn");
    }

    @Test
    public void handle_events_and_throw1() throws Exception {
        assertThrow(SAXException.class).withMessage("SAXParseException").whenRunning(new Code() {
            @SuppressWarnings({"ThrowableInstanceNeverThrown"})
            public void run() throws Throwable {
                XMLErrorHandler handler = new XMLErrorHandler(true);
                handler.warning(new SAXParseException("SAXParseException-Warn", null));
                assertTrue(handler.hasWarning());
                assertFalse(handler.hasError());
                handler.error(new SAXParseException("SAXParseException", null));
            }
        });
    }

    @Test
    public void handle_events_and_throw2() throws Exception {
        assertThrow(TransformerException.class).withMessage("TransformerException").whenRunning(new Code() {
            @SuppressWarnings({"ThrowableInstanceNeverThrown"})
            public void run() throws Throwable {
                XMLErrorHandler handler = new XMLErrorHandler(true);
                handler.warning(new SAXParseException("SAXParseException-Warn", null));
                assertTrue(handler.hasWarning());
                assertFalse(handler.hasError());
                handler.error(new TransformerException("TransformerException"));
            }
        });

    }

    @Test
    public void handle_events_and_throw3() throws Exception {
        assertThrow(SAXException.class).withMessage("SAXParseException").whenRunning(new Code() {
            @SuppressWarnings({"ThrowableInstanceNeverThrown"})
            public void run() throws Throwable {
                XMLErrorHandler handler = new XMLErrorHandler(true);
                handler.warning(new SAXParseException("SAXParseException-Warn", null));
                assertTrue(handler.hasWarning());
                assertFalse(handler.hasError());
                handler.fatalError(new SAXParseException("SAXParseException", null));
            }
        });
    }
}

