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

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.util.*;

import static com.mycila.xmltool.Utils.notEmpty;
import static com.mycila.xmltool.Utils.notNull;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDoc implements XMLTag {

    private final XMLDocDefinition definition;
    Element current;

    XMLDoc(XMLDocDefinition definition) {
        this.definition = definition;
        this.current = definition.getRoot();
    }

    public NamespaceContext getContext() {
        return definition;
    }

    public boolean hasAttribute(String name) {
        return current.hasAttribute(name);
    }

    public boolean hasAttribute(String name, String relativeXpath, Object... arguments) {
        Element old = current;
        try {
            return gotoTag(relativeXpath, arguments).hasAttribute(name);
        }
        finally {
            current = old;
        }
    }

    public boolean hasTag(String relativeXpath, Object... arguments) {
        Element old = current;
        try {
            gotoTag(relativeXpath, arguments);
            return true;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            current = old;
        }
    }

    public XMLTag forEachChild(CallBack callBack) {
        notNull("Callback", callBack);
        Element old = current;
        try {
            for (Element node : getChildElement()) {
                current = node;
                callBack.execute(this);
            }
            return this;
        }
        finally {
            current = old;
        }
    }

    public XMLTag forEach(CallBack callBack, String relativeXpath, Object... arguments) {
        notNull("Callback", callBack);
        Element old = current;
        try {
            Node[] nodes = definition.getXpath().findNodes(current, relativeXpath, arguments);
            List<Element> els = new ArrayList<Element>(nodes.length);
            for (Node node : nodes) {
                if (isElement(node)) {
                    els.add((Element) node);
                }
            }
            for (Element node : els) {
                current = node;
                callBack.execute(this);
            }
            return this;
        }
        finally {
            current = old;
        }
    }

    public XMLTag forEach(String xpath, CallBack callBack) {
        return forEach(callBack, xpath);
    }

    public String rawXpathString(String relativeXpath, Object... arguments) {
        return definition.getXpath().rawXpathString(current, relativeXpath, arguments);
    }

    public Number rawXpathNumber(String relativeXpath, Object... arguments) {
        return definition.getXpath().rawXpathNumber(current, relativeXpath, arguments);
    }

    public Boolean rawXpathBoolean(String relativeXpath, Object... arguments) {
        return definition.getXpath().rawXpathBoolean(current, relativeXpath, arguments);
    }

    public Node rawXpathNode(String relativeXpath, Object... arguments) {
        return definition.getXpath().rawXpathNode(current, relativeXpath, arguments);
    }

    public NodeList rawXpathNodeSet(String relativeXpath, Object... arguments) {
        return definition.getXpath().rawXpathNodeSet(current, relativeXpath, arguments);
    }

    public String getPefix(String namespaceURI) {
        notNull("namespaceURI", namespaceURI);
        String prefix = getContext().getPrefix(namespaceURI);
        return prefix == null ? XMLConstants.DEFAULT_NS_PREFIX : prefix;
    }

    @SuppressWarnings({"unchecked"})
    public String[] getPefixes(String namespaceURI) {
        notNull("namespaceURI", namespaceURI);
        Set<String> prefixes = new TreeSet<String>();
        Iterator<String> i = getContext().getPrefixes(namespaceURI);
        while (i.hasNext()) {
            prefixes.add(i.next());
        }
        return prefixes.toArray(new String[prefixes.size()]);
    }

    public XMLTag addNamespace(String prefix, String namespaceURI) {
        definition.addNamespace(prefix, namespaceURI);
        return this;
    }

    public XMLTag addDocument(XMLTag tag) {
        notNull("XMLTag instance", tag);
        return addDocument(tag.toDocument());
    }

    public XMLTag addDocument(Document doc) {
        notNull("DOM Document", doc);
        current.appendChild(current.getOwnerDocument().importNode(doc.getDocumentElement(), true));
        return this;
    }

    public XMLTag addTag(XMLTag tag) {
        notNull("XMLTag instance", tag);
        return addTag(tag.getCurrentTag());
    }

    public XMLTag addTag(Element tag) {
        notNull("DOM Element", tag);
        current.appendChild(current.getOwnerDocument().importNode(tag, true));
        return this;
    }

    public XMLTag addTag(String name) {
        Element el = definition.createElement(name);
        current.appendChild(el);
        current = el;
        return this;
    }

    public XMLTag addAttribute(String name, String value) {
        if (hasAttribute(name)) {
            throw new XMLDocumentException("Attribute '%s' already exist on tag '%s'", name, getCurrentTagName());
        }
        definition.createAttribute(current, name, value);
        return this;
    }

    public XMLTag addAttribute(Attr attr) {
        notNull("DOM Attribute", attr);
        if (hasAttribute(attr.getName())) {
            throw new XMLDocumentException("Attribute '%s' already exist on tag '%s'", attr.getName(), getCurrentTagName());
        }
        current.setAttributeNodeNS((Attr) current.getOwnerDocument().importNode(attr, true));
        return this;
    }

    public XMLTag addText(String text) {
        current.appendChild(definition.createText(text));
        return gotoParent();
    }

    public XMLTag addText(Text text) {
        notNull("DOM Text node", text);
        current.appendChild(current.getOwnerDocument().importNode(text, true));
        return gotoParent();
    }

    public XMLTag addCDATA(String data) {
        current.appendChild(definition.createCDATA(data));
        return gotoParent();
    }

    public XMLTag addCDATA(CDATASection data) {
        notNull("DOM CDATA node", data);
        current.appendChild(current.getOwnerDocument().importNode(data, true));
        return gotoParent();
    }

    public XMLTag delete() {
        if (current == definition.getRoot()) {
            throw new XMLDocumentException("Cannot delete root node '%s'", getCurrentTagName());
        }
        Node toDelete = current;
        gotoParent();
        current.removeChild(toDelete);
        return this;
    }

    public XMLTag deleteChilds() {
        List<Element> toDelete = getChildElement();
        for (Element node : toDelete) {
            current.removeChild(node);
        }
        return this;
    }

    public XMLTag deleteAttributes() {
        List<Attr> attrs = attr(current);
        for (Attr attr : attrs) {
            current.removeAttributeNode(attr);
        }
        return this;
    }

    public XMLTag deleteAttribute(String name) {
        notEmpty("Attribute name", name);
        if (!hasAttribute(name)) {
            throw new XMLDocumentException("Cannot delete attribute '%s' from element '%s': attribute does noe exist", name, getCurrentTagName());
        }
        current.removeAttribute(name);
        return this;
    }

    public XMLTag deleteAttributeIfExists(String name) {
        notEmpty("Attribute name", name);
        if (hasAttribute(name)) {
            current.removeAttribute(name);
        }
        return this;
    }

    public XMLTag renameTo(String newNodeName) {
        notEmpty("Tag name", newNodeName);
        current = definition.rename(current, newNodeName);
        return this;
    }

    public XMLTag deletePrefixes() {
        final boolean ignoreNS = definition.isIgnoreNamespaces();
        final String nsPref = XMLNS_ATTRIBUTE + ":";
        final Queue<Element> queue = new LinkedList<Element>();
        queue.offer(current);
        definition.resetNamespaces();
        while (!queue.isEmpty()) {
            Element tag = queue.poll();
            for (Attr attr : attr(tag)) {
                String attrName = attr.getNodeName();
                if (XMLNS_ATTRIBUTE.equals(attrName) || attrName.startsWith(nsPref)) {
                    tag.removeAttributeNode(attr);
                } else {
                    int pos = attrName.indexOf(":");
                    if (pos != -1) {
                        if (ignoreNS) {
                            definition.renameWithoutNS(attr, attrName.substring(pos + 1));
                        } else {
                            attr.setPrefix(null);
                        }
                    }
                }
            }
            String tagName = tag.getTagName();
            int pos = tagName.indexOf(":");
            if (pos != -1) {
                tagName = tagName.substring(pos + 1);
            }
            if (current == tag) {
                current = tag = definition.renameWithoutNS(tag, tagName);
            } else {
                tag = definition.renameWithoutNS(tag, tagName);
            }
            for (Element element : childs(tag)) {
                queue.offer(element);
            }
        }
        definition.readNamespaces();
        return this;
    }

    public XMLTag getInnerDocument() {
        return fromCurrentTag(this, definition.isIgnoreNamespaces());
    }

    public String getInnerText() {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, definition.getEncoding());
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter sw = new StringWriter();
            NodeList list = current.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                transformer.transform(new DOMSource(list.item(i)), new StreamResult(sw));
            }
            return sw.toString();
        }
        catch (TransformerException e) {
            throw new XMLDocumentException("Transformation error", e);
        }

    }

    public XMLTag gotoParent() {
        Node n = current.getParentNode();
        if (current != definition.getRoot() && isElement(n)) {
            current = (Element) n;
        }
        return this;
    }

    public XMLTag gotoRoot() {
        current = definition.getRoot();
        return this;
    }

    public XMLTag gotoChild() {
        List<Element> els = childs(current);
        switch (els.size()) {
            case 0:
                throw new XMLDocumentException("Current element '%s' has no child", getCurrentTagName());
            case 1:
                current = els.get(0);
                break;
            default:
                throw new XMLDocumentException("Cannot select child: current element '%s' has '%s' children", getCurrentTagName(), els.size());
        }
        return this;
    }

    public XMLTag gotoChild(int i) {
        List<Element> els = childs(current);
        if (i > 0 && i <= els.size()) {
            current = els.get(i - 1);
        } else {
            throw new XMLDocumentException("Cannot acces child '%s' of element '%s' amongst its '%s' childs", i, getCurrentTagName(), els.size());
        }
        return this;
    }

    public XMLTag gotoChild(String nodeName) {
        notEmpty("Tag name", nodeName);
        List<Element> els = childs(current);
        List<Element> found = new ArrayList<Element>(els.size());
        for (Element el : els) {
            if (el.getTagName().equals(nodeName)) {
                found.add(el);
            }
        }
        switch (found.size()) {
            case 0:
                throw new XMLDocumentException("Current element '%s' has no child named '%s'", getCurrentTagName(), nodeName);
            case 1:
                current = found.get(0);
                break;
            default:
                throw new XMLDocumentException("Cannot select child: current element '%s' has '%s' children named '%s'", getCurrentTagName(), found.size(), nodeName);
        }
        return this;
    }

    public XMLTag gotoFirstChild() throws XMLDocumentException {
        List<Element> els = childs(current);
        if (els.isEmpty()) {
            throw new XMLDocumentException("Current element '%s' has no child", getCurrentTagName());
        }
        current = els.get(0);
        return this;
    }

    public XMLTag gotoFirstChild(String name) throws XMLDocumentException {
        notEmpty("Tag name", name);
        List<Element> els = childs(current);
        if (els.isEmpty()) {
            throw new XMLDocumentException("Current element '%s' has no child", getCurrentTagName());
        }
        for (Element el : els) {
            if (el.getTagName().equals(name)) {
                current = el;
                return this;
            }
        }
        throw new XMLDocumentException("No child found in current tag '%s' having name '%s'", getCurrentTagName(), name);
    }

    public XMLTag gotoLastChild() throws XMLDocumentException {
        List<Element> els = childs(current);
        if (els.isEmpty()) {
            throw new XMLDocumentException("Current element '%s' has no child", getCurrentTagName());
        }
        current = els.get(els.size() - 1);
        return this;
    }

    public XMLTag gotoLastChild(String name) throws XMLDocumentException {
        notEmpty("Tag name", name);
        List<Element> els = childs(current);
        if (els.isEmpty()) {
            throw new XMLDocumentException("Current element '%s' has no child", getCurrentTagName());
        }
        for (int i = els.size() - 1; i >= 0; i--) {
            if (els.get(i).getTagName().equals(name)) {
                current = els.get(i);
                return this;
            }
        }
        throw new XMLDocumentException("No child found in current tag '%s' having name '%s'", getCurrentTagName(), name);
    }

    public XMLTag gotoTag(String relativeXpath, Object... arguments) {
        Node n = definition.getXpath().findNode(current, relativeXpath, arguments);
        if (!isElement(n)) {
            throw new XMLDocumentException("XPath expression '%s' does not target an element. Targeted node is '%s' (node type is '%s')", String.format(relativeXpath, arguments), n.getNodeName(), n.getNodeType());
        }
        current = (Element) n;
        return this;
    }

    public Element getCurrentTag() {
        return current;
    }

    public int getChildCount() {
        return getChildElement().size();
    }

    public Iterable<XMLTag> getChilds() {
        final IteratorAdapter iterator = new IteratorAdapter(this, getChildElement().iterator());
        return new Iterable<XMLTag>() {
            public Iterator<XMLTag> iterator() {
                return iterator;
            }
        };
    }

    public Iterable<XMLTag> getChilds(String relativeXpath, Object... arguments) {
        Node[] nodes = definition.getXpath().findNodes(current, relativeXpath, arguments);
        List<Element> els = new ArrayList<Element>(nodes.length);
        for (Node node : nodes) {
            if (isElement(node)) {
                els.add((Element) node);
            }
        }
        final IteratorAdapter iterator = new IteratorAdapter(this, els.iterator());
        return new Iterable<XMLTag>() {
            public Iterator<XMLTag> iterator() {
                return iterator;
            }
        };
    }

    public List<Element> getChildElement() {
        return childs(current);
    }

    public String getCurrentTagName() {
        return current.getTagName();
    }

    public String getCurrentTagLocation() {
        StringBuilder sb = new StringBuilder();
        Element thisNode = current;
        while (thisNode != definition.getRoot()) {
            Element parent = (Element) thisNode.getParentNode();
            List<Element> els = childs(parent);
            for (int i = 0; i < els.size(); i++) {
                if (els.get(i) == thisNode) {
                    sb.insert(0, "/*[" + (i + 1) + "]");
                    break;
                }
            }
            thisNode = parent;
        }
        return sb.length() == 0 ? "." : sb.deleteCharAt(0).toString();
    }

    public String getAttribute(String name) {
        notEmpty("Attribute name", name);
        if (!hasAttribute(name)) {
            throw new XMLDocumentException("Element '%s' does not have attribute '%s'", getCurrentTagName(), name);
        }
        return current.getAttribute(name);
    }

    public String findAttribute(String name) {
        notEmpty("Attribute name", name);
        return hasAttribute(name) ? current.getAttribute(name) : null;
    }

    public String getAttribute(String name, String relativeXpath, Object... arguments) {
        Element old = current;
        try {
            return gotoTag(relativeXpath, arguments).getAttribute(name);
        }
        finally {
            current = old;
        }
    }

    public String findAttribute(String name, String relativeXpath, Object... arguments) throws XMLDocumentException {
        Element old = current;
        try {
            return gotoTag(relativeXpath, arguments).findAttribute(name);
        }
        finally {
            current = old;
        }
    }

    @SuppressWarnings({"MismatchedReadAndWriteOfArray"})
    public String[] getAttributeNames() {
        List<Attr> attrs = attr(current);
        String[] names = new String[attrs.size()];
        for (int i = 0; i < attrs.size(); i++) {
            names[i] = (attrs.get(i)).getName();
        }
        return names;
    }

    public String getText(String relativeXpath, Object... arguments) {
        Element old = current;
        try {
            return gotoTag(relativeXpath, arguments).getText();
        }
        finally {
            current = old;
        }
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        List<Node> nodes = childs(Document.TEXT_NODE);
        for (Node node : nodes) {
            String val = node.getNodeValue();
            if (val != null) {
                sb.append(val);
            }
        }
        return sb.toString();
    }

    public String getTextOrCDATA() {
        String txt = getText();
        return "".equals(txt) ? getCDATA() : txt;

    }

    public String getTextOrCDATA(String relativeXpath, Object... arguments) throws XMLDocumentException {
        Element old = current;
        try {
            return gotoTag(relativeXpath, arguments).getTextOrCDATA();
        }
        finally {
            current = old;
        }
    }

    public String getCDATAorText() {
        String txt = getCDATA();
        return "".equals(txt) ? getText() : txt;
    }

    public String getCDATAorText(String relativeXpath, Object... arguments) throws XMLDocumentException {
        Element old = current;
        try {
            return gotoTag(relativeXpath, arguments).getCDATAorText();
        }
        finally {
            current = old;
        }
    }

    public String getCDATA(String relativeXpath, Object... arguments) {
        Element old = current;
        try {
            return gotoTag(relativeXpath, arguments).getCDATA();
        }
        finally {
            current = old;
        }
    }

    public String getCDATA() {
        StringBuilder sb = new StringBuilder();
        List<Node> nodes = childs(Document.CDATA_SECTION_NODE);
        for (Node node : nodes) {
            String val = ((CDATASection) node).getData();
            if (val != null) {
                sb.append(val);
            }
        }
        return sb.toString();
    }

    public Document toDocument() {
        return definition.getDocument();
    }

    public Source toSource() {
        return new DOMSource(toDocument());
    }

    @Override
    public String toString() {
        return toString(definition.getEncoding());
    }

    public String toString(String encoding) {
        StringWriter out = new StringWriter();
        toStream(out, encoding);
        return out.toString();
    }

    public byte[] toBytes() {
        return toBytes(definition.getEncoding());
    }

    public byte[] toBytes(String encoding) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toStream(out, encoding);
        return out.toByteArray();
    }

    public XMLTag toStream(OutputStream out) {
        return toStream(out, definition.getEncoding());
    }

    public XMLTag toStream(OutputStream out, String encoding) {
        notEmpty("encoding", encoding);
        try {
            return toStream(new BufferedWriter(new OutputStreamWriter(out, encoding)));
        } catch (UnsupportedEncodingException e) {
            throw new XMLDocumentException(e.getMessage(), e);
        }
    }

    public XMLTag toStream(Writer out) {
        return toStream(out, definition.getEncoding());
    }

    public XMLTag toStream(Writer out, String encoding) {
        notEmpty("encoding", encoding);
        return toResult(new StreamResult(out), encoding);
    }

    public Result toResult() {
        Result r = new DOMResult();
        toResult(r);
        return r;
    }

    public Result toResult(String encoding) {
        Result r = new DOMResult();
        toResult(r, encoding);
        return r;
    }

    public OutputStream toOutputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toStream(baos);
        return baos;
    }

    public OutputStream toOutputStream(String encoding) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        toStream(baos, encoding);
        return baos;
    }

    public Writer toWriter() {
        Writer w = new StringWriter();
        toStream(w);
        return w;
    }

    public Writer toWriter(String encoding) {
        Writer w = new StringWriter();
        toStream(w, encoding);
        return w;
    }

    public XMLTag toResult(Result out) {
        return toResult(out, definition.getEncoding());
    }

    public XMLTag toResult(Result out, String encoding) {
        notEmpty("encoding", encoding);
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                tf.setAttribute("indent-number", 4);
            } catch (Exception ignored) {
            }
            Transformer transformer = tf.newTransformer();
            transformer.setParameter("indent-number", 4);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, definition.getDocument().getXmlStandalone() ? "yes" : "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(toSource(), out);
            return this;
        }
        catch (TransformerException e) {
            throw new XMLDocumentException("Transformation error", e);
        }
    }

    public ValidationResult validate(Source... schemas) {
        notNull("schemas", schemas);
        try {
            return Utils.validate(this.toDocument(), schemas);
        }
        catch (Exception e) {
            throw new XMLDocumentException("Validation failed", e);
        }
    }

    public ValidationResult validate(URL... schemaLocations) {
        notNull("schemaLocations", schemaLocations);
        try {
            return Utils.validate(this.toDocument(), schemaLocations);
        }
        catch (Exception e) {
            throw new XMLDocumentException("Validation failed", e);
        }
    }

    private boolean isElement(Node n) {
        return n != null && n.getNodeType() == Document.ELEMENT_NODE;
    }

    private List<Element> childs(Element e) {
        NodeList list = e.getChildNodes();
        List<Element> els = new ArrayList<Element>(list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            if (isElement(list.item(i))) {
                els.add((Element) list.item(i));
            }
        }
        return els;
    }

    private List<Attr> attr(Element e) {
        NamedNodeMap list = e.getAttributes();
        List<Attr> attrs = new ArrayList<Attr>(list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            attrs.add((Attr) list.item(i));
        }
        return attrs;
    }

    private List<Node> childs(short type) {
        NodeList childs = current.getChildNodes();
        List<Node> nodes = new ArrayList<Node>(childs.getLength());
        for (int i = 0; i < childs.getLength(); i++) {
            if (childs.item(i).getNodeType() == type) {
                nodes.add(childs.item(i));
            }
        }
        return nodes;
    }

    public XMLTag duplicate() {
        return XMLDoc.from(this, definition.isIgnoreNamespaces())
                .gotoRoot()
                .gotoTag(getCurrentTagLocation());
    }

    public XMLTag setText(String text) {
        for (Node node : childs(Document.CDATA_SECTION_NODE)) {
            current.removeChild(node);
        }
        for (Node node : childs(Document.TEXT_NODE)) {
            current.removeChild(node);
        }
        return addText(text);
    }

    public XMLTag setText(String text, String relativeXpath, Object... arguments) throws XMLDocumentException {
        Element old = current;
        try {
            gotoTag(relativeXpath, arguments).setText(text);
        }
        finally {
            current = old;
        }
        return this;
    }

    public XMLTag setTextIfExist(String text, String relativeXpath, Object... arguments) throws XMLDocumentException {
        return hasTag(relativeXpath, arguments) ? setText(text, relativeXpath, arguments) : this;
    }

    public XMLTag setCDATA(String data) {
        for (Node node : childs(Document.CDATA_SECTION_NODE)) {
            current.removeChild(node);
        }
        for (Node node : childs(Document.TEXT_NODE)) {
            current.removeChild(node);
        }
        return addCDATA(data);
    }

    public XMLTag setCDATA(String data, String relativeXpath, Object... arguments) throws XMLDocumentException {
        Element old = current;
        try {
            gotoTag(relativeXpath, arguments).setCDATA(data);
        }
        finally {
            current = old;
        }
        return this;
    }

    public XMLTag setCDATAIfExist(String data, String relativeXpath, Object... arguments) throws XMLDocumentException {
        return hasTag(relativeXpath, arguments) ? setCDATA(data, relativeXpath, arguments) : this;
    }

    public XMLTag setAttribute(String name, String value) throws XMLDocumentException {
        notNull("Attribute name", name);
        notNull("Attribute value", value);
        if (!hasAttribute(name)) {
            throw new XMLDocumentException("Element '%s' does not have attribute '%s'", getCurrentTagName(), name);
        }
        current.getAttributeNode(name).setValue(value);
        return this;
    }

    public XMLTag setAttributeIfExist(String name, String value) {
        notNull("Attribute name", name);
        notNull("Attribute value", value);
        if (hasAttribute(name)) {
            current.getAttributeNode(name).setValue(value);
        }
        return this;
    }

    public XMLTag setAttribute(String name, String value, String relativeXpath, Object... arguments) throws XMLDocumentException {
        Element old = current;
        try {
            gotoTag(relativeXpath, arguments).setAttribute(name, value);
        }
        finally {
            current = old;
        }
        return this;
    }

    public XMLTag setAttributeIfExist(String name, String value, String relativeXpath, Object... arguments) throws XMLDocumentException {
        return hasAttribute(name, relativeXpath, arguments) ? setAttribute(name, value, relativeXpath, arguments) : this;
    }

    public static XMLDocBuilder newDocument(boolean ignoreNamespaces) {
        return XMLDocBuilder.newDocument(ignoreNamespaces);
    }

    public static XMLTag from(Node node, boolean ignoreNamespaces) {
        notNull("Node", node);
        return XMLDocBuilder.from(node, ignoreNamespaces);
    }

    public static XMLTag from(InputSource source, boolean ignoreNamespaces) {
        notNull("InputSource", source);
        return XMLDocBuilder.from(source, ignoreNamespaces);
    }

    public static XMLTag from(Reader reader, boolean ignoreNamespaces) {
        notNull("Reader", reader);
        return XMLDocBuilder.from(reader, ignoreNamespaces);
    }

    public static XMLTag from(Reader reader, boolean ignoreNamespaces, String encoding) {
        notNull("Reader", reader);
        return XMLDocBuilder.from(reader, ignoreNamespaces, encoding);
    }

    public static XMLTag from(InputStream is, boolean ignoreNamespaces) {
        notNull("InputStream", is);
        return XMLDocBuilder.from(is, ignoreNamespaces);
    }

    public static XMLTag from(InputStream is, boolean ignoreNamespaces, String encoding) {
        notNull("InputStream", is);
        return XMLDocBuilder.from(is, ignoreNamespaces, encoding);
    }

    public static XMLTag from(File file, boolean ignoreNamespaces) {
        notNull("File", file);
        return XMLDocBuilder.from(file, ignoreNamespaces);
    }

    public static XMLTag from(File file, boolean ignoreNamespaces, String encoding) {
        notNull("File", file);
        return XMLDocBuilder.from(file, ignoreNamespaces, encoding);
    }

    public static XMLTag from(URL xmlLocation, boolean ignoreNamespaces) {
        notNull("URL", xmlLocation);
        return XMLDocBuilder.from(xmlLocation, ignoreNamespaces);
    }

    public static XMLTag from(URL xmlLocation, boolean ignoreNamespaces, String encoding) {
        notNull("URL", xmlLocation);
        return XMLDocBuilder.from(xmlLocation, ignoreNamespaces, encoding);
    }

    public static XMLTag from(String xmlData, boolean ignoreNamespaces) {
        notEmpty("XML Data", xmlData);
        return XMLDocBuilder.from(xmlData, ignoreNamespaces);
    }

    public static XMLTag from(String xmlData, boolean ignoreNamespaces, String encoding) {
        notEmpty("XML Data", xmlData);
        return XMLDocBuilder.from(xmlData, ignoreNamespaces, encoding);
    }

    public static XMLTag from(Source source, boolean ignoreNamespaces) {
        notNull("Source", source);
        return XMLDocBuilder.from(source, ignoreNamespaces);
    }

    public static XMLTag from(Source source, boolean ignoreNamespaces, String encoding) {
        notNull("Source", source);
        return XMLDocBuilder.from(source, ignoreNamespaces, encoding);
    }

    public static XMLTag from(XMLTag tag, boolean ignoreNamespaces) {
        notNull("XML Tag", tag);
        return XMLDocBuilder.from(tag, ignoreNamespaces);
    }

    public static XMLDocBuilder newDocument() {
        return newDocument(true);
    }

    public static XMLTag from(Node node) {
        return from(node, true);
    }

    public static XMLTag from(InputSource source) {
        return from(source, true);
    }

    public static XMLTag from(Reader reader) {
        return from(reader, true);
    }

    public static XMLTag from(InputStream is) {
        return from(is, true);
    }

    public static XMLTag from(File file) {
        return from(file, true);
    }

    public static XMLTag from(URL xmlLocation) {
        return from(xmlLocation, true);
    }

    public static XMLTag from(String xmlData) {
        return from(xmlData, true);
    }

    public static XMLTag from(Source source) {
        return from(source, true);
    }

    public static XMLTag from(XMLTag tag) {
        return from(tag, true);
    }

    /**
     * Create another {@link com.mycila.xmltool.XMLTag} instance from the hierarchy under the current tag. The current tag becomes the root tag.
     *
     * @param tag              The current XML Tag positionned to the new root tag
     * @param ignoreNamespaces Wheter to build a namespace aware document
     * @return The inner XMLTag instance
     */
    public static XMLTag fromCurrentTag(XMLTag tag, boolean ignoreNamespaces) {
        notNull("XML Tag", tag);
        return XMLDocBuilder.fromCurrentTag(tag, ignoreNamespaces);
    }
}
