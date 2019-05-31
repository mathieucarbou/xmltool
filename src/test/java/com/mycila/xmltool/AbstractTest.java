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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Locale;

import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class AbstractTest {

    private static final SoftHashMap<URL, byte[]> cache = new SoftHashMap<URL, byte[]>();
    private static final byte[] NULL = new byte[0];

    @BeforeClass
    public static void setLocaleToEN_US() {
      // needed to make tests pass on non-EN/US systems due to localized exception messsages
      Locale.setDefault(Locale.US);
    }
    
    protected void assertSameDoc(final String actual, final String expected) {
        Assert.assertEquals(actual.replaceAll("\\r|\\n", "").replaceAll(">\\s*<", "><"), expected.replaceAll("\\r|\\n", "").replaceAll(">\\s*<", "><"));
    }

    public static URL resource(String classPath) {
        URL u = Thread.currentThread().getContextClassLoader().getResource(classPath.startsWith("/") ? classPath.substring(1) : classPath);
        if (u == null) {
            throw new IllegalArgumentException("Resource not found in classpath: " + classPath);
        }
        return u;
    }

    public static String readString(String classPath) {
        return readString(classPath, "UTF-8");
    }

    public static String readString(String classPath, String encoding) {
        try {
            return new String(readByte(classPath), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static byte[] readByte(String classPath) {
        return readByte(resource(classPath));
    }

    public static byte[] readByte(URL url) {
        byte[] data = cache.get(url);
        if (data == null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedInputStream bis = new BufferedInputStream(url.openStream());
                data = new byte[8192];
                int count;
                while ((count = bis.read(data)) != -1) {
                    baos.write(data, 0, count);
                }
                bis.close();
                data = baos.toByteArray();
                cache.put(url, data);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return data == NULL ? null : data;
    }
}
