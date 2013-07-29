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

import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class IteratorAdapter implements Iterator<XMLTag> {

    final Iterator<Element> iterator;
    final XMLDoc xmlDoc;
    final Element old;

    IteratorAdapter(XMLDoc xmlDoc, Iterator<Element> iterator) {
        this.xmlDoc = xmlDoc;
        this.old = xmlDoc.current;
        this.iterator = iterator;
    }

    public boolean hasNext() {
        boolean hasNext = iterator.hasNext();
        if(hasNext) {
            xmlDoc.current = iterator.next();
        } else {
            xmlDoc.current = old;
        }
        return hasNext;
    }

    public XMLTag next() {
        return xmlDoc;
    }

    public void remove() {
        iterator.remove();
    }
}