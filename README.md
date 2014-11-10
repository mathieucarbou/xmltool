**Table of Contents**

- [Mycila XML Tool](#mycila-xml-tool)
	- [Maven Repository](#maven-repository)
	- [Documentation](#documentation)
		- [Creating XML documents](#creating-xml-documents)
			- [Creating a new XML document](#creating-a-new-xml-document)
			- [Loading an existing XML document](#loading-an-existing-xml-document)
			- [Ignoring namespaces](#ignoring-namespaces)
		- [Using namespaces](#using-namespaces)
			- [Adding and retrieving namespaces and prefixes](#adding-and-retrieving-namespaces-and-prefixes)
			- [Prefix constraints](#prefix-constraints)
		- [XML elements operations](#xml-elements-operations)
			- [On elements](#on-elements)
			- [On attributes](#on-attributes)
			- [On text and data](#on-text-and-data)
		- [Navigation, XPath and Callback support](#navigation-xpath-and-callback-support)
			- [Raw XPath](#raw-xpath)
			- [Gotos](#gotos)
			- [Callbacks on selected nodes](#callbacks-on-selected-nodes)
		- [Converting your XML document](#converting-your-xml-document)
		- [Validating your XML document](#validating-your-xml-document)
		- [Exception handling](#exception-handling)

# Mycila XML Tool #

XMLTool is a very simple Java library to be able to do all sorts of common operations with an XML document. As a Java developer, I often end up writing the always the same code for processing XML, transforming, ... So i decided to put all in a very easy to use class using the Fluent Interface pattern to facilitate XML manipulations.

    XMLTag tag = XMLDoc.newDocument(false)
        .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
        .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0")
        .addRoot("html")
        .addTag("wicket:border")
        .gotoRoot().addTag("head")
        .addNamespace("other", "http://other-ns.com")
        .gotoRoot().addTag("other:foo");
    System.out.println(tag.toString());

__Features__

With XML Tool you will be able to quickly:

 * Create new XML documents from external sources or new document from scrash
 * Manage namespaces
 * Manipulating nodes (add, remove, rename)
 * Manipulating data (add, remove text or CDATA)
 * Navigate into the document with shortcuts and XPath (note: XPath supports namespaces)
 * Tranform an XMlDoc instance to a String or a Document
 * Validate your document against schemas
 * Executin callbacks on a hierarchy
 * Remove all namspaces (namespace ignoring)
 * ... and a lot of other features !

__Project status__

 - __Issues:__ https://github.com/mycila/xmltool/issues
 - __OSGI Compliant:__ <img width="100px" src="http://www.sonatype.com/system/images/W1siZiIsIjIwMTMvMDQvMTIvMTEvNDAvMzcvMTgzL05leHVzX0ZlYXR1cmVfTWF0cml4X29zZ2lfbG9nby5wbmciXV0/Nexus-Feature-Matrix-osgi-logo.png" title="OSGI Compliant"></img>
 - __Build Status:__ [![Build Status](https://travis-ci.org/mycila/xmltool.png?branch=master)](https://travis-ci.org/mycila/xmltool)

## Maven Repository ##

 __Releases__

Available in Maven Central Repository: http://repo1.maven.org/maven2/com/mycila/mycila-xmltool/

__Snapshots__
 
Available in OSS Repository:  https://oss.sonatype.org/content/repositories/snapshots/com/mycila/mycila-xmltool/

__Maven dependency__

    <dependency>
        <groupId>com.mycila</groupId>
        <artifactId>mycila-xmltool</artifactId>
        <version>X.Y.ga</version>
    </dependency>

__Maven sites__

 - [4.0.ga] (http://mycila.github.io/xmltool/reports/4.0.ga/index.html)

## Documentation ##

### Performance consideration ###

XML Tool uses the Java DOM API and `Document` creation has a cost. Thus, to improve peformance, XML Tool uses 2 Object pools of `DocumentBuilder` instances: 

* one pool for namespace-aware document builders
* another one ignoring namespaces

You can configure the pools by using `XMLDocumentBuilderFactory.setPoolConfig(config)`

By default, each of the 2 pools have the following configuration:

* min idle = 0
* max idle = CPU core number
* max total = CPU core number * 4
* max wait time = -1

If your application is heavily threaded and a lot of threads are using XMLTag concurrently, to avoid thread contention you might want to increase the max total to match your peak thread count and max idle to match your average thread count.

If your application does not use a lot of thread and often create documents, you could probably lower those numbers.

The goal is to have sufficient `DocumentBuilder` instances available in the pool to be able to "feed" your application as demand without waiting for these objects to become available.

Using an object pool is sure much more complicated, but it will prevent any threading issues and also maximize performance because of object reuse.

### Creating XML documents ###

#### Creating a new XML document ####

The `newDocument` method crate a new XML document. You then have to choose a default namespace if you want and then choose the root name of the document.

    System.out.println(XMLDoc.newDocument(true).addRoot("html").toString());

gives:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html/>

#### Loading an existing XML document ####

The `from` methods can load an XML document from any of the following types:

 * org.w3c.dom.Node
 * InputSource
 * Reader
 * InputStream
 * File
 * URL
 * String
 * javax.xml.transform.Source

__Example:__

    URL yahooGeoCode = new URL("http://local.yahooapis.com/MapsService/V1/geocode?appid=YD-9G7bey8_JXxQP6rxl.fBFGgCdNjoDMACQA--&state=QC&country=CA&zip=H1W3B8");
    System.out.println(XMLDoc.from(yahooGeoCode, true).toString());
    System.out.println(XMLDoc.from(yahooGeoCode, true).getText("Result/City"));

outputs:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <ResultSet xmlns="urn:yahoo:maps" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:yahoo:maps http://api.local.yahoo.com/MapsService/V1/GeocodeResponse.xsd">
    <Result precision="zip">
        <Latitude>45.543289</Latitude>
        <Longitude>-73.543098</Longitude>
        <Address/>
        <City>Montreal</City>
        <State>QC</State>
        <Zip>H1W 3B8</Zip>
        <Country>CA</Country>
    </Result>
    </ResultSet>
    <!-- ws04.search.re2.yahoo.com uncompressed Tue Dec  9 13:39:12 PST 2008 -->
    
    Montreal

#### Ignoring namespaces ####

All creational methods `XMLDoc.newDocument` and `XMLDoc.from` requires a boolean attribute `ignoreNamespaces`. If this attribute is set to true, all namespaces in the document are ignored. This is really useful if you use XPath a lot since you can avoid prefixing all your XPath elements.

__Example:__

    System.out.println(XMLDoc.newDocument(true)
        .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
        .addRoot("html"));
    System.out.println(XMLDoc.newDocument(false)
        .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
        .addRoot("html"));

outputs:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html/>

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html xmlns="http://www.w3.org/2002/06/xhtml2/"/>

Navigating in a document with namespaces using XPath is quite a pain:

    doc.gotoTag("ns0:body").addTag("child")
       .gotoParent().addCDATA("with special characters")
       .gotoTag("ns0:body").addCDATA("<\"!@#$%'^&*()>")

whereas if you load the same document with `ignoreNamespaces`, you can simply navigate like this when you use XPath:

    doc.gotoTag("body").addTag("child")
       .gotoParent().addCDATA("with special characters")
       .gotoTag("body").addCDATA("<\"!@#$%'^&*()>")

### Using namespaces ###

When you create or load a document, and if you decide to not ignore namespaces, you can add a default namespace for your document and add other ones after. Namespace management is quite a challenge, specifically when using XPath. When you have an XMLTag instance, you have access to the following methods to manage namespaces in the document:

#### Adding and retrieving namespaces and prefixes ####

__addDefaultNamespace__

When you create an empty document, you can define a default namespace to use for the document. In example:

    XMLTag doc = XMLDoc.newDocument()
        .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
        .addRoot("html");

will produce:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <html xmlns="http://www.w3.org/2002/06/xhtml2/"/>

__addNamespace__

When you obtained an XMLTag instance, you can add any namespace you want. In example:

    XMLTag doc = XMLDoc.newDocument()
        .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
        .addNamespace("wicket", "http://wicket.sourceforge.net/wicket-1.0")
        .addRoot("html")
        .addTag("wicket:border")
        .gotoRoot().addTag("head")
        .addNamespace("other", "http://other-ns.com")
        .gotoRoot().addTag("other:foo");

will produce:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <html xmlns="http://www.w3.org/2002/06/xhtml2/">
        <wicket:border xmlns:wicket="http://wicket.sourceforge.net/wicket-1.0"/>
        <head/>
        <other:foo xmlns:other="http://other-ns.com"/>
    </html>

__Namespace prefix generation__

When you load an existing XML document, or when you define a default namespace in a new document, prefixes and namespaces are automatically found in the whole document. Often, XML documents have default namespace. This is often the case for example in XHTML documents, like below. For this case, XMLDoc will generate for you a prefix that you can use for XPath navigation, and register the namespace as being the default one.

In example, the following document will have a default namespace and also a prefix generated to access it: `ns0`.

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
        <head>
            <title/>
        </head>
        <body/>
    </html>

    XMLTag doc = XMLDoc.from(...);
    assertEquals(doc.getPrefix("http://www.w3.org/1999/xhtml"), "ns0");
    assertEquals(doc.getContext().getNamespaceURI("ns0"), "http://www.w3.org/1999/xhtml");

The prefix 'ns0' has been generated in the namespace context of the document so that XPath expression can use it.

You can access the javax.xml.namespace.NamespaceContext like this:

    NamespaceContext ctx = doc.getContext();

#### Prefix constraints ####

You cannot override an already defined prefix in a context, and you cannot override default XML prefixes. The following 3 attempts will throw an exception:

    // these prefixes are reserved
    XMLDoc.newDocument().addRoot("html").addNamespace("xml", "http://ns0");
    XMLDoc.newDocument().addRoot("html").addNamespace("xmlns", "http://ns0");
    
    // shows namespace generation prefix: when we add default prefix, 'ns0' is also created (or another if it already exists). So we cannot bind another namespace to this prefix.
    XMLDoc.newDocument()
        .addDefaultNamespace("http://def")
        .addRoot("html")
        .addNamespace("ns0", "http://ns0");

### XML elements operations ###

#### On elements ####

Operations affecting elements: `hasTag`, `addTag`, `getCurrentTag`, `getCurrentTagName`, `deleteChilds`, `delete`, `renameTo`

__hasTag__

Check for the existence of a tag.

__addTag__

Create a new tag

    System.out.println(XMLDoc.newDocument(true)
            .addRoot("html")
            .addTag("head")
            .toString());

outputs:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html>
        <head/>
    </html>

__getCurrentTag__

Returns the current `org.w3c.dom.Element`.

__getCurrentTagName__

Returns the current tag name.

    System.out.println(XMLDoc.newDocument(true).addRoot("html").getCurrentTagName());

outputs:

    html

__delete__

Deletes the current tag. The parent tag of the deleted tag becomes  one the current tag. If we call delete on the root tag, an exception is thrown. Root node can only be renamed.

    System.out.println(XMLDoc.newDocument(true)
            .addRoot("html")
            .addTag("head")
            .delete()
            .toString());

outputs:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html/>

__deleteChilds__

Deletes all tags under the current tag.

    System.out.println(XMLDoc.newDocument(true)
            .addRoot("html")
            .addTag("head").addTag("title")
            .toString());
    System.out.println(XMLDoc.newDocument(true)
            .addRoot("html")
            .addTag("head").addTag("title")
            .gotoRoot().deleteChilds()
            .toString());

outputs:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html>
        <head>
            <title/>
        </head>
    </html>
    
    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html/>

__renameTo__

Rename a tag to another name.

    System.out.println(XMLDoc.newDocument(true)
        .addRoot("html")
        .renameTo("xhtml")
        .toString());

outputs:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html/>

#### On attributes ####

Operations affecting elements: `hasAttribute`, `getAttributeNames`, `getAttribute`, `deleteAttributes`, `deleteAttribute`

Supposing we load the following XML file:

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE html
            PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <title>my title</title>
    </head>
    <body>
        <div id="header" class="banner"></div>
        <div id="content" class="cool"></div>
        <div id="footer" class="end"></div>
    </body>
    </html>

__hasAttribute__

Check for the existence of an attribute.

__getAttributeNames__

Returns a list of attribute names of the current tag.

    String[] names = XMLDoc.from(resource("test.xhtml"), true)
            .gotoTag("body/div[1]")
            .getAttributeNames();
    System.out.println(Arrays.toString(names));

outputs:

    [class, id]

__getAttribute__

Returns an attribute value of the current tag or the selected tag by the XPath expression. If the attribute does not exist, throws an exception.

    System.out.println(XMLDoc.from(resource("test.xhtml"), true)
            .gotoTag("body/div[1]")
            .getAttribute("class"));
    System.out.println(XMLDoc.from(resource("test.xhtml"), true)
            .getAttribute("class", "body/div[2]"));

outputs:

    banner
    cool

__deleteAttributes__

Deletes all attributes of a the current tag.

    System.out.println(XMLDoc.from(resource("test.xhtml"), true)
        .gotoTag("body/div[1]")
        .deleteAttributes()
        .toString());

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
        <title>my title</title>
    </head>
    <body>
        <div/>
        <div class="cool" id="content"/>
        <div class="end" id="footer"/>
    </body>
    </html>

__deleteAttribute__

Deletes a specific attribute. If it does not exist, an exception is thrown.

    System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true)
            .hasAttribute("id", "body/div[1]"));
    System.out.println(XMLDoc.from(getClass().getResource("/test.xhtml"), true)
            .gotoTag("body/div[1]").deleteAttribute("id")
            .hasAttribute("id"));

    true
    false

#### On text and data ####

Operations affecting elements: `addText`, `addCDATA`, `getAttribute`, `deleteAttributes`, `deleteAttribute`

__addText__, __addCDATA__

Adds text or CDATA sections to the document. As you have seen above, you can mix text, data and tags under one tag. When we add text or data, the current tag automatically becomes the parent tag. This behavior facilitate document creation since most of the time you will have to add one text or one data per tag like this:

    System.out.println(XMLDoc.newDocument(true)
        .addRoot("html")
        .addTag("head").addText("<\"!@#$%'^&*()>")
        .addTag("body").addCDATA("<\"!@#$%'^&*()>")
        .toString());

which gives:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <html>
        <head>&lt;"!@#$%'^&amp;*()&gt;</head>
        <body><![CDATA[<"!@#$%'^&*()>]]></body>
    </html>

__getText__, __getCDATA__

Returns the text or data contained in the current tag or the targetted tag with the XPath expression. If the tag has no text, returns "".

Given:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <html xmlns="http://www.w3.org/2002/06/xhtml2/" xmlns:ns0="http://wicket.sourceforge.net/wicket-1.0">
        <head>
            <title ns0:id="titleID">my special title: &lt;"!@#$%'^&amp;*()&gt;</title>
        </head>
        <body>
            <![CDATA[my special data: ]]>
            <ns0:border>
                <div/>
                child1
            </ns0:border>
            <ns0:border>child2</ns0:border>
            <![CDATA[<"!@#$%'^&*()>]]>
            <ns0:border>child3</ns0:border>
        </body>
    </html>

The following assertions are true:

    assertEquals(doc.getCurrentTag().getNodeType(), Document.ELEMENT_NODE);
    assertEquals(doc.getCurrentTagName(), "html");
    assertEquals(doc.getCurrentTagName(), "html");
    assertEquals(doc.getPefix("http://www.w3.org/2002/06/xhtml2/"), "ns1"); // ns0 is already used in the document
    assertEquals(doc.gotoTag("ns1:head/ns1:title").getText(), "my special title: <\"!@#$%'^&*()>");
    assertEquals(doc.getText("."), "my special title: <\"!@#$%'^&*()>");
    assertEquals(doc.getCDATA("../../ns1:body"), "my special data: <\"!@#$%'^&*()>");
    assertEquals(doc.getAttribute("ns0:id"), "titleID");

NB: we loaded the document by not ignorign namespaces. That's why you see required ns prefixes in XPath expressions.

### Navigation, XPath and Callback support ###

#### Raw XPath ####

You can execute RAW XPath directly through Java Xpath API by using `rawXpath` methods:

 * Boolan XMLTag.rawXpathBoolean(...)
 * Number XMLTag.rawXpathNumber(...)
 * String XMLTag.rawXpathString(...)
 * Node XMLTag.rawXpathNode(...)
 * NodeList XMLTag.rawXpathNodeSet(...)

#### Gotos ####

Navigation in the document is achieved by `gotos` methods

__gotoParent__

Returns to the parent tag, or remain to the root tag if we are already in the root tag.

__gotoRoot__

As it says, goes to the root tag.

__gotoChild__

Goes to the only existing child of a tag. It is just a useful method to traverse XML document from child to child when there are only one child per element. If you call this method when you are in a tag that does not contain exactly one child element, the method will throw an exception.

__gotoChild(int i)__

Goes to the Nth child of the current element. Index is from 1 up to `child number`, exactly like XPath array selection (child[i]) If the child at given position does not exist, an exception is thrown.

__gotoChild(String name)__

Goes to to the unique existing child element having given name. If there is no child with this name, or if there are more than one, an exception will be thrown.

__gotoTag(String relativeXpath, Object... arguments)__

Goes to to a tag element given an XPath expression. `arguments` is useful to parametrize the XPath expression with namespace prefixes for example. It uses String.format(). Remember when using XPath on a document with namespaces, you must always use prefixes even when the document has a default namespace.

__Example:__

Given:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <html xmlns="http://www.w3.org/2002/06/xhtml2/" xmlns:w="http://wicket.sourceforge.net/wicket-1.0">
        <head>
            <title w:id="title"/>
        </head>
        <body>
            <w:border>
                <div/>
                child1
            </w:border>
            <w:border>child2</w:border>
            <w:border>child3</w:border>
        </body>
    </html>

We can browse the above document like this:

    XMLTag doc = XMLDoc.from(getClass().getResource("/goto.xml"), false);
            String ns = doc.getPefix("http://www.w3.org/2002/06/xhtml2/");
            doc.gotoChild("head")      // jump to the only 'head' tag under 'html'
                    .gotoChild()       // jump to the only child of 'head'
                    .gotoRoot()        // go to 'html'
                    .gotoChild(2)      // go to child 'body'
                    .gotoChild(3)      // go to third child 'w:border' having text 'child3'
                    .gotoRoot()        // return to root
                    .gotoTag("%1$s:body/w:border[1]/%1$s:div", ns); // xpath navigation with namespace

Notice the Xpath expression when we use namespace: as we load an existing document, we can get generated prefix for a namespace with the `getPrefix` method. Then we can use this generated prefix in our XPath. `%1$s` means that we take the first argument provided (see String.format() documentation). If you debug, you will see that the XPath expression is `ns0:body/w:border[1]/ns0:div`.

#### Callbacks on selected nodes ####

Callbacks: `forEach`, `forEachChilds`

XMLTool enables you to execute callback actions for each node selected or each child nodes.

__Example:__

If we take back the XHTML example:

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE html
            PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <title>my title</title>
    </head>
    <body>
        <div id="header" class="banner"></div>
        <div id="content" class="cool"></div>
        <div id="footer" class="end"></div>
    </body>
    </html>

And we execute:

    XMLDoc.from(getClass().getResource("/test.xhtml"), true).forEachChild(new CallBack() {
        public void execute(XMLTag doc) {
            System.out.println(doc.getCurrentTagName());
        }

    XMLDoc.from(getClass().getResource("/test.xhtml"), true).forEach(new CallBack() {
        public void execute(XMLTag doc) {
            System.out.println(doc.getAttribute("id"));
        }
    }, "//div");

We obtain:

    head
    body
    header
    content
    footer

### Converting your XML document ###

Document conversion is done through `to*` methods.

__toDocument__

Converts to an org.w3c.dom.Document instance.

__toString__
__toString(String encoding)__

Converts to a formatted string, optionally giving an encoding. 

__toBytes__

Convert to a byte array

__toResult__, __toStream__

Converts to streams. 

__Example:__

    XMLDoc.newDocument(true).addRoot("html")
        .toResult(new DOMResult())
        .toStream(new StringWriter())
        .toStream(new ByteArrayOutputStream());

### Validating your XML document ###

XML validation enables to validate current document against a shema. Of course, to use this functionnality you need to create a document that does not ignore namespaces.

__validate__

This method is used to validate the document against schemas. It returns a ValidationResult instance containing all warning and error issued during validation.

__Example:__

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <html xmlns="http://www.w3.org/2002/06/xhtml2/" xmlns:w="http://wicket.sourceforge.net/wicket-1.0">
        <head>
            <title w:id="title"/>
        </head>
        <body>
            <w:border>
                <div/>
                child1
            </w:border>
            <w:border>child2</w:border>
            <w:border>child3</w:border>
        </body>
    </html>

 * Namespace http://www.w3.org/2002/06/xhtml2/ is defined in schema http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd
 * Namespace http://wicket.sourceforge.net/wicket-1.0 is defined in schema http://wicket.sourceforge.net/wicket-1.0.xsd

If we validate the XML document goto.xml seen above:

    ValidationResult results = XMLDoc.from(getClass().getResource("/goto.xml")).validate(
            new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"),
            new URL("http://wicket.sourceforge.net/wicket-1.0.xsd")
    );
    assertFalse(results.hasError());

If we validate the following document created by us below:

    results = XMLDoc.newDocument()
            .addDefaultNamespace("http://www.w3.org/2002/06/xhtml2/")
            .addRoot("htmlZZ")
            .validate(new URL("http://www.w3.org/MarkUp/SCHEMA/xhtml2.xsd"));
    assertTrue(results.hasError());
    System.out.println(Arrays.deepToString(results.getErrorMessages()));

The output is:

    [cvc-elt.1: Cannot find the declaration of element 'htmlxxx'.]

### Exception handling ###

Each operation causing an exception throws a XMLDocumentException with a described message.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/c89084a008d60a2378ce0b480d883d8e "githalytics.com")](http://githalytics.com/mycila/xmltool)
