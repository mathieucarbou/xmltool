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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.mycila.xmltool.Utils.notEmpty;
import static com.mycila.xmltool.Utils.notNull;
import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class XMLDocDefinition implements NamespaceContext {

    private Element root;
    private final Document document;
    private final XMLDocPath xpath;
    private final Map<String, String> namespaces = new HashMap<String, String>();
    private final boolean ignoreNamespaces;

    XMLDocDefinition(Node node, boolean ignoreNamespaces) {
        this.ignoreNamespaces = ignoreNamespaces;
        this.document = node instanceof Document ? (Document) node : node.getOwnerDocument();
        this.root = this.document.getDocumentElement();
        xpath = new XMLDocPath(this);
        resetNamespaces();
        readNamespaces();
    }

    XMLDocDefinition addNamespace(String prefix, String namespaceURI) throws XMLDocumentException {
        if (!ignoreNamespaces) {
            notNull("prefix", prefix);
            notNull("namespaceURI", namespaceURI);
            String existing = namespaces.get(prefix);
            if (existing != null && !existing.equals(namespaceURI)) {
                throw new XMLDocumentException("Prefix '%s' is already bound to another namespace '%s'", prefix, namespaces.get(prefix));
            }
            if (!namespaces.values().contains(namespaceURI)) {
                namespaces.put(prefix, namespaceURI);
            }
        }
        return this;
    }

    XMLDocDefinition addDefaultNamespace(String defaultNamespaceURI) {
        if (!ignoreNamespaces) {
            notNull("defaultNamespaceURI", defaultNamespaceURI);
            Iterator<String> i = getPrefixes(defaultNamespaceURI);
            while (i.hasNext()) {
                namespaces.remove(i.next());
            }
            namespaces.put(DEFAULT_NS_PREFIX, defaultNamespaceURI);
            namespaces.put(generatePrefix(), defaultNamespaceURI);
        }
        return this;
    }

    String getEncoding() {
        String enc = document.getXmlEncoding();
        return enc == null ? "UTF-8" : enc;
    }

    Document getDocument() {
        return document;
    }

    Element getRoot() {
        return root;
    }

    XMLDocPath getXpath() {
        return xpath;
    }

    Element createElement(String tagName) {
        notEmpty("tag name", tagName);
        if (ignoreNamespaces && tagName.contains(":")) {
            tagName = tagName.substring(tagName.indexOf(":") + 1);
        }
        return document.createElementNS(getNamespace(tagName), tagName);
    }

    Attr createAttribute(Element current, String name, String value) {
        notEmpty("Attribute name", name);
        if (ignoreNamespaces && name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        Attr attr = document.createAttributeNS(getNamespace(name), name);
        attr.setValue(value);
        current.setAttributeNodeNS(attr);
        return attr;
    }

    Text createText(String text) {
        notNull("Text", text);
        return document.createTextNode(text);
    }

    CDATASection createCDATA(String data) {
        notNull("Data", data);
        return document.createCDATASection(data);
    }

    XMLDocDefinition createRoot(String tagName) {
        root = createElement(tagName);
        document.appendChild(root);
        return this;
    }

    Element rename(Element node, String newNodeName) {
        return rename(node, newNodeName, getNamespace(newNodeName));
    }

    Element renameWithoutNS(Element node, String newNodeName) {
        return rename(node, newNodeName, null);
    }

    Element rename(Element node, String newNodeName, String ns) {
        Element el = (Element) getDocument().renameNode(node, ns, newNodeName);
        if (root.equals(node)) {
            root = el;
        }
        return el;
    }

    public Attr rename(Attr attr, String newTagName) {
        return (Attr) getDocument().renameNode(attr, getNamespace(newTagName), newTagName);
    }

    public Attr renameWithoutNS(Attr attr, String newTagName) {
        return (Attr) getDocument().renameNode(attr, null, newTagName);
    }

    XMLDocDefinition normalize() {
        document.normalizeDocument();
        return this;
    }

    void resetNamespaces() {
        namespaces.clear();
        namespaces.put(XML_NS_PREFIX, XML_NS_URI);
        namespaces.put(XMLNS_ATTRIBUTE, XMLNS_ATTRIBUTE_NS_URI);
        namespaces.put(DEFAULT_NS_PREFIX, NULL_NS_URI);
    }

    private String getNamespace(String tagName) {
        int pos = tagName.indexOf(":");
        return getNamespaceURI(pos == -1 ? DEFAULT_NS_PREFIX : tagName.substring(0, pos));
    }

    void readNamespaces() {
        if (!ignoreNamespaces) {
            Set<String> defNs = new LinkedHashSet<String>();
            for (Node node : xpath.findNodes(root, "//*")) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    if (XMLNS_ATTRIBUTE.equals(attr.getNodeName())) {
                        defNs.add(attr.getNodeValue());
                    } else if (XMLNS_ATTRIBUTE.equals(attr.getPrefix())) {
                        addNamespace(attr.getNodeName().substring(6), attr.getNodeValue());
                    }
                }
            }
            for (String ns : defNs) {
                if (defaultNamespaceDefined()) {
                    addNamespace(generatePrefix(), ns);
                } else {
                    addDefaultNamespace(ns);
                }
            }
        }
    }

    private boolean defaultNamespaceDefined() {
        return !NULL_NS_URI.equals(getDefaultNamespace());
    }

    private String getDefaultNamespace() {
        return namespaces.get(DEFAULT_NS_PREFIX);
    }

    private String generatePrefix() {
        String prefix = "ns0";
        int i = 1;
        while (namespaces.keySet().contains(prefix)) {
            prefix = "ns" + i++;
        }
        return prefix;
    }

    public boolean isIgnoreNamespaces() {
        return ignoreNamespaces;
    }

    // Implementation methods. Please read the spec of each methods befores modifying them !

    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null");
        }
        String namespaceURI = namespaces.get(prefix);
        return namespaceURI == null ? NULL_NS_URI : namespaceURI;
    }

    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("namespaceURI cannot be null");
        } else {
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (!DEFAULT_NS_PREFIX.equals(entry.getKey()) && entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public Iterator<String> getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("namespaceURI cannot be null");
        }
        Set<String> prefixes = new HashSet<String>();
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            if (entry.getValue().equals(namespaceURI)) {
                prefixes.add(entry.getKey());
            }
        }
        return prefixes.iterator();
    }

}
