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
import com.mycila.xmltool.XMLDoc;
import com.mycila.xmltool.XMLTag;

import java.io.File;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class Sample {
    public static void main(String[] args) {
        XMLTag nons = XMLDoc.from(new File("big-sample.xml"), true).deletePrefixes();
        System.out.println(nons);
        System.out.println("DATE: " + nons.gotoChild("Type").gotoChild("Contract").gotoLastChild("ContractVersionSummary").gotoChild("LastUpdateTimestamp").getText());
        System.out.println("DATE: " + nons.gotoRoot().getText("Type/Contract/ContractVersionSummary[last()]/LastUpdateTimestamp"));
        System.out.println("UUID: " + nons.gotoRoot().getAttribute("UUID", "Vehicle[1]/Type"));

        XMLTag ns = XMLDoc.from(new File("big-sample.xml"), false).deletePrefixes();
        System.out.println(ns);
        System.out.println("DATE: " + ns.gotoChild("Type").gotoChild("Contract").gotoLastChild("ContractVersionSummary").gotoChild("LastUpdateTimestamp").getText());
        System.out.println("DATE: " + ns.gotoRoot().getText("Type/Contract/ContractVersionSummary[last()]/LastUpdateTimestamp"));
        System.out.println("UUID: " + nons.gotoRoot().getAttribute("UUID", "Vehicle[1]/Type"));
    }
}
