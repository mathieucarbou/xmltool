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

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Arrays;

import static com.mycila.xmltool.Assert.Code;
import static com.mycila.xmltool.Assert.assertThrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocBuilderNoNamespaceTest extends AbstractTest {

    @Test
    public void test_new_doc() {
        assertSameDoc(XMLDocBuilder.newDocument(true).addRoot("html").toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_from_Document() {
        Document d = XMLDocBuilder.newDocument(true).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDocBuilder.from(d, true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputSource() {
        assertSameDoc(XMLDocBuilder.from(new InputSource(new StringReader("<html></html>")), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Reader() {
        assertSameDoc(XMLDocBuilder.from(new StringReader("<html></html>"), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputStream() {
        assertSameDoc(XMLDocBuilder.from(new ByteArrayInputStream("<html></html>".getBytes()), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_File() {
        assertSameDoc(XMLDocBuilder.from(new File("src/test/resources/doc.xhtml"), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Url() {
        assertSameDoc(XMLDocBuilder.from(XMLDocTest.class.getResource("/doc.xhtml"), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_String() {
        assertSameDoc(XMLDocBuilder.from("<html></html>", true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Source() {
        Document d = XMLDocBuilder.newDocument(true).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDocBuilder.from(new DOMSource(d), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_malformed_source1() {
        assertThrow(XMLDocumentException.class).withMessage("Error creating XMLDoc. Please verify that the input source can be read and is well formed: org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 13; XML document structures must start and end within the same entity.").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDocBuilder.from(new StreamSource(new StringReader("<html><html>")), true);
            }
        });
    }

    @Test
    public void validate_URL_NS() throws Exception {
        XMLTag doc  = XMLDoc.from(readString("doc2.xhtml"), false);
        ValidationResult res = doc.validate(getClass().getResource("/doc.xsd"));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
        res = doc.validate(new StreamSource(getClass().getResource("/doc.xsd").openStream()));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
    }

    @Test
    @Ignore
    //when we ignore namespace, validation becomes unpredictable on diffrent jdk versions
    public void validate_URL() throws Exception {
        XMLTag doc  = XMLDoc.from(readString("doc2.xhtml"), true);
        ValidationResult res = doc.validate(getClass().getResource("/doc.xsd"));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
        res = doc.validate(new StreamSource(getClass().getResource("/doc.xsd").openStream()));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
    }

    @Test
    @Ignore
    //when we ignore namespace, validation becomes unpredictable on diffrent jdk versions
    public void validate_URL_invalid() throws Exception {
        XMLTag doc  = XMLDoc.from(readString("doc3.xhtml"), true);
        ValidationResult res = doc.validate(getClass().getResource("/doc.xsd"));
        assertTrue(res.hasError());
        assertFalse(res.hasWarning());
        System.out.println(Arrays.deepToString(res.getErrorMessages()));
    }

    @Test
    public void test_from_malformed() {
        assertThrow(XMLDocumentException.class).withMessage("Error creating XMLDoc. Please verify that the input source can be read and is well formed: XML document structures must start and end within the same entity.").whenRunning(new Code() {
            public void run() throws Throwable {
                assertSameDoc(XMLDocBuilder.from("<html><html>", true).toDocument().getFirstChild().getNodeName(), "html");
            }
        });
    }

    @Test
    public void test_from_malformed_source2() {
        assertThrow(XMLDocumentException.class).withMessage("Error creating XMLDoc. Please verify that the input source can be read and is well formed: Premature end of file.").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDocBuilder.from("", true);
            }
        });
    }

    @Test
    public void test_namespace() throws Exception {
        XMLTag doc  = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://ns1.com")
                .addNamespace("ns2", "http://ns2.com")
                .addRoot("html");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_from_XMLTag() throws Exception {
        XMLTag tag = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://default")
                .addNamespace("toto", "http://toto")
                .addRoot("root")
                .addTag("toto:body")
                .gotoParent().addTag("toto:body")
                .addTag("div")
                .addTag("title").addText("Yo !")
                .gotoParent().addTag("div");
        System.out.println(tag);

        tag.gotoRoot().gotoTag("body[2]/div[1]/title");
        assertEquals(tag.getCurrentTagName(), "title");

        XMLTag tag2 = XMLDoc.from(tag, true);
        System.out.println(tag2);

        assertEquals(tag.toString(), tag2.toString());
        assertEquals(tag2.getCurrentTagName(), "root");

        tag.gotoRoot().gotoTag("body[2]").deleteChilds();
        System.out.println(tag);
        System.out.println(tag2);
        assertFalse(tag.toString().equals(tag2.toString()));
    }

}