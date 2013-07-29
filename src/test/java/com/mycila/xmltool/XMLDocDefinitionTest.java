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

import static com.mycila.xmltool.Assert.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocDefinitionTest {

    @Test
    public void test_rename() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false).addNamespace("t", "ns").addRoot("t:root");
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getRoot().getNodeName(), "t:root");
        def.rename(def.getRoot(), "t:toto");
        assertEquals(def.getRoot().getNodeName(), "t:toto");
        def.rename(def.getRoot(), "toto");
        assertEquals(def.getRoot().getNodeName(), "toto");
    }

    @Test
    public void test_createElement() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false).addNamespace("t", "ns").addRoot("t:root");
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getRoot().getNodeName(), "t:root");
        Element e = def.createElement("t:toto");
        assertEquals(e.getNodeName(), "t:toto");
        e = def.createElement("toto");
        assertEquals(e.getNodeName(), "toto");
    }

    @Test
    public void test_createAttribute() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false).addNamespace("t", "ns").addRoot("t:root");
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getRoot().getNodeName(), "t:root");
        Attr a = def.createAttribute(def.getRoot(), "t:toto", "val");
        assertEquals(a.getNodeName(), "t:toto");
        a = def.createAttribute(def.getRoot(), "toto", "val");
        assertEquals(a.getNodeName(), "toto");
    }

    @Test
    public void test_ctor() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getDocument(), doc.toDocument());
        def = new XMLDocDefinition(doc.getCurrentTag(), false);
        assertEquals(def.getDocument(), doc.toDocument());
    }

    @Test
    public void test_namepsace_generation() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getPrefix("http://ns3.com"), "ns2");
    }

    @Test
    public void test_getNamespaceURI_null() throws Exception {
        assertThrow(IllegalArgumentException.class).withMessage("prefix cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
                XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
                def.getNamespaceURI(null);
            }
        });
    }

    @Test
    public void test_getNamespaceURI() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getNamespaceURI("ns2"), "http://ns3.com");
        assertEquals(def.getNamespaceURI("inexisting"), "");
    }

    @Test
    public void test_getPrefix_null() throws Exception {
        assertThrow(IllegalArgumentException.class).withMessage("namespaceURI cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
                XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
                def.getPrefix(null);
            }
        });
    }

    @Test
    public void test_getPrefix() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        def.addDefaultNamespace("http://ns10.com");
        assertEquals(def.getPrefix("http://ns3.com"), "ns2");
        assertEquals(def.getPrefix("inexisting"), null);
        assertEquals(def.getPrefix("http://ns10.com"), "ns3");
    }

    @Test
    public void test_getPrefixes() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        def.addDefaultNamespace("http://ns3.com");
        Iterator it = def.getPrefixes("http://ns3.com");
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
    }

    @Test
    public void test_getPrefixes_null() throws Exception {
        assertThrow(IllegalArgumentException.class).withMessage("namespaceURI cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
                XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
                def.getPrefixes(null);
            }
        });
    }

    @Test
    public void test_createRoot() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
        doc.toDocument().removeChild(doc.getCurrentTag());
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.createRoot("a").getRoot().getNodeName(), "a");
    }

    @Test
    public void test_getEncoding() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), false);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getEncoding(), "ISO8859-1");

        doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
        def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getEncoding(), "UTF-8");
        System.out.println(XMLDoc.from(getClass().getResource("/xpath.xml"), false).toString());

        doc = XMLDoc.newDocument(false).addRoot("html");
        def = new XMLDocDefinition(doc.toDocument(), false);
        assertEquals(def.getEncoding(), "UTF-8");
    }

}
