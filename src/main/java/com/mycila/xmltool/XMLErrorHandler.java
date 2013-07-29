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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class XMLErrorHandler implements ErrorHandler, ValidationResult, ErrorListener {

    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final List<Throwable> warnings = new ArrayList<Throwable>();
    private final boolean throwExceptionOnError;

    XMLErrorHandler() {
        this(false);
    }

    XMLErrorHandler(boolean throwExceptionOnError) {
        this.throwExceptionOnError = throwExceptionOnError;
    }

    private void logError(Throwable cause) {
        errors.add(cause);
    }

    private void logWarning(Throwable cause) {
        warnings.add(cause);
    }

    public void error(TransformerException exception) throws TransformerException {
        logError(exception);
        if (throwExceptionOnError) {
            throw exception;
        }
    }

    public void fatalError(TransformerException exception) throws TransformerException {
        error(exception);
    }

    public void warning(TransformerException exception) throws TransformerException {
        logWarning(exception);
    }

    public void warning(SAXParseException exception) throws SAXException {
        logWarning(exception);
    }

    public void error(SAXParseException exception) throws SAXException {
        logError(exception);
        if (throwExceptionOnError) {
            throw exception;
        }
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        error(exception);
    }

    public Throwable[] getErrors() {
        return errors.toArray(new Throwable[errors.size()]);
    }

    public Throwable[] getWarnings() {
        return warnings.toArray(new Throwable[warnings.size()]);
    }

    public String[] getErrorMessages() {
        return messages(errors);
    }

    public String[] getWarningMessages() {
        return messages(warnings);
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    public boolean hasWarning() {
        return !warnings.isEmpty();
    }

    private String[] messages(List<Throwable> exceptions) {
        String[] msg = new String[exceptions.size()];
        int i = 0;
        for (Throwable exception : exceptions) {
            msg[i++] = Utils.getMessage(exception);
        }
        return msg;
    }
}
