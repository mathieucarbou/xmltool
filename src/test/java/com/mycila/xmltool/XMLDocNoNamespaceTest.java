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

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.mycila.xmltool.Assert.Code;
import static com.mycila.xmltool.Assert.assertThrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocNoNamespaceTest extends AbstractTest {

    @Test
    public void test_entity_resolver() throws Exception {
        XMLDoc.from(getClass().getResource("/test.xhtml"), true);
        XMLDoc.from(getClass().getResource("/test.xhtml"), true).gotoTag("body/div[1]");
        System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true).toString());
    }

    @Test
    public void test_wiki_manual_2() throws Exception {
        //URL yahooGeoCode = new URL("http://local.yahooapis.com/MapsService/V1/geocode?appid=YD-9G7bey8_JXxQP6rxl.fBFGgCdNjoDMACQA--&state=QC&country=CA&zip=H1W3B8");
        //System.out.println(XMLDoc.from(yahooGeoCode, true).toString());
        //System.out.println(XMLDoc.from(yahooGeoCode, true).getText("Result/City"));
        System.out.println(XMLDoc.newDocument(true).addRoot("html").getCurrentTagName());
        System.out.println(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("head")
                .toString());
        System.out.println(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("head")
                .delete()
                .toString());
        System.out.println(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("head").addTag("title")
                .toString());
        System.out.println(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("head").addTag("title")
                .gotoRoot().deleteChilds()
                .toString());
        System.out.println(XMLDoc.newDocument(true)
                .addRoot("html")
                .renameTo("xhtml")
                .toString());
        String[] names = XMLDoc.from(getClass().getResource("/test.xhtml"), true)
                .gotoTag("body/div[1]")
                .getAttributeNames();
        System.out.println(Arrays.toString(names));
        System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true)
                .gotoTag("body/div[1]")
                .getAttribute("class"));
        System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true)
                .getAttribute("class", "body/div[2]"));
        System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true)
                .gotoTag("body/div[1]")
                .deleteAttributes()
                .toString());
        System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true)
                .hasAttribute("id", "body/div[1]"));
        System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true)
                .gotoTag("body/div[1]").deleteAttribute("id")
                .hasAttribute("id"));
        System.out.println(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("head").addText("<\"!@#$%'^&*()>")
                .addTag("body").addCDATA("<\"!@#$%'^&*()>")
                .toString());
        XMLDoc.from(getClass().getResource("/test.xhtml"), true).forEachChild(new CallBack() {
            public void execute(XMLTag doc) {
                System.out.println(doc.getCurrentTagName());
            }
        });
        XMLDoc.from(getClass().getResource("/test.xhtml"), true).forEach(new CallBack() {
            public void execute(XMLTag doc) {
                System.out.println(doc.getAttribute("id"));
            }
        }, "//div");
    }

    @Test
    public void test_indentation() {
        System.out.println(XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/") // http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd
                .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0") // http://wicket.sourceforge.net/wicket-1.0.xsd
                .addRoot("html")
                .addTag("wicket:border")
                .gotoRoot().addTag("head")
                .addNamespace("other", "http://other-ns.com")
                .gotoRoot().addTag("other:foo")
                .toString());
    }

    @Test
    public void test_wiki_manual() throws Exception {
        System.out.println(XMLDoc.newDocument(true).addRoot("html").toString());

        System.out.println(XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addRoot("html"));

        System.out.println(XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/") // http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd
                .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0") // http://wicket.sourceforge.net/wicket-1.0.xsd
                .addRoot("html")
                .addTag("wicket:border")
                .gotoRoot().addTag("head")
                .addNamespace("other", "http://other-ns.com")
                .gotoRoot().addTag("other:foo")
                .toString());

        System.out.println(XMLDoc.newDocument(true)
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
                .gotoTag("body").addTag("child")
                .gotoParent().addCDATA("with special characters")
                .gotoTag("body").addCDATA("<\"!@#$%'^&*()>")
                .toString());

        System.out.println(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("body")
                .addTag("span").addText("a span")
                .addTag("div").addText("a div")
                .toString());

        XMLTag doc = XMLDoc.from(getClass().getResource("/goto.xml"), true);
        doc.gotoChild("head")      // jump to the only 'head' tag under 'html'v
                .gotoChild()       // jump to the only child of 'head'
                .gotoRoot()        // go to 'html'
                .gotoChild(2)      // go to child 'body'
                .gotoChild(3)      // go to third child 'w:border' having text 'child3'
                .gotoRoot()        // return to root
                .gotoTag("body/border[1]/div"); // xpath navigation

        doc = XMLDoc.from(getClass().getResource("/get.xml"), true);
        System.out.println(doc.toString());
        assertEquals(doc.getCurrentTag().getNodeType(), Document.ELEMENT_NODE);
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.getPefix("http://www.w3.org/2002/06/xhtml2/"), ""); // ns0 is already used in the document
        assertEquals(doc.gotoTag("head/title").getText(), "my special title: <\"!@#$%'^&*()>");
        assertEquals(doc.getText("."), "my special title: <\"!@#$%'^&*()>");
        assertEquals(doc.getCDATA("../../body"), "my special data: <\"!@#$%'^&*()>");
        assertEquals(doc.getAttribute("ns0:id"), "titleID");

        //when we ignore namespace, validation becomes unpredictable on diffrent jdk versions
        /*ValidationResult results = XMLDoc.from(getClass().getResource("/goto.xml"), true).validate(
                new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"),
                new URL("http://wicket.sourceforge.net/wicket-1.0.xsd")
        );
        assertFalse(results.hasError());

        results = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addRoot("htmlZZ")
                .validate(new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"));
        assertTrue(results.hasError());
        System.out.println(Arrays.deepToString(results.getErrorMessages()));*/

        XMLDoc.newDocument(true).addRoot("html")
                .toResult(new DOMResult())
                .toStream(new StringWriter())
                .toStream(new ByteArrayOutputStream());
    }

    @Test
    public void test_getCDATA_with_getText() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("test").addCDATA("<\"!@#$%'^&*()>");
        System.out.println(doc.toString());
        System.out.println("Text: " + doc.gotoRoot().getText());
        System.out.println("CDATA: " + doc.gotoRoot().getCDATA());
        doc = XMLDoc.newDocument(true).addRoot("test").addText("    ");
        System.out.println("Text: " + doc.gotoRoot().getText());
    }

    @Test
    public void test_new_doc() {
        assertSameDoc(XMLDoc.newDocument(true).addRoot("html").toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_from_Document() {
        Document d = XMLDoc.newDocument(true).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDoc.from(d, true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputSource() {
        assertSameDoc(XMLDoc.from(new InputSource(new StringReader("<html></html>")), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Reader() {
        assertSameDoc(XMLDoc.from(new StringReader("<html></html>"), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_InputStream() {
        assertSameDoc(XMLDoc.from(new ByteArrayInputStream("<html></html>".getBytes()), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_File() {
        assertSameDoc(XMLDoc.from(new File("src/test/resources/doc.xhtml"), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Url() {
        assertSameDoc(XMLDoc.from(XMLDocNoNamespaceTest.class.getResource("/doc.xhtml"), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_String() {
        assertSameDoc(XMLDoc.from("<html></html>", true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_from_Source() {
        Document d = XMLDoc.newDocument(true).addRoot("html").addTag("head").gotoParent().addTag("body").toDocument();
        assertSameDoc(XMLDoc.from(new DOMSource(d), true).toDocument().getFirstChild().getNodeName(), "html");
    }

    @Test
    public void test_toString() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html").addTag("head").gotoParent().addTag("body").addText("�a �t� ���");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><head/><body>�a �t� ���</body></html>");
        assertTrue(doc.toString("ASCII").startsWith("<?xml version=\"1.0\" encoding=\"ASCII\" standalone=\"no\"?>"));
    }

    @Test
    public void test_toDocument() {
        Document doc = XMLDoc.newDocument(true).addRoot("html").addTag("head").gotoParent().addTag("body").addText("�a �t� ���").toDocument();
        assertSameDoc(doc.getFirstChild().getFirstChild().getNodeName(), "head");
    }

    @Test
    public void test_addNode() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html")
                .addTag("head")
                .gotoRoot()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><head/><body/></html>");
    }

    @Test
    public void test_addAttribute() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html")
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
                XMLDoc.newDocument(true).addRoot("html")
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
        XMLTag doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://myns")
                .addRoot("html")
                .addAttribute("lang", "en");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html lang=\"en\"/>");

        doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://myns")
                .addRoot("ns0:html")
                .addAttribute("lang", "en");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html lang=\"en\"/>");

        doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://myns")
                .addRoot("html");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");

        doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://myns")
                .addRoot("ns0:html");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");

        doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://myns")
                .addRoot("ns1:html");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");
    }

    @Test
    public void test_addNamespace_already_bound_xml() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html").addNamespace("xml", "http://ns0");
        assertEquals(doc.getPefix("http://ns"), "");
        assertEquals(doc.getPefix(XMLConstants.XML_NS_URI), XMLConstants.XML_NS_PREFIX);
    }

    @Test
    public void test_addNamespace_already_bound_xmlns() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html").addNamespace("xmlns", "http://ns0");
        assertEquals(doc.getPefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI), XMLConstants.XMLNS_ATTRIBUTE);
    }

    @Test
    public void test_addNamespace_already_bound() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://def")
                .addRoot("html")
                .addNamespace("ns0", "http://ns0");
        assertEquals(doc.getPefix("xml"), "");
    }

    @Test
    public void test_addNamespace_default() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addNamespace("ns", "http://ns");
        assertEquals(doc.getPefix("http://ns"), "");
    }

    @Test
    public void test_addNamespace() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("ns1:body")
                .addNamespace("ns1", "http://ns");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><body/></html>");

        doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addNamespace("ns1", "http://ns")
                .addTag("ns1:body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><body/></html>");

        doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://myns")
                .addRoot("ns1:html")
                .addTag("head").gotoParent()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><head/><body/></html>");

        doc = XMLDoc.newDocument(true)
                .addNamespace("ns1", "http://myns")
                .addDefaultNamespace("http://def")
                .addRoot("ns1:html")
                .addTag("head").gotoParent()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><head/><body/></html>");

        doc = XMLDoc.newDocument(true)
                .addNamespace("ns1", "http://myns")
                .addDefaultNamespace("http://def")
                .addRoot("html")
                .addTag("ns1:head").gotoParent()
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><head/><body/></html>");

        doc = XMLDoc.newDocument(true)
                .addNamespace("ns1", "http://ns1")
                .addDefaultNamespace("http://def")
                .addRoot("html")
                .addAttribute("ns1:attr", "val")
                .addTag("ns1:head")
                .addAttribute("attr", "val")
                .addTag("body")
                .addAttribute("attr", "val");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html attr=\"val\"><head attr=\"val\"><body attr=\"val\"/></head></html>");

    }

    @Test
    public void test_same_namespace() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/namespace.xml"), true);
        System.out.println(doc.toString());
    }

    @Test
    public void test_xpath_no_ns() throws Exception {
        XMLTag doc = XMLDoc.from(getClass().getResource("/ws.xml"), false);
        System.out.println(doc.toString());
        assertEquals(doc.getText("s:body/p:data/state"), "true");
        doc = XMLDoc.from(getClass().getResource("/ws.xml"), true);
        System.out.println(doc.toString());
        assertEquals(doc.getText("body/data/state"), "true");
    }

    @Test
    public void test_withText() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addText("1<2");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>1&lt;2</html>");

        doc = XMLDoc.newDocument(true)
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
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addCDATA("1<2");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><![CDATA[1<2]]></html>");

        doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addCDATA("1<2")
                .addTag("body")
                .gotoParent().addCDATA("bla").addCDATA("1<2");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html><![CDATA[1<2]]><body/><![CDATA[bla]]><![CDATA[1<2]]></html>");
    }

    @Test
    public void test_deleteChilds() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html").deleteChilds();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");

        doc = XMLDoc.newDocument(true).addRoot("html").addAttribute("a", "b").deleteChilds();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\"/>");

        doc = XMLDoc.newDocument(true).addRoot("html")
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
                XMLDoc.newDocument(true).addRoot("html").delete();
            }
        });
    }

    @Test
    public void test_delete() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
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
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .deleteAttributes();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html/>");

        doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addAttribute("a", "b")
                .addAttribute("b", "c")
                .addAttribute("d", "e")
                .addText("blabla")
                .addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" b=\"c\" d=\"e\">blabla<body/></html>");

        doc.gotoRoot().deleteAttributes();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>blabla<body/></html>");

        doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://def")
                .addNamespace("ns", "http://ns")
                .addRoot("html")
                .addAttribute("ns:a", "b")
                .addAttribute("b", "c")
                .addAttribute("ns:d", "e")
                .addText("blabla")
                .addTag("ns:body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" b=\"c\" d=\"e\">blabla<body/></html>");

        doc.gotoRoot().deleteAttributes();
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>blabla<body/></html>");
    }

    @Test
    public void test_deleteAttribute_inexisting() throws Exception {
        assertThrow(XMLDocumentException.class).withMessage("Cannot delete attribute 'd' from element 'html': attribute does noe exist").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(true).addRoot("html").deleteAttribute("d");
            }
        });
    }

    @Test
    public void test_deleteAttribute() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html")
                .addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("e", "f");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" c=\"d\" e=\"f\"/>");

        doc.deleteAttribute("c");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" e=\"f\"/>");

        doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://def")
                .addNamespace("ns", "http://ns")
                .addRoot("html")
                .addAttribute("ns:a", "b")
                .addAttribute("b", "c")
                .addAttribute("ns:d", "e")
                .addText("blabla")
                .addCDATA("data");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html a=\"b\" b=\"c\" d=\"e\">blabla<![CDATA[data]]></html>");

        doc.deleteAttribute("a");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html b=\"c\" d=\"e\">blabla<![CDATA[data]]></html>");

        doc.deleteAttribute("b");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html d=\"e\">blabla<![CDATA[data]]></html>");

        doc.deleteAttribute("d");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html>blabla<![CDATA[data]]></html>");
    }

    @Test
    public void test_renameTo() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/1999/xhtml")
                .addRoot("html")
                .addAttribute("attr", "val")
                .addTag("child");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html attr=\"val\"><child/></html>");

        doc.gotoRoot().renameTo("xhtml");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><xhtml attr=\"val\"><child/></xhtml>");
    }

    @Test
    public void test_renameTo_namepace() throws Exception {
        XMLTag doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/1999/xhtml")
                .addRoot("html")
                .addAttribute("attr", "val")
                .addTag("child");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><html attr=\"val\"><child/></html>");

        doc.gotoRoot().renameTo("ns1:xhtml");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns1:xhtml attr=\"val\"><child/></ns1:xhtml>");

        doc.addNamespace("ns1", "http://ns1.com").renameTo("ns1:thtml").addTag("body");
        assertSameDoc(doc.toString(), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><ns1:thtml attr=\"val\"><child/><body/></ns1:thtml>");
    }

    @Test
    public void test_gotoParent() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html");
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.gotoParent().getCurrentTagName(), "html");

        doc.addNamespace("ns", "http://ns").addTag("ns:body");
        assertEquals(doc.getCurrentTagName(), "body");
        assertEquals(doc.gotoParent().getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoRoot() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html");
        assertEquals(doc.getCurrentTagName(), "html");
        assertEquals(doc.gotoRoot().getCurrentTagName(), "html");

        doc.addNamespace("ns", "http://ns").addTag("ns:body");
        assertEquals(doc.getCurrentTagName(), "body");
        assertEquals(doc.gotoRoot().getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoNode_inexisting() {
        assertThrow(XMLDocumentException.class).withMessage("Error executing xpath 'a' from node 'html': Inexisting target node.").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(true).addRoot("html").gotoTag("a");
            }
        });
    }

    @Test
    public void test_gotoNode_bad_xpath() {
        assertThrow(XMLDocumentException.class).withMessage("Error compiling xpath ':-)': A location path was expected, but the following token was encountered:  :-").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(true).addRoot("html").gotoTag(":-)");
            }
        });
    }

    @Test
    public void test_gotoNode_not_target_element() {
        assertThrow(XMLDocumentException.class).withMessage("XPath expression '..' does not target an element. Targeted node is '#document' (node type is '9')").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(true).addRoot("html").gotoTag("..");
            }
        });
    }

    @Test
    public void test_gotoNode_not_target_element2() {
        assertThrow(XMLDocumentException.class).withMessage("XPath expression 'text()' does not target an element. Targeted node is '#text' (node type is '3')").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(true).addRoot("html").addText("text").gotoTag("text()");
            }
        });
    }

    @Test
    public void test_gotoNode_this() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html");
        assertEquals(doc.gotoTag(".").getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoNode_parent() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html").addTag("head");
        assertEquals(doc.gotoTag("..").getCurrentTagName(), "html");
    }

    @Test
    public void test_gotoNode_child() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html")
                .addTag("body").addText("txt")
                .addTag("head").addText("txt1")
                .addTag("head").addText("txt2");
        assertEquals(doc.gotoRoot().gotoTag("body").getText(), "txt");
        assertEquals(doc.gotoRoot().gotoTag("head[1]").getText(), "txt1");
        assertEquals(doc.gotoRoot().gotoTag("head[2]").getText(), "txt2");
    }

    @Test
    public void test_gotoNode_namespace() {
        XMLTag doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addNamespace("w", "http://wicket.sourceforge.net/wicket-1.0")
                .addRoot("html")
                .addTag("w:border")
                .gotoParent()
                .addTag("head")
                .addTag("title")
                .addAttribute("w:id", "title");
        assertEquals(doc.gotoRoot().gotoTag("head").getCurrentTagName(), "head");
        assertEquals(doc.gotoRoot().gotoTag("border").getCurrentTagName(), "border");
    }

    @Test
    public void test_gotoChild_no_child() {
        assertThrow(XMLDocumentException.class).withMessage("Current element 'html' has no child").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(true).addRoot("html").addText("txt");
                doc.gotoChild();
            }
        });
    }

    @Test
    public void test_gotoChild_too_many() {
        assertThrow(XMLDocumentException.class).withMessage("Cannot select child: current element 'html' has '2' children").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLTag doc = XMLDoc.newDocument(true)
                        .addRoot("html").addText("aa")
                        .addTag("child").addText("txt")
                        .gotoRoot().addTag("child");
                doc.gotoRoot().gotoChild();
            }
        });
    }

    @Test
    public void test_gotoChild() {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html").addText("aa")
                .addTag("child").addText("txt");
        assertEquals(doc.gotoRoot().gotoChild().getText(), "txt");
    }

    @Test
    public void test_gotoChild_with_ns() {
        XMLTag doc = XMLDoc.newDocument(true)
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
                XMLTag doc = XMLDoc.newDocument(true)
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
                XMLTag doc = XMLDoc.newDocument(true)
                        .addRoot("html").addTag("child")
                        .gotoRoot().addTag("child");
                doc.gotoRoot().gotoChild(3);
            }
        });
    }

    @Test
    public void test_gotoChild_i() {
        XMLTag doc = XMLDoc.newDocument(true)
                .addRoot("html").addTag("child").addText("txt1")
                .gotoRoot().addTag("child").addText("txt2");
        assertEquals(doc.gotoRoot().gotoChild(1).getText(), "txt1");
    }

    @Test
    public void test_gotoChild_name() {
        XMLTag doc = XMLDoc.newDocument(true)
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
                XMLTag doc = XMLDoc.newDocument(true)
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
                XMLTag doc = XMLDoc.newDocument(true)
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
                XMLTag doc = XMLDoc.newDocument(true).addRoot("html");
                doc.gotoRoot().gotoChild("aa");
            }
        });
    }

    @Test
    public void test_getCurrentTag_Name() {
        assertSameDoc(XMLDoc.newDocument(true).addRoot("html").getCurrentTag().getNodeName(), "html");
        assertSameDoc(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("title")
                .getCurrentTagName(), "title");
        assertSameDoc(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("title")
                .gotoParent().gotoParent().gotoParent()
                .getCurrentTag().getNodeName(), "html");
        assertSameDoc(XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("title")
                .gotoParent().gotoParent().gotoParent()
                .addTag("body")
                .gotoRoot()
                .getCurrentTagName(), "html");
    }

    @Test
    public void test_context_and_getprefix() {
        XMLTag doc = XMLDoc.from(getClass().getResource("/xpath.xml"), true);
        assertEquals(doc.getPefix("http://ns3.com"), "");
        assertEquals(doc.getPefix("http://ns4.com"), "");
        assertEquals(doc.getPefix("http://inexisting"), "");
        assertEquals(doc.getContext().getNamespaceURI("ns0"), "");
        assertEquals(doc.getContext().getNamespaceURI("ns1"), "");
        assertEquals(doc.getContext().getNamespaceURI("ns2"), "");
        assertEquals(doc.getContext().getNamespaceURI("ns3"), "");
    }

    @Test
    public void test_context_and_getprefixes() {
        XMLTag doc = XMLDoc.from(getClass().getResource("/namespace.xml"), true);
        assertEquals(doc.getPefixes("").length, 1);
        assertEquals(doc.getPefixes("inexisting").length, 0);
        assertEquals(doc.getPefixes("http://www.w3.org/2002/06/xhtml2/").length, 0);
    }

    @Test
    public void test_getText() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html");
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
        XMLTag doc = XMLDoc.newDocument(true)
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
    public void test_getCDATA() {
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html");
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
        XMLTag doc = XMLDoc.newDocument(true)
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
        XMLTag doc = XMLDoc.newDocument(true).addRoot("html").addNamespace("ns0", "http://ns0");
        assertEquals(doc.getAttributeNames().length, 0);

        doc.addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("ns0:e", "f");
        assertEquals(doc.getAttributeNames().length, 3);
        assertEquals(doc.getAttributeNames()[0], "a");
    }

    int count = 0;

    @Test
    public void test_forEach() {

        XMLDoc.from(getClass().getResource("/get.xml"), true).forEach(new CallBack() {
            public void execute(XMLTag doc) {
                System.out.println(doc.getCurrentTagName() + "'s text: " + doc.getText());
                System.out.println(doc.getCurrentTagName() + "'s data: " + doc.getCDATA());
                count++;
            }
        }, "//*");
        assertEquals(count, 8);
        count = 0;
        XMLDoc.from(getClass().getResource("/get.xml"), true).forEach(new CallBack() {
            public void execute(XMLTag doc) {
                System.out.println(doc.getCurrentTagName() + "'s text: " + doc.getText());
                System.out.println(doc.getCurrentTagName() + "'s data: " + doc.getCDATA());
                count++;
            }
        }, "@*");
        assertEquals(count, 0);
    }

    @Test
    public void test_getAttribute_error() {
        assertThrow(XMLDocumentException.class).withMessage("Element 'html' does not have attribute 'inexisting'").whenRunning(new Code() {
            public void run() throws Throwable {
                XMLDoc.newDocument(true).addRoot("html").getAttribute("inexisting");
            }
        });
    }

    @Test
    public void test_getAttribute() {
        XMLTag doc = XMLDoc.newDocument(true).addDefaultNamespace("http://def").addRoot("html").addNamespace("ns1", "http://ns1");
        assertEquals(doc.getAttributeNames().length, 0);
        doc.addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("ns1:e", "f");
        assertEquals(doc.getAttributeNames().length, 3);
        assertEquals(doc.getAttributeNames()[0], "a");
        assertEquals(doc.getAttributeNames()[1], "c");
        assertEquals(doc.getAttributeNames()[2], "e");
        assertEquals(doc.toString().replace("\r", ""), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<html a=\"b\" c=\"d\" e=\"f\"/>\n");
    }

    @Test
    public void test_getAttribute_xpath() {
        XMLTag doc = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://def")
                .addRoot("html")
                .addTag("foo")
                .addNamespace("ns1", "http://ns1")
                .addAttribute("a", "b")
                .addAttribute("c", "d")
                .addAttribute("ns1:e", "f")
                .gotoRoot();
        assertEquals(doc.getAttribute("a", "foo"), "b");
        assertEquals(doc.getAttribute("e", "foo"), "f");
    }

    @Test
    public void validate_URL() throws Exception {
        ValidationResult res = XMLDoc.newDocument(true)
                .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
                .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0")
                .addRoot("html")
                .addTag("wicket:border")
                .gotoRoot().addTag("head")
                .addNamespace("other", "http://other-ns.com")
                .gotoRoot().addTag("other:foo")
                .validate(new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"),
                        new URL("http://wicket.sourceforge.net/wicket-1.0.xsd"));
        System.out.println(Arrays.deepToString(res.getErrorMessages()));
        assertTrue(res.hasError());
        assertFalse(res.hasWarning());
    }

    @Test
    public void validate_URL_invalid() throws Exception {
        ValidationResult res = XMLDoc.newDocument(true)
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
    public void addTag_XMLTag() throws Exception {
        XMLTag tag1 = XMLDoc.from(getClass().getResourceAsStream("/namespace.xml"), true);
        XMLTag tag2 = XMLDoc.from(getClass().getResourceAsStream("/test.xhtml"), true);
        assertEquals(tag1.gotoRoot().hasTag("head/body/div[1]"), false);
        tag1.gotoChild("ns1:head").addTag(tag2.gotoChild("body"));
        System.out.println(tag1.toString());
        System.out.println(tag2.toString());
        assertEquals(tag1.gotoRoot().hasTag("head/body/div[1]"), true);
    }

    @Test
    public void addDocumentXMLTag() throws Exception {
        XMLTag tag1 = XMLDoc.from(getClass().getResourceAsStream("/namespace.xml"), true);
        XMLTag tag2 = XMLDoc.from(getClass().getResourceAsStream("/test.xhtml"), true);
        assertEquals(tag1.gotoRoot().hasTag("head/body/div[1]"), false);
        tag1.gotoTag("head").addDocument(tag2);
        System.out.println(tag1.toString());
        System.out.println(tag2.toString());
        assertEquals(tag1.gotoRoot().hasTag("head/html/head/title"), true);
    }

    @Test
    public void test_clone() throws Exception {
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

        XMLTag cloned = tag.duplicate();
        System.out.println(cloned);
        assertEquals(cloned.toString(), tag.toString());
        assertEquals(cloned.getCurrentTagName(), "title");
    }

    @Test
    public void test_use_case_inner_xml() throws Exception {
        XMLTag tag = XMLDoc.from(getClass().getResource("/inner-flat.xml"), true);
        tag.forEach(new CallBack() {
            public void execute(XMLTag tag) {
                // version 2.5
                System.out.println("2.5: addTag\n" + XMLDoc.newDocument(true).addRoot("content").addTag(tag).toString());
                // version 2.6
                System.out.println("2.6: fromCurrentTag:\n" + XMLDoc.fromCurrentTag(tag, true).toString());
                System.out.println("2.6: getInnerDocument:\n'" + tag.getInnerDocument().toString() + "'");
                System.out.println("2.6: getInnerText:\n'" + tag.getInnerText() + "'");
            }
        }, "b");
    }

    @Test
    public void getCurrentTagLocation() throws Exception {
        XMLTag tag = XMLDoc.from("<root/>", true);
        tag.gotoTag(".");
        assertEquals(tag.getCurrentTagLocation(), ".");
        tag.addTag("hello").addText("world");
        assertEquals(tag.gotoChild().getCurrentTagLocation(), "*[1]");
        assertEquals(tag.gotoRoot().gotoTag("*[1]").getCurrentTagName(), "hello");
        tag.addTag("hellochild1")
                .gotoParent().addTag("hellochild2")
                .addTag("yo");
        assertEquals(tag.getCurrentTagLocation(), "*[1]/*[2]/*[1]");
    }

    @Test
    public void test_use_case_inner_xml2() throws Exception {
        /* XMLDoc.from(getClass().getResource("/inner2.xml"), true).forEach(new CallBack() {
           public void execute(XMLTag tag) {
               System.out.println("==> " + XMLDoc.fromCurrentTag(tag, true).gotoTag("b2/dob").deleteAttribute("xsi:nil").toString());
           }
       }, "b");*/
        XMLDoc.from(getClass().getResource("/inner2.xml"), false).forEach(new CallBack() {
            public void execute(XMLTag tag) {
                System.out.println("==> " + XMLDoc.fromCurrentTag(tag, false).toString());
            }
        }, "b");
        XMLDoc.from(getClass().getResource("/inner2.xml"), false).forEach(new CallBack() {
            public void execute(XMLTag tag) {
                System.out.println("==> " + XMLDoc.fromCurrentTag(tag, true).toString());
            }
        }, "b");

        assertEquals(XMLDoc.from("<root/>", true).duplicate().toString().replace("\r", ""), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<root/>\n");
    }

    @Test
    public void test_iterator() throws Exception {
        class MyCallBack implements CallBack, Iterable<XMLTag> {
            List<XMLTag> inners = new ArrayList<XMLTag>();

            public void execute(XMLTag doc) {
                inners.add(XMLDoc.fromCurrentTag(doc, true));
            }

            public Iterator<XMLTag> iterator() {
                return inners.iterator();
            }
        }
        MyCallBack myCallBack = new MyCallBack();
        XMLDoc.from(getClass().getResource("/inner2.xml"), false).forEach(myCallBack, "b");
        for (XMLTag xmlTag : myCallBack) {
            System.out.println(xmlTag);
        }
    }

    @Test
    public void test_getChilds() throws Exception {
        XMLTag tag = XMLDoc.from(resource("/child.xml"), true);
        assertEquals(tag.gotoRoot().getCurrentTagName(), "a");
        for (XMLTag xmlTag : tag.getChilds()) {
            System.out.println("current: " + xmlTag.getCurrentTagName());
            System.out.println("first child: " + xmlTag.gotoFirstChild().getCurrentTagName());
            System.out.println("location: " + xmlTag.getCurrentTagLocation());
        }
        assertEquals(tag.getCurrentTagName(), "a");
        for (XMLTag xmlTag : tag.getChilds()) {
            System.out.println("current: " + xmlTag.getCurrentTagName());
            System.out.println("first child: " + xmlTag.gotoFirstChild().getCurrentTagName());
            System.out.println("location: " + xmlTag.getCurrentTagLocation());
            break;
        }
        assertEquals(tag.getCurrentTagName(), "c");
    }

    @Test
    public void test_getChilds2() throws Exception {
        XMLTag tag = XMLDoc.newDocument(true)
                .addRoot("root").addTag("a")
                .gotoParent().addTag("b")
                .gotoParent().addTag("c")
                .gotoRoot();
        assertEquals(tag.getCurrentTagName(), "root");
        for (XMLTag xmlTag : tag.getChilds()) {
            if (xmlTag.getCurrentTagName().equals("b")) {
                break;
            }
        }
        assertEquals(tag.getCurrentTagName(), "b");
    }

    @Test
    public void test_getChilds3() throws Exception {
        XMLTag tag = XMLDoc.newDocument(true)
                .addRoot("root").addTag("a")
                .gotoParent().addTag("b")
                .gotoParent().addTag("c")
                .gotoRoot();
        assertEquals(tag.getCurrentTagName(), "root");
        for (XMLTag xmlTag : tag.getChilds()) {
            System.out.println(xmlTag.getCurrentTagName());
        }
        assertEquals(tag.getCurrentTagName(), "root");
    }

    @Test
    @Ignore
    public void test_yahoo() throws Exception {
        URL yahooGeoCode = new URL("http://local.yahooapis.com/MapsService/V1/geocode?appid=YD-9G7bey8_JXxQP6rxl.fBFGgCdNjoDMACQA--&state=QC&country=CA&zip=H1W3B8");
        System.out.println(XMLDoc.from(yahooGeoCode, true).toString());
        assertEquals(XMLDoc.from(yahooGeoCode, true).getText("Result/City"), "Montreal");
    }

}
