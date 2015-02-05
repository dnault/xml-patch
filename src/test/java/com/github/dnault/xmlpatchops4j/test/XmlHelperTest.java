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

package com.github.dnault.xmlpatchops4j.test;

import static com.github.dnault.xmlpatchops4j.XmlHelper.*;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;

public class XmlHelperTest extends TestCase {
	
	public void testGetInScopeNamespaceDeclarations() throws Exception {
		String xml = "<a xmlns:one='uri:one' xmlns:two='uri:two'>" + 
		"<b xmlns='uri:default'>" +
		"<two:c xmlns:two='uri:anothertwo'>" +
		"<d/></two:c></b></a>";
		
		Document doc = TestHelper.parse(xml);
		Element a = doc.getRootElement();
		Element b = getFirstChild(a);
		Element c = getFirstChild(b);
		Element d = getFirstChild(c);
	
		Map<String,String> prefixToURI = new HashMap<String,String>();
		prefixToURI.put("","");
		prefixToURI.put("one","uri:one");
		prefixToURI.put("two","uri:two");
		assertEquals(prefixToURI, getInScopeNamespaceDeclarations(a));

		prefixToURI.put("","uri:default");
		assertEquals(prefixToURI, getInScopeNamespaceDeclarations(b));
				
		prefixToURI.put("two","uri:anothertwo");
		assertEquals(prefixToURI, getInScopeNamespaceDeclarations(c));
		assertEquals(prefixToURI, getInScopeNamespaceDeclarations(d));
	}

	private Element getFirstChild(Element e) {
		return (Element) e.getChildren().get(0);
	}
	
}

