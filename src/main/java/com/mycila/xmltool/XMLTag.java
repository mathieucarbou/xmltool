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

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.List;

/**
 * Enables you to build, parse, navigate, modify XML documents through a simple and intuitive fluent interface.
 * <p/>
 * {@code XMLTag} is <strong>not thread-safe</strong> and cannot be considered so, even for non mutating methods.
 * This is due to the current location which needs to be kept, as we work in a fluent interface style.
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface XMLTag {


    ////////////////// BUILD METHODS - add* //////////////////////


    /**
     * Add a namespace to the document
     *
     * @param prefix       The prefix of the namespace
     * @param namespaceURI The URI of the namespace
     * @return this
     */
    com.mycila.xmltool.XMLTag addNamespace(String prefix, String namespaceURI);

    /**
     * Create a tag under the current location and use it as the current node
     *
     * @param name Name of the element to add
     * @return this
     */
    com.mycila.xmltool.XMLTag addTag(String name);

    /**
     * Create a new attribute for the current node
     *
     * @param name  Name of the attribute to add
     * @param value value of the attribute to add
     * @return this
     */
    com.mycila.xmltool.XMLTag addAttribute(String name, String value);

    /**
     * Add a text node under the current node, and jump to the parent node. This enables the create or quick documents like this:
     * <p/>
     * <code>addTag("name").addText("Bob")addTag("sex").addText("M")addTag("age").addText("30")</code>
     * <p/>
     * {@code <name>Bob</name><sex>M</sex><age>30</age>}
     *
     * @param text the text to add
     * @return this
     */
    com.mycila.xmltool.XMLTag addText(String text);

    /**
     * Add a data node under the current node, and jump to the parent node. This enables the create or quick documents like this:
     * <p/>
     * <code>addTag("name").addCDATA("Bob")addTag("sex").addCDATA("M")addTag("age").addCDATA("30")</code>
     * <p/>
     * {@code <name><![CDATA[Bob]]></name><sex><![CDATA[M]]></sex><age><![CDATA[30]]></age>}
     *
     * @param data the data to add
     * @return this
     */
    com.mycila.xmltool.XMLTag addCDATA(String data);

    /**
     * Inserts another {@link com.mycila.xmltool.XMLTag} instance under the current tag. The whole document will be inserted.
     *
     * @param tag The {@link com.mycila.xmltool.XMLTag} instance to insert
     * @return this
     */
    com.mycila.xmltool.XMLTag addDocument(com.mycila.xmltool.XMLTag tag);

    /**
     * Inserts another {@link org.w3c.dom.Document} instance under the current tag
     *
     * @param doc The {@link org.w3c.dom.Document} instance to insert
     * @return this
     */
    com.mycila.xmltool.XMLTag addDocument(Document doc);

    /**
     * Inserts another {@link com.mycila.xmltool.XMLTag} tag hierarchy under the current tag. Only the current tag of the given document
     * will be inserted with its hierarchy, not the whole document.
     *
     * @param tag The {@link com.mycila.xmltool.XMLTag} current tag hierarchy to insert
     * @return this
     */
    com.mycila.xmltool.XMLTag addTag(com.mycila.xmltool.XMLTag tag);

    /**
     * Inserts a {@link org.w3c.dom.Element} instance and its hierarchy under the current tag
     *
     * @param tag The {@link org.w3c.dom.Element} instance to insert
     * @return this
     */
    com.mycila.xmltool.XMLTag addTag(Element tag);

    /**
     * Add given attribute to current element
     *
     * @param attr The attribute to insert
     * @return this
     */
    com.mycila.xmltool.XMLTag addAttribute(Attr attr);

    /**
     * Add a text note to the current tag
     *
     * @param text The node to insert
     * @return this
     */
    com.mycila.xmltool.XMLTag addText(Text text);

    /**
     * Add a CDATA note to the current tag
     *
     * @param data The node to insert
     * @return this
     */
    com.mycila.xmltool.XMLTag addCDATA(CDATASection data);

    ////////////////// NAVIGATION METHODS - goto* //////////////////////


    /**
     * Go to parent tag. Do nothing if we are already at root
     *
     * @return this
     */
    com.mycila.xmltool.XMLTag gotoParent();

    /**
     * Go to document root tag
     *
     * @return this
     */
    com.mycila.xmltool.XMLTag gotoRoot();

    /**
     * Go to a specific node
     *
     * @param relativeXpath XPath expresion
     * @param arguments     to be replaced in xpath expression before compiling. Uses String.format() to build XPath expression.
     * @return this
     * @throws com.mycila.xmltool.XMLDocumentException
     *          if the node does not exist or if the XPath expression is invalid
     */
    com.mycila.xmltool.XMLTag gotoTag(String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * Go to the only child element of the curent node.
     *
     * @return this
     * @throws XMLDocumentException If the current node has several childs or no child at all
     */
    com.mycila.xmltool.XMLTag gotoChild() throws XMLDocumentException;

    /**
     * Go to the Nth child of the curent node.
     *
     * @param i index of the child, from 1 to child element number
     * @return this
     * @throws XMLDocumentException If the child node does not exist
     */
    com.mycila.xmltool.XMLTag gotoChild(int i) throws XMLDocumentException;

    /**
     * Go to the child found with given node name
     *
     * @param nodeName name of the child to find.
     * @return this
     * @throws XMLDocumentException If the element with this name has not been found or if there are too many elements
     */
    com.mycila.xmltool.XMLTag gotoChild(String nodeName) throws XMLDocumentException;

    /**
     * Go to the first child element of the curent node.
     *
     * @return this
     * @throws XMLDocumentException If the current node has no child at all
     */
    com.mycila.xmltool.XMLTag gotoFirstChild() throws XMLDocumentException;

    /**
     * Go to the first child occurance found having given name
     *
     * @param name Name of the child to go at
     * @return this
     * @throws XMLDocumentException If the current node has no child at all
     */
    com.mycila.xmltool.XMLTag gotoFirstChild(String name) throws XMLDocumentException;

    /**
     * Go to the lastest child element of the curent node.
     *
     * @return this
     * @throws XMLDocumentException If the current node has no child at all
     */
    com.mycila.xmltool.XMLTag gotoLastChild() throws XMLDocumentException;

    /**
     * Go to the last child occurance found having given name
     *
     * @param name Name of the child to go at
     * @return this
     * @throws XMLDocumentException If the current node has no child at all
     */
    com.mycila.xmltool.XMLTag gotoLastChild(String name) throws XMLDocumentException;

    ////////////////// EXISTENCE CHECKS - has* //////////////////////

    /**
     * Check if a tag exist in the document
     *
     * @param relativeXpath XPath expression where the tag should be located
     * @param arguments     XPath arguments. Uses String.format() to build XPath expression.
     * @return true if the tag is exist
     */
    boolean hasTag(String relativeXpath, Object... arguments);

    /**
     * Check if targeted tag has an attribute of given name
     *
     * @param name          the name of the attribute
     * @param relativeXpath XPath that target the tag
     * @param arguments     optional arguments of xpath expression. Uses String.format() to build XPath expression.
     * @return true if the tag exist with this attribute name
     * @throws XMLDocumentException If the targetted node does not exist or if xpath expression is not valid
     */
    boolean hasAttribute(String name, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * Check wheter current tag contains an atribute
     *
     * @param name Attribute name
     * @return true if the attribute is in current tag
     */
    boolean hasAttribute(String name);

    ////////////////// CLOSURE - for* //////////////////////

    /**
     * Execute an action for each child in the current node.
     *
     * @param callBack Callback method to run after the current tag of the document has changed to a child
     * @return this
     */
    com.mycila.xmltool.XMLTag forEachChild(CallBack callBack);

    /**
     * Execute an action for each selected tags from the current node.
     *
     * @param callBack      Callback method to run after the current tag of the document has changed to a child
     * @param relativeXpath XXath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return this
     */
    com.mycila.xmltool.XMLTag forEach(CallBack callBack, String relativeXpath, Object... arguments);

    com.mycila.xmltool.XMLTag forEach(String xpath, CallBack callBack);

    ////////////////// DATA ACCESS METHODS - raw xpath * //////////////////////

    /**
     * Execute an XPath expression directly using the Java XPath API, from the current node.
     *
     * @param relativeXpath The XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return The {@link javax.xml.xpath.XPathConstants#STRING} return type
     */
    String rawXpathString(String relativeXpath, Object... arguments);

    /**
     * Execute an XPath expression directly using the Java XPath API, from the current node.
     *
     * @param relativeXpath The XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return The {@link javax.xml.xpath.XPathConstants#NUMBER} return type
     */
    Number rawXpathNumber(String relativeXpath, Object... arguments);

    /**
     * Execute an XPath expression directly using the Java XPath API, from the current node.
     *
     * @param relativeXpath The XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return The {@link javax.xml.xpath.XPathConstants#BOOLEAN} return type
     */
    Boolean rawXpathBoolean(String relativeXpath, Object... arguments);

    /**
     * Execute an XPath expression directly using the Java XPath API, from the current node.
     *
     * @param relativeXpath The XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return The {@link javax.xml.xpath.XPathConstants#NODE} return type
     */
    Node rawXpathNode(String relativeXpath, Object... arguments);

    /**
     * Execute an XPath expression directly using the Java XPath API, from the current node.
     *
     * @param relativeXpath The XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return The {@link javax.xml.xpath.XPathConstants#NODESET} return type
     */
    NodeList rawXpathNodeSet(String relativeXpath, Object... arguments);

    ////////////////// DATA ACCESS METHODS - get* //////////////////////

    /**
     * @return the current tag
     */
    Element getCurrentTag();

    /**
     * @return The number of child for the current tag
     */
    int getChildCount();

    /**
     * <pre>{@code
     * XMLTag tag = XMLDoc.newDocument(true)
     *         .addRoot("root").addTag("a")
     *         .gotoParent().addTag("b")
     *         .gotoParent().addTag("c")
     *         .gotoRoot();
     * assertEquals(tag.getCurrentTagName(), "root");
     * for (XMLTag xmlTag : tag.getChilds()) {
     *     if(xmlTag.getCurrentTagName().equals("b")) {
     *         break;
     *     }
     * }
     * assertEquals(tag.getCurrentTagName(), "b");}</pre>
     * <p/>
     * <pre>{@code
     * XMLTag tag = XMLDoc.newDocument(true)
     *         .addRoot("root").addTag("a")
     *         .gotoParent().addTag("b")
     *         .gotoParent().addTag("c")
     *         .gotoRoot();
     * assertEquals(tag.getCurrentTagName(), "root");
     * for (XMLTag xmlTag : tag.getChilds()) {
     *     System.out.println(xmlTag.getCurrentTagName());
     * }
     * assertEquals(tag.getCurrentTagName(), "root");}</pre>
     *
     * @return An iterable object over childs
     */
    Iterable<com.mycila.xmltool.XMLTag> getChilds();

    /**
     * Create an iterable object over selected elements. Act as the getChilds method: The current position of XMLTag is modified at each iteration.
     * Thus, if you break in the iteration, the current position will not be the same as the position before.
     *
     * @param relativeXpath XPath to select tags
     * @param arguments     XPath arguments
     * @return The iterable object
     */
    Iterable<com.mycila.xmltool.XMLTag> getChilds(String relativeXpath, Object... arguments);

    /**
     * @return The child element's list
     */
    List<Element> getChildElement();

    /**
     * @return the current tag name
     */
    String getCurrentTagName();

    /**
     * @return An XPath expression representing the current tag location in the document. If you are in the root node and run gotoTag() giving
     *         this XPath expression, you will return to the current node.
     */
    String getCurrentTagLocation();

    /**
     * @return the namespace context
     */
    NamespaceContext getContext();

    /**
     * Get the prefix of a namespace
     *
     * @param namespaceURI The URI of the namespace
     * @return the prefix or "" if not found ("" is the default prefix - see {@link javax.xml.namespace.NamespaceContext javadoc})
     */
    String getPefix(String namespaceURI);

    /**
     * Get all bound prefixes of a namespace
     *
     * @param namespaceURI The URI of the namespace
     * @return a list of prefixes or an empty array.
     */
    String[] getPefixes(String namespaceURI);

    /**
     * @return The text content of the current node, "" if none
     */
    String getText();

    /**
     * Get the text of a sepcific node
     *
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return the text of "" if none
     * @throws XMLDocumentException If the XPath expression is not valid or if the node does not exist
     */
    String getText(String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * @return The CDATA content of the current node, "" if none
     */
    String getCDATA();

    /**
     * Get the CDATA of a selected node
     *
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return the text of "" if none
     * @throws XMLDocumentException If the XPath expression is invalid or if the node does not exist
     */
    String getCDATA(String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * @return The text content of the current node, if none tries to get the CDATA content, if none returns ""
     */
    String getTextOrCDATA();

    /**
     * Get the text of a sepcific node
     *
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return The text content of the current node, if none tries to get the CDATA content, if none returns ""
     * @throws XMLDocumentException If the XPath expression is not valid or if the node does not exist
     */
    String getTextOrCDATA(String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * @return The CDATA content of the current node, if none tries to get the text content, if none returns ""
     */
    String getCDATAorText();

    /**
     * Get the text of a sepcific node
     *
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return The CDATA content of the current node, if none tries to get the text content, if none returns ""
     * @throws XMLDocumentException If the XPath expression is not valid or if the node does not exist
     */
    String getCDATAorText(String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * @return all attribute names of current node
     */
    String[] getAttributeNames();

    /**
     * returns the attribute value of the current node
     *
     * @param name attribute name
     * @return attribute value
     * @throws XMLDocumentException If the attribute does not exist
     */
    String getAttribute(String name) throws XMLDocumentException;

    /**
     * Returns the attribute value of the node pointed by given XPath expression
     *
     * @param name          attribute name
     * @param relativeXpath XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return attribute value
     * @throws XMLDocumentException Attribute does not exist, targetted node or XPath expression is invalid
     */
    String getAttribute(String name, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * returns the attribute value of the current node or null if the attribute does not exist
     *
     * @param name attribute name
     * @return attribute value or null if no attribute
     */
    String findAttribute(String name);

    /**
     * Returns the attribute value of the node pointed by given XPath expression or null if the attribute does not exist
     *
     * @param name          attribute name
     * @param relativeXpath XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return the attribute value or null if the attribute does not exist
     * @throws XMLDocumentException targetted node does not exist or XPath expression is invalid
     */
    String findAttribute(String name, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * @return Another XMLTag instance in which the current tag becomes de root element of the new document, and it contains all inner elements as in the previous document.
     */
    com.mycila.xmltool.XMLTag getInnerDocument();

    /**
     * @return The text representation of the inner nodes of the current node. The current node is outputed as the root element of inner tags.
     */
    String getInnerText();

    /**
     * @return A new com.mycila.xmltool.XMLTag instance having the same properties and documents of the current instance.
     *         The current tag will also remain the same (this.getCurrentTagName().equals(this.duplicate().getCurrentTagName()))
     */
    com.mycila.xmltool.XMLTag duplicate();


    ////////////////// MTATION METHODS - set* //////////////////////


    /**
     * Set the text in the current node. This method will replace all existing text and cdata by the given text.
     * Also Jump after to the parent node. This enables the quicly replace test on several nodes like this:
     * <p/>
     * <code>gotoChild("name").setText("Bob").gotoChild("sex").setText("M").gotoChild("age").setText("30")</code>
     * <p/>
     * {@code <name>Bob</name><sex>M</sex><age>30</age>}
     *
     * @param text text to put under this node
     * @return this
     */
    com.mycila.xmltool.XMLTag setText(String text);

    /**
     * Set the text in the targetted node. This method will replace all existing text and cdata by the given text, but remains on the current tag.
     *
     * @param text          text to put under this node
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return this
     * @throws XMLDocumentException If the XPath expression is invalid or if the node does not exist
     */
    com.mycila.xmltool.XMLTag setText(String text, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * Set the text in the targetted node. This method will replace all existing text and cdata by the given text, but remains on the current tag.
     * If the targetted node does not exist, do nothing.
     *
     * @param text          text to put under this node
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return this
     * @throws XMLDocumentException If the XPath expression is invalid
     */
    com.mycila.xmltool.XMLTag setTextIfExist(String text, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * Set the cdata in the current node. This method will replace all existing text and cdata by the given cdata
     * Also Jump after to the parent node. This enables the quicly replace test on several nodes like this:
     * <p/>
     * <code>gotoChild("name").setText("Bob").gotoChild("sex").setText("M").gotoChild("age").setText("30")</code>
     * <p/>
     * {@code <name>Bob</name><sex>M</sex><age>30</age>}
     *
     * @param data text to put under this node in a cdata section
     * @return this
     */
    com.mycila.xmltool.XMLTag setCDATA(String data);

    /**
     * Set the cdata in the targetted node. This method will replace all existing text and cdata by the given cdata, but remains on the current tag.
     *
     * @param data          text to put under this node in a cdata section
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return this
     * @throws XMLDocumentException If the XPath expression is invalid or if the node does not exist
     */
    com.mycila.xmltool.XMLTag setCDATA(String data, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * Set the cdata in the targetted node. This method will replace all existing text and cdata by the given cdata, but remains on the current tag.
     * If the targetted node does not exist, do nothing.
     *
     * @param data          text to put under this node in a cdata section
     * @param relativeXpath XPath expression that select the node
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return this
     * @throws XMLDocumentException If the XPath expression is invalid
     */
    com.mycila.xmltool.XMLTag setCDATAIfExist(String data, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * Sets the new value on an existign attribute, and remains on the current tag.
     *
     * @param name  attribute name
     * @param value new attribute'svalue
     * @return attribute value
     * @throws XMLDocumentException If the attribute does not exist
     */
    com.mycila.xmltool.XMLTag setAttribute(String name, String value) throws XMLDocumentException;

    /**
     * Sets the new value on an attribute, and remains on the current tag. If it does not exist, do nothing.
     *
     * @param name  attribute name
     * @param value new attribute value
     * @return attribute value
     */
    com.mycila.xmltool.XMLTag setAttributeIfExist(String name, String value);

    /**
     * Sets the new value on a targetted node's attribute, and remains on the current tag.
     *
     * @param name          attribute name
     * @param value         new attribute's value
     * @param relativeXpath XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return attribute value
     * @throws XMLDocumentException Attribute does not exist, targetted node does not exit, or XPath expression is invalid
     */
    com.mycila.xmltool.XMLTag setAttribute(String name, String value, String relativeXpath, Object... arguments) throws XMLDocumentException;

    /**
     * Sets the new value on a targetted node's attribute, and remains on the current tag.
     * If the attribute does not exist, do nothing.
     *
     * @param name          attribute name
     * @param value         new attribute's value
     * @param relativeXpath XPath expression
     * @param arguments     facultative Xpath expression arguments. Uses String.format() to build XPath expression.
     * @return attribute value
     * @throws XMLDocumentException XPath expression is invalid or targetted node does not exist
     */
    com.mycila.xmltool.XMLTag setAttributeIfExist(String name, String value, String relativeXpath, Object... arguments) throws XMLDocumentException;


    ////////////////// DELETE / CHANGE METHODD //////////////////////


    /**
     * Delete all existing elements of this node
     *
     * @return this
     */
    com.mycila.xmltool.XMLTag deleteChilds();

    /**
     * Delete current tag and its childs. If the current tag is the root tag of xml document, it cannot be
     * delete. It can just be replaced. In this case, an exception is thrown
     *
     * @return this
     * @throws XMLDocumentException if the current node is the root node
     */
    com.mycila.xmltool.XMLTag delete() throws XMLDocumentException;

    /**
     * Delete all existing attributes of current node
     *
     * @return this
     */
    com.mycila.xmltool.XMLTag deleteAttributes();

    /**
     * Delete an attribute of the current node.
     *
     * @param name attribute name
     * @return this
     * @throws XMLDocumentException if the attribute does not exist
     */
    com.mycila.xmltool.XMLTag deleteAttribute(String name) throws XMLDocumentException;

    /**
     * Delete an attribute of the current node, if it exists
     *
     * @param name attribute name
     * @return this
     */
    com.mycila.xmltool.XMLTag deleteAttributeIfExists(String name);

    /**
     * Replace current element name by another name
     *
     * @param newTagName New name of the tag
     * @return this
     */
    com.mycila.xmltool.XMLTag renameTo(String newTagName);

    /**
     * Remove any prefix and namespaces contained in the tag name, childs and attributes, thus changing namespace and tag name.
     * This can be very useful if you are working in a document when you want to ignore namespaces, if you don't know the tag prefix
     *
     * @return this
     */
    com.mycila.xmltool.XMLTag deletePrefixes();

    ////////////////// TRANSFORMATION METHODS - to* //////////////////////


    /**
     * @return the Document
     */
    Document toDocument();

    /**
     * @return This document as {@link javax.xml.transform.Source}
     */
    Source toSource();

    /**
     * @return a string representation of the document in UTF-8 (Unicode)
     */
    String toString();

    /**
     * @param encoding destination encoding of XML document
     * @return a string representation of the document
     */
    String toString(String encoding);

    /**
     * @return This document representation as String bytes, using default encoding of the document
     */
    byte[] toBytes();

    /**
     * @param encoding The encoding to use
     * @return This document representation as String bytes using sepcified ancoding
     */
    byte[] toBytes(String encoding);

    /**
     * Converts this document to the result provided
     *
     * @param out The output result
     * @return this
     */
    com.mycila.xmltool.XMLTag toResult(Result out);

    /**
     * Converts this document to the result provided, overriding default encoding of xml document
     *
     * @param out      The output result
     * @param encoding The new encoding
     * @return this
     */
    com.mycila.xmltool.XMLTag toResult(Result out, String encoding);

    /**
     * Write this document to a stream
     *
     * @param out The output result
     * @return this
     */
    com.mycila.xmltool.XMLTag toStream(OutputStream out);

    /**
     * Write this document to a stream
     *
     * @param out      The output result
     * @param encoding The new encoding
     * @return this
     */
    com.mycila.xmltool.XMLTag toStream(OutputStream out, String encoding);

    /**
     * Write this document to a stream
     *
     * @param out The output result
     * @return this
     */
    com.mycila.xmltool.XMLTag toStream(Writer out);

    /**
     * Write this document to a stream
     *
     * @param out      The output result
     * @param encoding The new encoding
     * @return this
     */
    com.mycila.xmltool.XMLTag toStream(Writer out, String encoding);

    /**
     * @return The {@link javax.xml.transform.Result} representation of this document. Useful when using web services for example.
     */
    Result toResult();

    /**
     * @param encoding The new encoding
     * @return The {@link javax.xml.transform.Result} representation of this document. Useful when using web services for example.
     */
    Result toResult(String encoding);

    /**
     * @return A stream where the document has already been written into
     */
    OutputStream toOutputStream();

    /**
     * @param encoding The new encoding
     * @return A stream where the document has already been written into
     */
    OutputStream toOutputStream(String encoding);

    /**
     * @return A stream where the document has already been written into
     */
    Writer toWriter();

    /**
     * @param encoding The new encoding
     * @return A stream where the document has already been written into
     */
    Writer toWriter(String encoding);

    ////////////////// VALIDATION METHODS - validate* //////////////////////


    /**
     * Validate this document against specifief schemas
     *
     * @param schemas A list of schemas
     * @return A validation result object containing exception list occured if any
     */
    ValidationResult validate(Source... schemas);

    /**
     * Validate this document against specifief schemas
     *
     * @param schemaLocations A list of schemas
     * @return A validation result object containing exception list occured if any
     */
    ValidationResult validate(URL... schemaLocations);

}
