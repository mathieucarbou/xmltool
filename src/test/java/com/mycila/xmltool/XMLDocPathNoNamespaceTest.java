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

import static com.mycila.xmltool.Assert.Code;
import static com.mycila.xmltool.Assert.assertThrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocPathNoNamespaceTest extends AbstractTest {

    @Test
    public void test_goto_xpath_get_from_cache() {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
        XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
        assertEquals(xpath.findNode(doc.getCurrentTag(), "head").getNodeName(), "ns2:head");
        assertEquals(xpath.findNode(doc.getCurrentTag(), "head").getNodeName(), "ns2:head");
        assertEquals(xpath.findNodes(doc.getCurrentTag(), "//*").length, 15);
        assertEquals(xpath.findNodes(doc.getCurrentTag(), "//*").length, 15);
    }

    @Test
    public void test_goto_xpath_inexisting_node() {
        assertThrow(XMLDocumentException.class).withMessage("Error executing xpath 'inexisting' from node 'html': Inexisting target node.").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag(), "inexisting");
            }
        });
    }

    @Test
    public void test_goto_xpath_inexisting_node2() {
        assertThrow(IllegalArgumentException.class).withMessage("XPath expression cannot be null").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag(), null);
            }
        });
    }

    @Test
    public void test_goto_xpath_inexisting_node3() {
        assertThrow(IllegalArgumentException.class).withMessage("XPath expression cannot be empty").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag(), "");
            }
        });
    }

    @Test
    public void invalid_xpath() {
        assertThrow(XMLDocumentException.class).withMessage("Error compiling xpath '..v': A location path was expected, but the following token was encountered:  ..v").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
                XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
                xpath.findNode(doc.getCurrentTag().getFirstChild(), "..v");
            }
        });
    }

    @Test
    public void xpath_findNodes_0() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
        XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
        assertEquals(xpath.findNodes(doc.getCurrentTag().getFirstChild(), "sss").length, 0);
    }

    @Test
    public void xpath_findNodes_0_for_invalid() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
        XMLDocPath xpath = new XMLDocPath((XMLDocDefinition) doc.getContext());
        assertEquals(xpath.findNodes(doc.getCurrentTag().getFirstChild(), "..v").length, 0);
    }

    @Test
    public void use_case() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/wantnons.xml"), true);
        doc.gotoChild("S:Body");
        assertEquals(doc.getText("isAccountActiveResponse/state"), "true");
    }

    @Test
    public void test_setText() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("person")
                .addTag("name").addCDATA("Math")
                .addTag("sex").addText("M")
                .addTag("age").addText("26")
                .addTag("hands")
                .addTag("left").addText("5 fingers")
                .addTag("right").addText("5 fingers")
                .gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name><![CDATA[Math]]></name><sex>M</sex><age>26</age><hands><left>5 fingers</left><right>5 fingers</right></hands></person>");
        doc
                .gotoChild("name").setText("Elise")
                .gotoChild("sex").setText("F")
                .gotoChild("age").setText("24")
                .gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name>Elise</name><sex>F</sex><age>24</age><hands><left>5 fingers</left><right>5 fingers</right></hands></person>");
        doc.setText("4 fingers", "hands/left").gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name>Elise</name><sex>F</sex><age>24</age><hands><left>4 fingers</left><right>5 fingers</right></hands></person>");
        doc.setTextIfExist("3 fingers", "hands/middle").gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name>Elise</name><sex>F</sex><age>24</age><hands><left>4 fingers</left><right>5 fingers</right></hands></person>");
        doc.setTextIfExist("3 fingers", "hands/right").gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name>Elise</name><sex>F</sex><age>24</age><hands><left>4 fingers</left><right>3 fingers</right></hands></person>");
    }

    @Test
    public void test_setCDATA() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("person")
                .addTag("name").addCDATA("Math")
                .addTag("sex").addText("M")
                .addTag("age").addText("26")
                .addTag("hands")
                .addTag("left").addText("5 fingers")
                .addTag("right").addText("5 fingers")
                .gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name><![CDATA[Math]]></name><sex>M</sex><age>26</age><hands><left>5 fingers</left><right>5 fingers</right></hands></person>");
        doc
                .gotoChild("name").setCDATA("Elise")
                .gotoChild("sex").setCDATA("F")
                .gotoChild("age").setCDATA("24")
                .gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name><![CDATA[Elise]]></name><sex><![CDATA[F]]></sex><age><![CDATA[24]]></age><hands><left>5 fingers</left><right>5 fingers</right></hands></person>");
        doc.setCDATA("4 fingers", "hands/left").gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name><![CDATA[Elise]]></name><sex><![CDATA[F]]></sex><age><![CDATA[24]]></age><hands><left><![CDATA[4 fingers]]></left><right>5 fingers</right></hands></person>");
        doc.setCDATAIfExist("3 fingers", "hands/middle").gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name><![CDATA[Elise]]></name><sex><![CDATA[F]]></sex><age><![CDATA[24]]></age><hands><left><![CDATA[4 fingers]]></left><right>5 fingers</right></hands></person>");
        doc.setCDATAIfExist("3 fingers", "hands/right").gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><person><name><![CDATA[Elise]]></name><sex><![CDATA[F]]></sex><age><![CDATA[24]]></age><hands><left><![CDATA[4 fingers]]></left><right><![CDATA[3 fingers]]></right></hands></person>");
    }

    @Test
    public void test_setAttribute() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/inner.xml"), true);
        doc.gotoChild(1).gotoChild("c");
        assertEquals(doc.hasAttribute("a"), true);
        assertEquals(doc.getAttribute("a"), "b");
        doc.setAttribute("a", "aa");
        assertEquals(doc.getAttribute("a"), "aa");
        doc.gotoRoot();
        doc.setAttribute("a", "bb", "b[1]/c");
        assertEquals(doc.getAttribute("a", "b[1]/c"), "bb");
        doc.setAttributeIfExist("inexisting", "bb", "b[1]/c");
        assertEquals(doc.hasAttribute("inexisting", "b[1]/c"), false);
        doc.setAttributeIfExist("a", "cc", "b[1]/c");
        assertEquals(doc.getAttribute("a", "b[1]/c"), "cc");

        doc.setAttributeIfExist("inexisting", "aa");
        try {
            doc.setAttribute("inexisting", "aa");
            fail();
        } catch (XMLDocumentException e) {
            assertEquals(e.getMessage() , "Element 'a' does not have attribute 'inexisting'");
        }
        doc.gotoRoot().gotoChild(1).gotoChild("c");
        doc.setAttributeIfExist("a", "cc");
        assertEquals(doc.getAttribute("a"), "cc");
    }

}
