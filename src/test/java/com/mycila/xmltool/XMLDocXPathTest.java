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

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocXPathTest {
    @Test
    public void test_xpath() throws Exception {
        XMLTag tag = XMLDoc.newDocument(true)
                .addRoot("html")
                .addTag("head").addText("one")
                .gotoChild("head")
                .addTag("title").addText("my title 1")
                .addTag("title").addText("my title 2")
                .addText("two")
                .addTag("body")
                .addTag("number").addText("1")
                .addTag("bool").addText("true")
                .gotoRoot();
        System.out.println(tag.toString());
        assertEquals(tag.rawXpathString("head/text()"), "one"); // should be onetwo according to the specs but Java returns only the first text node. See http://www.w3schools.com/Xpath/xpath_axes.asp
        assertEquals(tag.rawXpathBoolean("body/bool/text()"), Boolean.TRUE);
        assertEquals(tag.rawXpathNumber("body/number/text()"), 1.0);
        assertEquals(tag.rawXpathNode("body/number").getNodeName(), "number");
        assertEquals(tag.rawXpathNodeSet("//title").getLength(), 2);
    }
}
