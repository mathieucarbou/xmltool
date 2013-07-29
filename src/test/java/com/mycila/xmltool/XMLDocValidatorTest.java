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

import java.util.Arrays;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class XMLDocValidatorTest extends AbstractTest {

    @Test
    public void validate_URL() throws Exception {
        XMLTag doc = XMLDoc.from(readString("doc2.xhtml"), false);
        ValidationResult res = Utils.validate(doc.toDocument(), getClass().getResource("/doc.xsd"));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
    }

    @Test
    public void validate_URL_invalid() throws Exception {
        XMLTag doc = XMLDoc.from(readString("doc3.xhtml"), false);
        ValidationResult res = Utils.validate(doc.toDocument(), getClass().getResource("/doc.xsd"));
        assertTrue(res.hasError());
        assertFalse(res.hasWarning());
        System.out.println(Arrays.deepToString(res.getErrorMessages()));
    }

    @Test(enabled = false) //when we ignore namespace, validation becomes unpredictable on diffrent jdk versions
    public void validate_URL_ignoringNS() throws Exception {
        XMLTag doc = XMLDoc.from(readString("doc2.xhtml"), true);
        ValidationResult res = Utils.validate(doc.toDocument(), getClass().getResource("/doc.xsd"));
        System.out.println(Arrays.deepToString(res.getErrorMessages()));
        assertFalse(res.hasError());
        assertFalse(res.hasWarning());
    }

    @Test(enabled = false) //when we ignore namespace, validation becomes unpredictable on diffrent jdk versions
    public void validate_URL_invalid_ignoringNS() throws Exception {
        XMLTag doc = XMLDoc.from(readString("doc3.xhtml"), true);
        ValidationResult res = Utils.validate(doc.toDocument(), getClass().getResource("/doc.xsd"));
        assertTrue(res.hasError());
        assertFalse(res.hasWarning());
        System.out.println(Arrays.deepToString(res.getErrorMessages()));
    }
}
