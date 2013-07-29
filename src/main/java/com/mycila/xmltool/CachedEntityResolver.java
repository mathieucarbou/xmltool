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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class CachedEntityResolver implements EntityResolver, EntityResolver2 {

    static final CachedEntityResolver instance = new CachedEntityResolver();

    private Map<String, String> cache = new LinkedHashMap<String, String>();

    private CachedEntityResolver() {
    }

    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return new InputSource(new StringReader(""));
    }

    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        return resolveEntity(publicId, systemId);
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String content = cache.get(systemId);
        if (content == null) {
            content = read(systemId);
            cache.put(systemId, content);
        }
        return new InputSource(new StringReader(content));
    }

    private String read(String url) {
        try {
            URL u = new URL(url);
            URLConnection con = u.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            con.setDefaultUseCaches(true);
            con.setUseCaches(true);
            con.connect();
            StringWriter sw = new StringWriter();
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(con.getInputStream()), con.getContentEncoding() != null ? con.getContentEncoding() : "UTF-8"));
            char[] buffer = new char[8192];
            int c;
            while ((c = br.read(buffer)) != -1) {
                sw.write(buffer, 0, c);
            }
            return sw.toString();
        } catch (IOException e) {
            return "";
        }
    }
}