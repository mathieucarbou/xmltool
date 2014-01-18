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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Map;

import static com.mycila.xmltool.Utils.notEmpty;
import static com.mycila.xmltool.Utils.notNull;
import static javax.xml.xpath.XPathConstants.BOOLEAN;
import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.NUMBER;
import static javax.xml.xpath.XPathConstants.STRING;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class XMLDocPath {

    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

    private final XPath xpath;
    private final Map<String, XPathExpression> compiled = new SoftHashMap<String, XPathExpression>();

    XMLDocPath(XMLDocDefinition context) {
        try {
            xpath = XPATH_FACTORY.newXPath();
            xpath.setNamespaceContext(context);
        }
        catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    String rawXpathString(Node root, String relativeXpath, Object... arguments) {
        return eval(getExpr(relativeXpath, arguments), root, STRING);
    }

    Number rawXpathNumber(Node root, String relativeXpath, Object... arguments) {
        return eval(getExpr(relativeXpath, arguments), root, NUMBER);
    }

    Boolean rawXpathBoolean(Node root, String relativeXpath, Object... arguments) {
        return eval(getExpr(relativeXpath, arguments), root, BOOLEAN);
    }

    Node rawXpathNode(Node root, String relativeXpath, Object... arguments) {
        return eval(getExpr(relativeXpath, arguments), root, NODE);
    }

    NodeList rawXpathNodeSet(Node root, String relativeXpath, Object... arguments) {
        return eval(getExpr(relativeXpath, arguments), root, NODESET);
    }

    Node[] findNodes(Node root, String xpathExpr, Object... args) {
        try {
            NodeList list = rawXpathNodeSet(root, xpathExpr, args);
            Node[] nodes = new Node[list.getLength()];
            for (int i = 0; i < list.getLength(); i++) {
                nodes[i] = list.item(i);
            }
            return nodes;
        } catch (Exception e) {
            return new Node[0];
        }
    }

    Node findNode(Node root, String xpathExpr, Object... args) {
        Node n = eval(getExpr(xpathExpr, args), root, NODE);
        if (n == null) {
            throw new XMLDocumentException(String.format("Error executing xpath '%s' from node '%s': Inexisting target node.", xpathExpr, root.getNodeName()));
        }
        return n;
    }

    @SuppressWarnings({"unchecked"})
    private <T> T eval(XPathExpression expr, Node root, QName retType) {
        notNull("Node", root);
        notNull("Return type", retType);
        try {
            return (T) expr.evaluate(root, retType);
        } catch (XPathExpressionException e) {
            throw new XMLDocumentException(String.format("Error executing xpath from node '%s': %s", root.getNodeName(), e.getMessage()), e);
        }
    }

    private XPathExpression getExpr(String xpathExpr, Object... args) {
        notEmpty("XPath expression", xpathExpr);
        try {
            xpathExpr = String.format(xpathExpr, args);
            XPathExpression expr = compiled.get(xpathExpr);
            if (expr == null) {
                expr = xpath.compile(xpathExpr);
                compiled.put(xpathExpr, expr);
            }
            return expr;
        } catch (Exception e) {
            throw new XMLDocumentException(String.format("Error compiling xpath '%s'", xpathExpr), e);
        }
    }
}
