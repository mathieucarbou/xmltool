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

import javax.xml.XMLConstants;
import java.util.Iterator;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocDefinitionNoNamespaceTest {

    @Test
    public void test_rename() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addNamespace("t", "ns").addRoot("t:root");
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getRoot().getNodeName(), "root");
        assertEquals(def.rename(def.getRoot(), "t:toto").getNodeName(), "t:toto");
        assertEquals(def.rename(def.getRoot(), "toto").getNodeName(), "toto");
    }

    @Test
    public void test_createElement() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addNamespace("t", "ns").addRoot("t:root");
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getRoot().getNodeName(), "root");
        Element e = def.createElement("t:toto");
        assertEquals(e.getNodeName(), "toto");
        e = def.createElement("toto");
        assertEquals(e.getNodeName(), "toto");
    }

    @Test
    public void test_createAttribute() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addNamespace("t", "ns").addRoot("t:root");
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getRoot().getNodeName(), "root");
        Attr a = def.createAttribute(def.getRoot(), "t:toto", "val");
        assertEquals(a.getNodeName(), "toto");
        a = def.createAttribute(def.getRoot(), "toto", "val");
        assertEquals(a.getNodeName(), "toto");
    }

    @Test
    public void test_ctor() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getDocument(), doc.toDocument());
        def = new XMLDocDefinition(doc.getCurrentTag(), true);
        assertEquals(def.getDocument(), doc.toDocument());
    }

    @Test
    public void test_namepsace_generation() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertNull(def.getPrefix("http://ns3.com"));
    }

    @Test
    public void test_getNamespaceURI_null() throws Exception {
        assertThrow(IllegalArgumentException.class).withMessage("prefix cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
                XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
                def.getNamespaceURI(null);
            }
        });
    }

    @Test
    public void test_getNamespaceURI() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getNamespaceURI("ns2"), XMLConstants.NULL_NS_URI);
        assertEquals(def.getNamespaceURI("inexisting"), XMLConstants.NULL_NS_URI);
    }

    @Test
    public void test_getPrefix_null() throws Exception {
        assertThrow(IllegalArgumentException.class).withMessage("namespaceURI cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
                XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
                def.getPrefix(null);
            }
        });
    }

    @Test
    public void test_getPrefix() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        def.addDefaultNamespace("http://ns10.com");
        assertNull(def.getPrefix("http://ns3.com"));
        assertNull(def.getPrefix("inexisting"));
        assertNull(def.getPrefix("http://ns10.com"));
    }

    @Test
    public void test_getPrefixes() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        def.addDefaultNamespace("http://ns3.com");
        Iterator it = def.getPrefixes("http://ns3.com");
        assertFalse(it.hasNext());
    }

    @Test
    public void test_getPrefixes_null() throws Exception {
        assertThrow(IllegalArgumentException.class).withMessage("namespaceURI cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
                XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
                def.getPrefixes(null);
            }
        });
    }

    @Test
    public void test_createRoot() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
        doc.toDocument().removeChild(doc.getCurrentTag());
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.createRoot("a").getRoot().getNodeName(), "a");
    }

    @Test
    public void test_getEncoding() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath3.xml"), true);
        XMLDocDefinition def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getEncoding(), "ISO8859-1");

        doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
        def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getEncoding(), "UTF-8");
        System.out.println(XMLDoc.from(getClass().getResource("/xpath.xml"), true).toString());

        doc = XMLDoc.newDocument(true).addRoot("html");
        def = new XMLDocDefinition(doc.toDocument(), true);
        assertEquals(def.getEncoding(), "UTF-8");
    }

}