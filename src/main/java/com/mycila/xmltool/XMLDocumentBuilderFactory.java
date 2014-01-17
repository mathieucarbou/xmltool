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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

public class XMLDocumentBuilderFactory {

    private static final DocumentBuilder ignoreNamespaceDocumentBuilder = newDocumentBuilder(true);
    private static final DocumentBuilder namespaceAwareDocumentBuilder = newDocumentBuilder(false);

    public static DocumentBuilder newDocumentBuilder(boolean ignoreNamespaces) {
        if (ignoreNamespaces && ignoreNamespaceDocumentBuilder != null) {
            return ignoreNamespaceDocumentBuilder;
        }
        if (!ignoreNamespaces && namespaceAwareDocumentBuilder != null) {
            return namespaceAwareDocumentBuilder;
        }
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(!ignoreNamespaces);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new XMLErrorHandler(true));
            builder.setEntityResolver(CachedEntityResolver.instance);
            return builder;
        }
        catch (ParserConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
