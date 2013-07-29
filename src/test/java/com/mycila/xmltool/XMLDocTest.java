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

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;

import static com.mycila.xmltool.Assert.Code;
import static com.mycila.xmltool.Assert.assertThrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocTest extends AbstractTest {

    @Test
    public void test_wiki_manual() throws Exception {
        System.out.println(XMLDoc.newDocument(false).addRoot("html").toString());

        System.out.println(XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addRoot("html"));

        System.out.println(XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/") // http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd
                .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0") // http://wicket.sourceforge.net/wicket-1.0.xsd
                .addRoot("html")
                .addTag("wicket:border")
                .gotoRoot().addTag("head")
                .addNamespace("other", "http://other-ns.com")
                .gotoRoot().addTag("other:foo")
                .toString());

        System.out.println(XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addNamespace("w", "http://wicket.sourceforge.net/wicket-1.0")
                .addRoot("html")
                .addTag("w:border").addText("one and...")
                .gotoChild(1).addText(" two !")
                .gotoParent().addTag("head")
                .addTag("title")
                .addAttribute("w:id", "title")
                .addText("This is my title with special characters: <\"!@#$%'^&*()>")
                .gotoParent()
                .addTag("body").addCDATA("Some data...")
                .gotoTag("ns0:body").addTag("child")
                .gotoParent().addCDATA("with special characters")
                .gotoTag("ns0:body").addCDATA("<\"!@#$%'^&*()>")
                .toString());

        System.out.println(XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("body")
                .addTag("span").addText("a span")
                .addTag("div").addText("a div")
                .toString());

        XMLTag doc = XMLDoc.from(getClass().getResource("/goto.xml"), false);
        String generatedNS = doc.getPefix("http://www.w3.org/2002/06/xhtml2/");
        doc.gotoChild("head")      // jump to the only 'head' tag under 'html'v
                .gotoChild()       // jump to the only child of 'head'
                .gotoRoot()        // go to 'html'
                .gotoChild(2)      // go to child 'body'
                .gotoChild(3)      // go to third child 'w:border' having text 'child3'
                .gotoRoot()        // return to root
                .gotoTag("%1$s:body/w:border[1]/%1$s:div", generatedNS); // xpath navigation

        doc = XMLDoc.from(getClass().getResource("/get.xml"), false);
        assertEquals(doc.getCurrentTag().getNodeType(), Document.ELEMENT_NODE);
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.getPefix("http://www.w3.org/2002/06/xhtml2/"), "ns1"); // ns0 is already used in the document
        assertEquals(doc.gotoTag("ns1:head/ns1:title").getText(), "my special title: <\"!@#$%'^&*()>");
        assertEquals(doc.getText("."), "my special title: <\"!@#$%'^&*()>");
        assertEquals(doc.getCDATA("../../ns1:body"), "my special data: <\"!@#$%'^&*()>");
        assertEquals(doc.getAttribute("ns0:id"), "titleID");

        ValidationResult results = XMLDoc.from(getClass().getResource("/goto.xml"), false).validate(
                new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"),
                new URL("http://wicket.sourceforge.net/wicket-1.0.xsd")
        );
        assertFalse(results.hasError());

        results = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addRoot("htmlZZ")
                .validate(new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"));
        assertTrue(results.hasError());
        System.out.println(Arrays.deepToString(results.getErrorMessages()));

        XMLDoc.newDocument(false).addRoot("html")
                .toResult(new DOMResult())
                .toStream(new StringWriter())
                .toStream(new ByteArrayOutputStream());
    }

    @Test
    public void test_getCDATA_with_getText() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("test").addCDATA("<\"!@#$%'^&*()>");
        System.out.println(doc.toString());
        System.out.println("Text: " + doc.gotoRoot().getText());
        System.out.println("CDATA: " + doc.gotoRoot().getCDATA());
        doc = XMLDoc.newDocument(false).addRoot("test").addText("    ");
        System.out.println("Text: " + doc.gotoRoot().getText());
    }

    @Test
    public void test_new_doc() {
        assertSameDoc(XMLDoc.newDocument(false).addRoot("html").toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_from_Document() {
        Document d = XMLDoc.newDocument(false).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDoc.from(d, false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputSource() {
        assertSameDoc(XMLDoc.from(new InputSource(new StringReader("<html></html>")), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Reader() {
        assertSameDoc(XMLDoc.from(new StringReader("<html></html>"), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputStream() {
        assertSameDoc(XMLDoc.from(new ByteArrayInputStream("<html></html>".getBytes()), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_File() {
        assertSameDoc(XMLDoc.from(new File("src/test/resources/doc.xhtml"), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Url() {
        assertSameDoc(XMLDoc.from(XMLDocTest.class.getResource("/doc.xhtml"), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_String() {
        assertSameDoc(XMLDoc.from("<html></html>", false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Source() {
        Document d = XMLDoc.newDocument(false).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDoc.from(new DOMSource(d), false).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_toString() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html").addTag("head").gotoParent().addTag("body").addText("�a �t� ���");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><head/><body>�a �t� ���</body></html>");
        assertTrue(doc.toString("ASCII").startsWith("<?xml version=\"1.0\" encoding=\"ASCII\" standalone=\"no\"?>"));
    }

    @Test
    public void test_toDocument() {
        Document doc = XMLDoc.newDocument(false).addRoot("html").addTag("head").gotoParent().addTag("body").addText("�a �t� ���").toDocument();
        assertSameDoc(doc.getFirstChild().getFirstChild().getNodeName(), "head");
    }

    @Test
    public void test_addNode() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html")
                .addTag("head")
                .gotoRoot()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><head/><body/></html>");
    }

    @Test
    public void test_addAttribute() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html")
                .addAttribute("lang", "en")
                .addTag("body")
                .addAttribute("onload", "func1")
                .addAttribute("onclick", "1<2");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html lang=\"en\"><body onclick=\"1&lt;2\" onload=\"func1\"/></html>");
    }

    @Test
    public void test_addAttribute_existing() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Attribute 'onclick' already exist on tag 'body'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html")
                        .addAttribute("lang", "en")
                        .addTag("body")
                        .addAttribute("onload", "func1")
                        .addAttribute("onclick", "func2")
                        .addAttribute("onclick", "1<2");
            }
        });
    }

    @Test
    public void test_addDefaultNamespace() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://myns")
                .addRoot("html")
                .addAttribute("lang", "en");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://myns\" xmlns:NS1=\"http://myns\" NS1:lang=\"en\"/>");

        doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://myns")
                .addRoot("ns0:html")
                .addAttribute("lang", "en");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns0:html xmlns:ns0=\"http://myns\" ns0:lang=\"en\"/>");

        doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://myns")
                .addRoot("html");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://myns\"/>");

        doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://myns")
                .addRoot("ns0:html");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns0:html xmlns:ns0=\"http://myns\"/>");

        doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://myns")
                .addRoot("ns1:html");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns1:html/>");
    }

    @Test
    public void test_addNamespace_already_bound_xml() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Prefix 'xml' is already bound to another namespace 'http://www.w3.org/XML/1998/namespace'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").addNamespace("xml", "http://ns0");
            }
        });
    }

    @Test
    public void test_addNamespace_already_bound_xmlns() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Prefix 'xmlns' is already bound to another namespace 'http://www.w3.org/2000/xmlns/'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").addNamespace("xmlns", "http://ns0");
            }
        });
    }

    @Test
    public void test_addNamespace_already_bound() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Prefix 'ns0' is already bound to another namespace 'http://def'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false)
                        .addDefaultNamespace("http://def")
                        .addRoot("html")
                        .addNamespace("ns0", "http://ns0");
            }
        });
    }

    @Test
    public void test_addNamespace_default() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Prefix '' is already bound to another namespace ''").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false)
                        .addRoot("html")
                        .addNamespace("", "http://ns");
            }
        });
    }

    @Test
    public void test_addNamespace() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("ns1:body")
                .addNamespace("ns1", "http://ns");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><ns1:body/></html>");

        doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addNamespace("ns1", "http://ns")
                .addTag("ns1:body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><ns1:body xmlns:ns1=\"http://ns\"/></html>");

        doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://myns")
                .addRoot("ns1:html")
                .addTag("head").gotoParent()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns1:html><head xmlns=\"http://myns\"/><body xmlns=\"http://myns\"/></ns1:html>");

        doc = XMLDoc.newDocument(false)
                .addNamespace("ns1", "http://myns")
                .addDefaultNamespace("http://def")
                .addRoot("ns1:html")
                .addTag("head").gotoParent()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns1:html xmlns:ns1=\"http://myns\"><head xmlns=\"http://def\"/><body xmlns=\"http://def\"/></ns1:html>");

        doc = XMLDoc.newDocument(false)
                .addNamespace("ns1", "http://myns")
                .addDefaultNamespace("http://def")
                .addRoot("html")
                .addTag("ns1:head").gotoParent()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\"><ns1:head xmlns:ns1=\"http://myns\"/><body/></html>");

        doc = XMLDoc.newDocument(false)
                .addNamespace("ns1", "http://ns1")
                .addDefaultNamespace("http://def")
                .addRoot("html")
                .addAttribute("ns1:attr", "val")
                .addTag("ns1:head")
                .addAttribute("attr", "val")
                .addTag("body")
                .addAttribute("attr", "val");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\" xmlns:ns1=\"http://ns1\" ns1:attr=\"val\"><ns1:head xmlns:ns0=\"http://def\" ns0:attr=\"val\"><body ns0:attr=\"val\"/>    </ns1:head></html>");

    }

    @Test
    public void test_same_namespace() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/namespace.xml"), false);
        System.out.println(doc.toString());
    }

    @Test
    public void test_withText() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addText("1<2");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>1&lt;2</html>");

        doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addText("1<2")
                .addTag("body")
                .gotoParent()
                .addText("bla")
                .addText("aaa");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>1&lt;2<body/>blaaaa</html>");
    }

    @Test
    public void test_withCDATA() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addCDATA("1<2");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><![CDATA[1<2]]></html>");

        doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addCDATA("1<2")
                .addTag("body")
                .gotoParent().addCDATA("bla").addCDATA("1<2");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><![CDATA[1<2]]><body/><![CDATA[bla]]><![CDATA[1<2]]></html>");
    }

    @Test
    public void test_deleteChilds() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html").deleteChilds();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");

        doc = XMLDoc.newDocument(false).addRoot("html").addAttribute("a", "b").deleteChilds();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\"/>");

        doc = XMLDoc.newDocument(false).addRoot("html")
                .addAttribute("a", "b")
                .addTag("head")
                .addAttribute("a", "b");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\"><head a=\"b\"/></html>");

        doc.gotoRoot().deleteChilds();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\"/>");
    }

    @Test
    public void test_delete_root() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Cannot delete root node 'html'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").delete();
            }
        });
    }

    @Test
    public void test_delete() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("aa").addAttribute("a", "b").addTag("bb");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><aa a=\"b\"><bb/></aa></html>");
        doc.delete();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><aa a=\"b\"/></html>");
        doc.delete();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_deleteAttributes() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .deleteAttributes();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");

        doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addAttribute("a", "b")
                .addAttribute("b", "c")
                .addAttribute("d", "e")
                .addText("blabla")
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" b=\"c\" d=\"e\">blabla<body/></html>");

        doc.gotoRoot().deleteAttributes();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>blabla<body/></html>");

        doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://def")
                .addNamespace("ns", "http://ns")
                .addRoot("html")
                .addAttribute("ns:a", "b")
                .addAttribute("b", "c")
                .addAttribute("ns:d", "e")
                .addText("blabla")
                .addTag("ns:body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\" xmlns:ns=\"http://ns\" xmlns:ns0=\"http://def\" ns0:b=\"c\" ns:a=\"b\" ns:d=\"e\">blabla<ns:body/></html>");

        doc.gotoRoot().deleteAttributes();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\">blabla<ns:body xmlns:ns=\"http://ns\"/></html>");
    }

    @Test
    public void test_deleteAttribute_inexisting() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Cannot delete attribute 'd' from element 'html': attribute does noe exist").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").deleteAttribute("d");
            }
        });
    }

    @Test
    public void deleteAttributeIfExists() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addAttribute("b", "c");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html b=\"c\"/>");
        assertSameDoc(doc.deleteAttributeIfExists("q").toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html b=\"c\"/>");
        assertSameDoc(doc.deleteAttributeIfExists("b").toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_deleteAttribute() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("e", "f");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" c=\"d\" e=\"f\"/>");

        doc.deleteAttribute("c");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" e=\"f\"/>");

        doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://def")
                .addNamespace("ns", "http://ns")
                .addRoot("html")
                .addAttribute("ns:a", "b")
                .addAttribute("b", "c")
                .addAttribute("ns:d", "e")
                .addText("blabla")
                .addCDATA("data");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\" xmlns:ns=\"http://ns\" xmlns:ns0=\"http://def\" ns0:b=\"c\" ns:a=\"b\" ns:d=\"e\">blabla<![CDATA[data]]></html>");

        doc.deleteAttribute("ns:a");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\" xmlns:ns=\"http://ns\" xmlns:ns0=\"http://def\" ns0:b=\"c\" ns:d=\"e\">blabla<![CDATA[data]]></html>");

        doc.deleteAttribute("b");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\" xmlns:ns=\"http://ns\" ns:d=\"e\">blabla<![CDATA[data]]></html>");

        doc.deleteAttribute("ns:d");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://def\" xmlns:ns=\"http://ns\">blabla<![CDATA[data]]></html>");
    }

    @Test
    public void findAttribute() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addAttribute("b", "c");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html b=\"c\"/>");
        assertEquals(doc.findAttribute("q"), null);
        assertEquals(doc.findAttribute("b"), "c");
    }

    @Test
    public void findAttributeXPath() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("child")
                .addAttribute("b", "c")
                .gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><child b=\"c\"/></html>");
        assertEquals(doc.findAttribute("q", "child"), null);
        assertEquals(doc.findAttribute("b", "child"), "c");
    }

    @Test
    public void test_renameTo() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/1999/xhtml")
                .addRoot("html")
                .addAttribute("attr", "val")
                .addTag("child")
                .gotoRoot();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:NS1=\"http://www.w3.org/1999/xhtml\" NS1:attr=\"val\"><child/></html>");

        doc.gotoRoot().renameTo("xhtml");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><xhtml xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:NS1=\"http://www.w3.org/1999/xhtml\" NS1:attr=\"val\"><child/></xhtml>");
    }

    @Test
    public void test_renameTo_namepace() throws Exception {
        XMLTag doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/1999/xhtml")
                .addRoot("html")
                .addAttribute("attr", "val")
                .addTag("child");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:NS1=\"http://www.w3.org/1999/xhtml\" NS1:attr=\"val\"><child/></html>");

        doc.gotoRoot().renameTo("ns1:xhtml");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns1:xhtml xmlns=\"\" xmlns:NS1=\"http://www.w3.org/1999/xhtml\" NS1:attr=\"val\"><child xmlns=\"http://www.w3.org/1999/xhtml\"/></ns1:xhtml>");

        doc.addNamespace("ns1", "http://ns1.com").renameTo("ns1:thtml").addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns1:thtml xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:NS1=\"http://www.w3.org/1999/xhtml\" NS1:attr=\"val\" xmlns:ns1=\"http://ns1.com\"><child/><body/></ns1:thtml>");
    }

    @Test
    public void test_gotoParent() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html");
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.gotoParent().getCurrentTagName(), "html");

        doc.addNamespace("ns", "http://ns").addTag("ns:body");
        assertEquals(doc.getCurrentTagName(), "ns:body");
        assertEquals(doc.gotoParent().getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoRoot() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html");
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.gotoRoot().getCurrentTagName(), "html");

        doc.addNamespace("ns", "http://ns").addTag("ns:body");
        assertEquals(doc.getCurrentTagName(), "ns:body");
        assertEquals(doc.gotoRoot().getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoNode_inexisting() {
        assertThrow(XMLDocumentException.class).withMessage("Error executing xpath 'a' from node 'html': Inexisting target node.").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").gotoTag("a");
            }
        });
    }

    @Test
    public void test_gotoNode_bad_xpath() {
        assertThrow(XMLDocumentException.class).withMessage("Error compiling xpath ':-)': A location path was expected, but the following token was encountered:  :-").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").gotoTag(":-)");
            }
        });
    }

    @Test
    public void test_gotoNode_not_target_element() {
        assertThrow(XMLDocumentException.class).withMessage("XPath expression '..' does not target an element. Targeted node is '#document' (node type is '9')").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").gotoTag("..");
            }
        });
    }

    @Test
    public void test_gotoNode_not_target_element2() {
        assertThrow(XMLDocumentException.class).withMessage("XPath expression 'text()' does not target an element. Targeted node is '#text' (node type is '3')").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").addText("text").gotoTag("text()");
            }
        });
    }

    @Test
    public void test_gotoNode_this() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html");
        assertEquals(doc.gotoTag(".").getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoNode_parent() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html").addTag("head");
        assertEquals(doc.gotoTag("..").getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoNode_child() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html")
                .addTag("body").addText("txt")
                .addTag("head").addText("txt1")
                .addTag("head").addText("txt2");
        assertEquals(doc.gotoRoot().gotoTag("body").getText(), "txt");
        assertEquals(doc.gotoRoot().gotoTag("head[1]").getText(), "txt1");
        assertEquals(doc.gotoRoot().gotoTag("head[2]").getText(), "txt2");
    }

    @Test
    public void test_gotoNode_namespace() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addNamespace("w", "http://wicket.sourceforge.net/wicket-1.0")
                .addRoot("html")
                .addTag("w:border")
                .gotoParent()
                .addTag("head")
                .addTag("title")
                .addAttribute("w:id", "title");
        assertEquals(doc.gotoRoot().gotoTag("ns0:head").getCurrentTagName(), "head");
        assertEquals(doc.gotoRoot().gotoTag("w:border").getCurrentTagName(), "w:border");
    }

    @Test
    public void test_gotoChild_no_child() {
        assertThrow(XMLDocumentException.class).withMessage("Current element 'html' has no child").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(false).addRoot("html").addText("txt");
                doc.gotoChild();
            }
        });
    }

    @Test
    public void test_gotoChild_too_many() {
        assertThrow(XMLDocumentException.class).withMessage("Cannot select child: current element 'html' has '2' children").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(false)
                        .addRoot("html").addText("aa")
                        .addTag("child").addText("txt")
                        .gotoRoot().addTag("child");
                doc.gotoRoot().gotoChild();
            }
        });
    }

    @Test
    public void test_gotoChild() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html").addText("aa")
                .addTag("child").addText("txt");
        assertEquals(doc.gotoRoot().gotoChild().getText(), "txt");
    }

    @Test
    public void test_gotoChild_with_ns() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://ns1")
                .addNamespace("ns2", "http://ns2")
                .addRoot("html").addText("aa")
                .addTag("ns2:child").addText("txt");
        assertEquals(doc.gotoRoot().gotoChild().getText(), "txt");
    }

    @Test
    public void test_gotoChild_i_inexisting1() {
        assertThrow(XMLDocumentException.class).withMessage("Cannot acces child '0' of element 'html' amongst its '2' childs").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(false)
                        .addRoot("html").addTag("child")
                        .gotoRoot().addTag("child");
                doc.gotoRoot().gotoChild(0);
            }
        });
    }

    @Test
    public void test_gotoChild_i_inexisting2() {
        assertThrow(XMLDocumentException.class).withMessage("Cannot acces child '3' of element 'html' amongst its '2' childs").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(false)
                        .addRoot("html").addTag("child")
                        .gotoRoot().addTag("child");
                doc.gotoRoot().gotoChild(3);
            }
        });
    }

    @Test
    public void test_gotoChild_i() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html").addTag("child").addText("txt1")
                .gotoRoot().addTag("child").addText("txt2");
        assertEquals(doc.gotoRoot().gotoChild(1).getText(), "txt1");
    }

    @Test
    public void test_gotoChild_name() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("child").addText("txt1")
                .gotoRoot().addTag("child").addText("txt2")
                .gotoRoot().addTag("other").addText("txt3");
        assertEquals(doc.gotoRoot().gotoChild("other").getText(), "txt3");
    }

    @Test
    public void test_gotoChild_name_too_many() {
        assertThrow(XMLDocumentException.class).withMessage("Cannot select child: current element 'html' has '2' children named 'child'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(false)
                        .addRoot("html")
                        .addTag("child").addText("txt1")
                        .gotoRoot().addTag("child").addText("txt2");
                doc.gotoRoot().gotoChild("child");
            }
        });
    }

    @Test
    public void test_gotoChild_name_noone() {
        assertThrow(XMLDocumentException.class).withMessage("Current element 'html' has no child named 'aa'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(false)
                        .addRoot("html")
                        .addTag("child").addText("txt1")
                        .gotoRoot().addTag("child").addText("txt2")
                        .gotoRoot().addTag("other").addText("txt3");
                doc.gotoRoot().gotoChild("aa");
            }
        });
    }

    @Test
    public void test_gotoChild_name_noone2() {
        assertThrow(XMLDocumentException.class).withMessage("Current element 'html' has no child named 'aa'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(false).addRoot("html");
                doc.gotoRoot().gotoChild("aa");
            }
        });
    }

    @Test
    public void test_getCurrentTag_Name() {
        assertSameDoc(XMLDoc.newDocument(false).addRoot("html").getCurrentTag().getNodeName(), "html");
        assertSameDoc(XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("title")
                .getCurrentTagName(), "title");
        assertSameDoc(XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("title")
                .gotoParent().gotoParent().gotoParent()
                .getCurrentTag().getNodeName(), "html");
        assertSameDoc(XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("title")
                .gotoParent().gotoParent().gotoParent()
                .addTag("body")
                .gotoRoot()
                .getCurrentTagName(), "html");
    }

    @Test
    public void test_context_and_getprefix() {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), false);
        assertEquals(doc.getPefix("http://ns3.com"), "ns0");
        assertEquals(doc.getPefix("http://ns4.com"), "ns3");
        assertEquals(doc.getPefix("http://inexisting"), "");
        assertEquals(doc.getContext().getNamespaceURI("ns0"), "http://ns3.com");
        assertEquals(doc.getContext().getNamespaceURI("ns1"), "http://ns1.com");
        assertEquals(doc.getContext().getNamespaceURI("ns2"), "http://ns2.com");
        assertEquals(doc.getContext().getNamespaceURI("ns3"), "http://ns4.com");
    }

    @Test
    public void test_context_and_getprefixes() {
        XMLTag doc = XMLDoc.from(getClass().getResource("/namespace.xml"), false);
        assertEquals(doc.getPefixes("").length, 1);
        assertEquals(doc.getPefixes("inexisting").length, 0);
        assertEquals(doc.getPefixes("http://www.w3.org/2002/06/xhtml2/").length, 2);
    }

    @Test
    public void test_getText() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html");
        assertSameDoc(doc.getText(), "");

        doc.addText("1<2");
        assertSameDoc(doc.getText(), "1<2");

        doc.addTag("body")
                .gotoParent()
                .addText("bla")
                .addText("aaa");
        assertSameDoc(doc.getText(), "1<2blaaaa");
    }

    @Test
    public void test_getText_xpath() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("nottext")
                .gotoParent()
                .addText("1<2")
                .addTag("title").addText("some text")
                .addText("bb")
                .gotoTag("title").addText(" and again");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><nottext/>1&lt;2<title>some text and again</title>bb</html>");
        assertEquals(doc.getText("."), "1<2bb");
        assertEquals(doc.getText("nottext"), "");
        assertEquals(doc.getText("title"), "some text and again");
        assertEquals(doc.gotoChild("title").getText("."), "some text and again");
        assertEquals(doc.getText("../nottext"), "");
        assertEquals(doc.getText(".."), "1<2bb");
    }

    @Test
    public void test_getTextOrCDATA() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("node1").addText("text")
                .addTag("node2").addCDATA("data")
                .addTag("node3").addText("text")
                .gotoChild("node3").addCDATA("&data")
                .gotoRoot();
        assertEquals(doc.getTextOrCDATA("node1"), "text");
        assertEquals(doc.getTextOrCDATA("node2"), "data");
        assertEquals(doc.getTextOrCDATA("node3"), "text");
    }

    @Test
    public void test_getCDATA() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html");
        assertSameDoc(doc.getCDATA(), "");

        doc.addCDATA("1<2");
        assertSameDoc(doc.getCDATA(), "1<2");

        doc.addTag("body")
                .gotoParent()
                .addCDATA("bla")
                .addCDATA("aaa");
        assertSameDoc(doc.getCDATA(), "1<2blaaaa");
    }

    @Test
    public void test_getCDATA_xpath() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addRoot("html")
                .addTag("nottext")
                .gotoParent()
                .addCDATA("<\"!@#$%'^&*()>")
                .addTag("title").addCDATA("some text")
                .addCDATA("bb")
                .gotoTag("title").addCDATA(" and again");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>    <nottext/><![CDATA[<\"!@#$%'^&*()>]]>    <title><![CDATA[some text]]><![CDATA[ and again]]></title><![CDATA[bb]]></html>");
        assertEquals(doc.getCDATA("."), "<\"!@#$%'^&*()>bb");
        assertEquals(doc.getCDATA("nottext"), "");
        assertEquals(doc.getCDATA("title"), "some text and again");
        assertEquals(doc.gotoChild("title").getCDATA("."), "some text and again");
        assertEquals(doc.getCDATA("../nottext"), "");
        assertEquals(doc.getCDATA(".."), "<\"!@#$%'^&*()>bb");
    }

    @Test
    public void test_getAttributeNames() {
        XMLTag doc = XMLDoc.newDocument(false).addRoot("html").addNamespace("ns0", "http://ns0");
        assertEquals(doc.getAttributeNames().length, 0);

        doc.addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("ns0:e", "f");
        assertEquals(doc.getAttributeNames().length, 3);
        assertEquals(doc.getAttributeNames()[0], "a");
    }

    @Test
    public void test_forEach() {
        XMLDoc.from(getClass().getResource("/get.xml"), false).forEach(new CallBack() {
            public void execute(XMLTag doc) {
                System.out.println(doc.getCurrentTagName() + "'s text: " + doc.getText());
                System.out.println(doc.getCurrentTagName() + "'s data: " + doc.getCDATA());
            }
        }, "//*");
    }

    @Test
    public void test_getAttribute_error() {
        assertThrow(XMLDocumentException.class).withMessage("Element 'html' does not have attribute 'inexisting'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(false).addRoot("html").getAttribute("inexisting");
            }
        });
    }

    @Test
    public void test_getAttribute() {
        XMLTag doc = XMLDoc.newDocument(false).addDefaultNamespace("http://def").addRoot("html").addNamespace("ns1", "http://ns1");
        assertEquals(doc.getAttributeNames().length, 1);

        doc.addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("ns1:e", "f");
        assertEquals(doc.getAttributeNames().length, 4);
        assertEquals(doc.getAttributeNames()[0], "a");
        assertEquals(doc.getAttributeNames()[3], "xmlns");
    }

    @Test
    public void test_getAttribute_xpath() {
        XMLTag doc = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://def")
                .addRoot("html")
                .addTag("foo")
                .addNamespace("ns1", "http://ns1")
                .addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("ns1:e", "f")
                .gotoRoot();
        String ns = doc.getPefix("http://def");
        assertEquals(doc.getAttribute("a", "%s:foo", ns), "b");
        assertEquals(doc.getAttribute("ns1:e", "%s:foo", ns), "f");
    }

    @Test
    public void validate_URL() throws Exception {
        ValidationResult res = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0")
                .addRoot("html")
                .addTag("wicket:border")
                .gotoRoot().addTag("head")
                .addNamespace("other", "http://other-ns.com")
                .gotoRoot().addTag("other:foo")
                .validate(new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"),
                        new URL("http://wicket.sourceforge.net/wicket-1.0.xsd"));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
    }

    @Test
    public void validate_URL_invalid() throws Exception {
        ValidationResult res = XMLDoc.newDocument(false)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0")
                .addRoot("htmlxxx")
                .addTag("wicket:aaaa")
                .gotoRoot().addTag("head")
                .addNamespace("other", "http://other-ns.com")
                .gotoRoot().addTag("other:foo")
                .validate(new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"),
                        new URL("http://wicket.sourceforge.net/wicket-1.0.xsd"));
        assertTrue(res.hasError());
        assertFalse(res.hasWarning());
        System.out.println(Arrays.deepToString(res.getErrorMessages()));
    }

    @Test
    public void test_clone() throws Exception {
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

        XMLTag cloned = tag.duplicate();
        System.out.println(cloned);
        assertEquals(cloned.toString(), tag.toString());
        assertEquals(cloned.getCurrentTagName(), "title");
    }

}
