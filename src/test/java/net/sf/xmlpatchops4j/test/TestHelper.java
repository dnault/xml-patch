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

package net.sf.xmlpatchops4j.test;

import static org.junit.Assert.*;

import java.io.*;

import net.sf.xmlpatchops4j.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.junit.Assert;

public class TestHelper {

	public static final String EOL = "\n";
	public static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + EOL;

	public static Document parse(String xml) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new StringReader(xml));
		return doc;
	}

	public static Document parseClasspathResource(String name) {
	    InputStream is = TestHelper.class.getResourceAsStream(name);
        try {
            return XmlHelper.parse(is);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	public static String toString(Document d) throws IOException {
	    XMLOutputter outputter = new XMLOutputter();
        StringWriter w = new StringWriter();
        outputter.output(d, w);
        return w.toString();
	}

	public static String toString(Element e) throws IOException {
        XMLOutputter outputter = new XMLOutputter();
        StringWriter w = new StringWriter();
        outputter.output(e, w);
        return w.toString();
    }

	public static void assertXmlEquals(Document expected, Document actual) throws IOException {
		Assert.assertEquals(toString(expected), toString(actual));
	}

	public static void doPatch(Document target, Document diff, Document expectedResult) throws Exception {
	    doPatch(toString(target),toString(diff),toString(expectedResult));
    }

	public static void doPatchExpectError(Document target, Document diff, ErrorCondition expectedError) throws Exception {
        doPatchExpectError(toString(target), toString(diff), expectedError);
    }

    public static void doPatch(String target, String diff, String expectedResult) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Patcher.patch(asStream(target), asStream(diff), os);

		Document expected = XmlHelper.parse(asStream(expectedResult));
		Document actual = XmlHelper.parse(new ByteArrayInputStream(os.toByteArray()));
		assertXmlEquals(expected, actual);
	}

	public static void doPatchExpectError(String target, String diff, ErrorCondition expectedError) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Patcher.patch(asStream(target), asStream(diff), os);
            fail("expected error condition: " + expectedError.name() +
                    "\nbut got:\n" + os.toString("UTF-8"));
        } catch (PatchException e) {
            if (!expectedError.equals(e.getErrorCondition())) {
                e.printStackTrace();
                assertEquals(expectedError, e.getErrorCondition());
            }
        }
    }

	private static InputStream asStream(String s) {
		try {
			return new ByteArrayInputStream(s.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String makeDiff(String s) {
		return DECLARATION + "<diff>" + s + "</diff>";
	}
	public static String makeDiffWithNamespace(String s, String namespace) {
		return DECLARATION + "<diff " + namespace + ">" + s + "</diff>";
	}


}
