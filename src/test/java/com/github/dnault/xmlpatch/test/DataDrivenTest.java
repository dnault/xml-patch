/*
 * Copyright 2015 David Nault and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnault.xmlpatch.test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import junit.framework.*;

import com.github.dnault.xmlpatch.ErrorCondition;
import com.github.dnault.xmlpatch.XmlHelper;

import org.jdom.*;
import org.jdom.filter.Filter;

public class DataDrivenTest extends TestCase {

    private final Document target = new Document();
    private final Document diff = new Document();
    private final Document expectedResult = new Document();
    private final ErrorCondition expectedError;
    
    private DataDrivenTest(Element e) {
        super(e.getAttributeValue("desc"));

        Filter prologFilter = new Filter() {
            public boolean matches(Object o) {
                return o instanceof Element || o instanceof Comment
                        || o instanceof ProcessingInstruction;
            }
        };
        target.addContent(XmlHelper.clone(e.getChild("target").getContent(prologFilter)));
        diff.setRootElement((Element) e.getChild("diff").clone());
        expectedResult.addContent(XmlHelper.clone(e.getChild("result").getContent(prologFilter)));

        String errorName = e.getChild("result").getAttributeValue("error");
        expectedError = errorName == null ? null : ErrorCondition.valueOf(errorName);
    }

    public static Test suite() {
        String resourceName = "regression-test.xml";
        
        TestSuite suite = new TestSuite(resourceName);
        
        
        InputStream is = DataDrivenTest.class.getResourceAsStream(resourceName);
        try {
            Document d = XmlHelper.parse(is);
            Set<String> testNames = new HashSet<String>();

            for (Object o : d.getRootElement().getChildren()) {
                Element e = (Element) o;
                
                String name = e.getAttributeValue("desc");
                if (!testNames.add(name)) {
                    throw new RuntimeException("duplicate test name '" + name + "'");
                }
                
                suite.addTest(new DataDrivenTest(e));
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return suite;
    }

    @Override
    protected void runTest() throws Throwable {
        if (expectedError != null) {
            TestHelper.doPatchExpectError(target, diff, expectedError);
        } else {
            TestHelper.doPatch(target, diff, expectedResult);
        }
    }

}