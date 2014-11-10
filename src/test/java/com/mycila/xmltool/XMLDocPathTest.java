/**
 * Copyright (C) 2008 Mycila (mathieu.carbou@gmail.com)
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

import static com.mycila.xmltool.Assert.Code;
import static com.mycila.xmltool.Assert.assertThrow;
import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocPathTest extends AbstractTest {

    @Test
    public void test_goto_xpath_get_from_cache() {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
        XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
        assertEquals(xpath.findNode(doc.getCurrentTag(), "ns2:head").getNodeName(), "ns2:head");
        assertEquals(xpath.findNode(doc.getCurrentTag(), "ns2:head").getNodeName(), "ns2:head");
        assertEquals(xpath.findNodes(doc.getCurrentTag(), "//*").length, 15);
        assertEquals(xpath.findNodes(doc.getCurrentTag(), "//*").length, 15);
    }

    @Test
    public void test_goto_xpath_inexisting_node() {
        assertThrow(XMLDocumentException.class).withMessage("Error executing xpath 'ns2:inexisting' from node 'html': Inexisting target node.").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag(), "ns2:inexisting");
            }
        });
    }

    @Test
    public void test_goto_xpath_inexisting_node2() {
        assertThrow(IllegalArgumentException.class).withMessage("XPath expression cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag(), null);
            }
        });
    }

    @Test
    public void test_goto_xpath_inexisting_node3() {
        assertThrow(IllegalArgumentException.class).withMessage("XPath expression cannot be empty").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag(), "");
            }
        });
    }

    @Test
    public void invalid_xpath() {
        assertThrow(XMLDocumentException.class).withMessage("Error compiling xpath '..v' - A location path was expected, but the following token was encountered:  ..v").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag().getFirstChild(), "..v");
            }
        });
    }

    @Test
    public void xpath_findNodes_0() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
        XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
        assertEquals(xpath.findNodes(doc.getCurrentTag().getFirstChild(), "sss").length, 0);
    }

    @Test
    public void xpath_findNodes_0_for_invalid() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
        XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
        assertEquals(xpath.findNodes(doc.getCurrentTag().getFirstChild(), "..v").length, 0);
    }
}
