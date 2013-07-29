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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Arrays;

import static com.mycila.xmltool.Assert.assertThrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocBuilderTest extends AbstractTest {

    @Test
    public void test_new_doc() {
        assertSameDoc(XMLDocBuilder.newDocument(false).addRoot("html").toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_from_Document() {
        Document d = XMLDocBuilder.newDocument(false).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDocBuilder.from(d, false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputSource() {
        assertSameDoc(XMLDocBuilder.from(new InputSource(new StringReader("<html></html>")), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Reader() {
        assertSameDoc(XMLDocBuilder.from(new StringReader("<html></html>"), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputStream() {
        assertSameDoc(XMLDocBuilder.from(new ByteArrayInputStream("<html></html>".getBytes()), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_File() {
        assertSameDoc(XMLDocBuilder.from(new File("src/test/resources/doc.xhtml"), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Url() {
        assertSameDoc(XMLDocBuilder.from(XMLDocTest.class.getResource("/doc.xhtml"), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_String() {
        assertSameDoc(XMLDocBuilder.from("<html></html>", false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Source() {
        Document d = XMLDocBuilder.newDocument(false).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDocBuilder.from(new DOMSource(d), false).toDocument().getFirstChild().getNodeName(), "html");
        try {
            XMLDocBuilder.from(new Source() {
                public void setSystemId(String systemId) {
                }

                public String getSystemId() {
                    return null;
                }
            }, false);
            fail("should throw ");
        } catch (Exception e) {
            assertEquals(e.getClass(), XMLDocumentException.class);
        }
    }

    @Test
    public void test_from_malformed_source1() {
        assertThrow(XMLDocumentException.class).withMessage("Error creating XMLDoc. Please verify that the input source can be read and is well formed: org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 13; XML document structures must start and end within the same entity.").whenRunning(new Assert.Code() {
            public void run() throws Throwable {
                XMLDocBuilder.from(new StreamSource(new StringReader("<html><html>")), false);
            }
        });
    }

    @Test
    public void validate_URL() throws Exception {
        XMLTag doc = XMLDoc.from(readString("doc2.xhtml"), false);
        ValidationResult res = doc.validate(getClass().getResource("/doc.xsd"));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
        res = doc.validate(new StreamSource(getClass().getResource("/doc.xsd").openStream()));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
    }

    @Test
    public void validate_URL_invalid() throws Exception {
        XMLTag doc = XMLDoc.from(readString("doc3.xhtml"), false);
        ValidationResult res = doc.validate(getClass().getResource("/doc.xsd"));
        assertTrue(res.hasError());
        assertFalse(res.hasWarning());
        System.out.println(Arrays.deepToString(res.getErrorMessages()));
    }

    @Test
    public void test_from_malformed() {
        assertThrow(XMLDocumentException.class).withMessage("Error creating XMLDoc. Please verify that the input source can be read and is well formed: XML document structures must start and end within the same entity.").whenRunning(new Assert.Code() {
            public void run() throws Throwable {
                assertSameDoc(XMLDocBuilder.from("<html><html>", false).toDocument().getFirstChild().getNodeName(), "html");
            }
        });
    }

    @Test
    public void test_from_malformed_source2() {
        assertThrow(XMLDocumentException.class).withMessage("Error creating XMLDoc. Please verify that the input source can be read and is well formed: Premature end of file.").whenRunning(new Assert.Code() {
            public void run() throws Throwable {
                XMLDocBuilder.from("", false);
            }
        });
    }

    @Test
    public void test_namespace() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://ns1.com")
                .addNamespace("ns2", "http://ns2.com")
                .addRoot("html");
        assertSameDoc(doc.toString(), readString("test_namespace0.xml"));
    }

    @Test
    public void test_from_XMLTag() throws Exception {
        XMLTag tag = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://default")
                .addNamespace("toto", "http://toto")
                .addRoot("root")
                .addTag("toto:body")
                .gotoParent().addTag("toto:body")
                .addTag("div")
                .addTag("title").addText("Yo !")
                .gotoParent().addTag("div");
        System.out.println(tag);

        tag.gotoRoot().gotoTag("toto:body[2]/ns0:div[1]/ns0:title");
        assertEquals(tag.getCurrentTagName(), "title");

        XMLTag tag2 = XMLDoc.from(tag, false);
        System.out.println(tag2);

        assertEquals(tag.toString(), tag2.toString());
        assertEquals(tag2.getCurrentTagName(), "root");

        tag.gotoRoot().gotoTag("toto:body[2]").deleteChilds();
        System.out.println(tag);
        System.out.println(tag2);
        assertFalse(tag.toString().equals(tag2.toString()));
    }

}
