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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class Utils {

    private Utils() {}

    static String getMessage(Throwable cause) {
        String message = null;
        while (cause != null && (message == null || message.length() == 0)) {
            message = cause.getMessage();
            cause = cause.getCause();
        }
        message = message == null ? "no description available" : message;
        if(message.startsWith("javax.xml.transform.TransformerException: ")) {
            return message.substring(42);
        }
        return message;
    }

    static void notEmpty(String name, String value) {
        notNull(name, value);
        if (value.length() == 0) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
    }

    static void notNull(String name, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    static ValidationResult validate(Document doc, Source... schemas) throws IOException, SAXException {
        XMLErrorHandler errorHandler = new XMLErrorHandler();
        Validator validator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemas).newValidator();
        try {
            validator.setErrorHandler(errorHandler);
            validator.validate(new DOMSource(doc));
            return errorHandler;
        } finally {
            validator.reset();
        }
    }

    static ValidationResult validate(Document doc, URL... schemaLocations) throws URISyntaxException, IOException, SAXException {
        Source[] sources = new Source[schemaLocations.length];
        for (int i = 0; i < schemaLocations.length; i++) {
            sources[i] = new StreamSource(schemaLocations[i].toURI().toASCIIString());
        }
        return validate(doc, sources);
    }
}
