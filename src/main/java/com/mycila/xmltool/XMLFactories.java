package com.mycila.xmltool;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class XMLFactories {
    private static ObjectPool<TransformerFactory> transformerFactoryPool;
    private static ObjectPool<XPathFactory> xpathFactoryPool;

    static {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMinIdle(0);
        config.setMaxIdle(Runtime.getRuntime().availableProcessors());
        config.setMaxTotal(Runtime.getRuntime().availableProcessors() * 4);
        config.setMaxWaitMillis(-1);

        setPoolConfig(config);
    }

    public static void setPoolConfig(GenericObjectPoolConfig config) {
        transformerFactoryPool = new GenericObjectPool<TransformerFactory>(
            new BasePooledObjectFactory<TransformerFactory>() {
                @Override
                public TransformerFactory create() throws Exception {
                    return TransformerFactory.newInstance();
                }

                @Override
                public PooledObject<TransformerFactory> wrap(TransformerFactory obj) {
                    return new DefaultPooledObject<TransformerFactory>(obj);
                }
            }, config);
        xpathFactoryPool = new GenericObjectPool<XPathFactory>(new BasePooledObjectFactory<XPathFactory>() {
            @Override
            public XPathFactory create() throws Exception {
                return XPathFactory.newInstance();
            }

            @Override
            public PooledObject<XPathFactory> wrap(XPathFactory obj) {
                return new DefaultPooledObject<XPathFactory>(obj);
            }
        }, config);
    }

    public static Transformer createTransformer() throws TransformerConfigurationException {
        try {
            TransformerFactory factory = transformerFactoryPool.borrowObject();
            try {
                return factory.newTransformer();
            } finally {
                transformerFactoryPool.returnObject(factory);
            }
        } catch (Exception e) {
            throw new TransformerConfigurationException("Failed to borrow transformer factory", e);
        }
    }

    public static XPath createXPath() {
        try {
            XPathFactory factory = xpathFactoryPool.borrowObject();
            try {
                return factory.newXPath();
            } finally {
                xpathFactoryPool.returnObject(factory);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to borrow XPath factory", e);
        }
    }
}
