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

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.util.concurrent.TimeoutException;

public class XMLDocumentBuilderFactory {

    private static ObjectPool<DocumentBuilder> ignoreNamespaceDocumentBuilderPool;
    private static ObjectPool<DocumentBuilder> namespaceAwareDocumentBuilderPool;

    static {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMinIdle(0);
        config.setMaxIdle(Runtime.getRuntime().availableProcessors());
        config.setMaxTotal(Runtime.getRuntime().availableProcessors() * 4);
        config.setMaxWaitMillis(-1);

        setPoolConfig(config);
    }

    public static void setPoolConfig(GenericObjectPoolConfig config) {
        ignoreNamespaceDocumentBuilderPool = new GenericObjectPool<DocumentBuilder>(new BasePooledObjectFactory<DocumentBuilder>() {
            @Override
            public DocumentBuilder create() throws Exception {
                return newDocumentBuilder(true);
            }

            @Override
            public PooledObject<DocumentBuilder> wrap(DocumentBuilder obj) {
                return new DefaultPooledObject<DocumentBuilder>(obj);
            }
        }, config);
        namespaceAwareDocumentBuilderPool = new GenericObjectPool<DocumentBuilder>(new BasePooledObjectFactory<DocumentBuilder>() {
            @Override
            public DocumentBuilder create() throws Exception {
                return newDocumentBuilder(false);
            }

            @Override
            public PooledObject<DocumentBuilder> wrap(DocumentBuilder obj) {
                return new DefaultPooledObject<DocumentBuilder>(obj);
            }
        }, config);
    }

    public static <V> V withDocumentBuilder(boolean ignoreNamespaces, final Callback<V> c) {
        try {
            if (ignoreNamespaces) {
                DocumentBuilder documentBuilder = null;
                try {
                    documentBuilder = ignoreNamespaceDocumentBuilderPool.borrowObject();
                    return c.apply(documentBuilder);
                } finally {
                    if (documentBuilder != null) {
                        ignoreNamespaceDocumentBuilderPool.returnObject(documentBuilder);
                    }
                }
            } else {
                DocumentBuilder documentBuilder = null;
                try {
                    documentBuilder = namespaceAwareDocumentBuilderPool.borrowObject();
                    return c.apply(documentBuilder);
                } finally {
                    if (documentBuilder != null) {
                        namespaceAwareDocumentBuilderPool.returnObject(documentBuilder);
                    }
                }
            }
        } catch (TimeoutException e) {
            throw new XMLDocumentException("Error creating XMLDoc: timed out.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new XMLDocumentException("Error creating XMLDoc: interrupted.", e);
        } catch (Exception e) {
            throw new XMLDocumentException("Error creating XMLDoc. Please verify that the input source can be read and is well formed.", e);
        }
    }

    interface Callback<V> {
        V apply(DocumentBuilder b) throws Exception;
    }

    public static DocumentBuilder newDocumentBuilder(boolean ignoreNamespaces) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(!ignoreNamespaces);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new XMLErrorHandler(true));
            builder.setEntityResolver(CachedEntityResolver.instance);
            return builder;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
