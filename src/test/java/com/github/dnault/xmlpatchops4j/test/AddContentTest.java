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

import static com.github.dnault.xmlpatchops4j.test.TestHelper.*;

import junit.framework.TestCase;

import com.github.dnault.xmlpatchops4j.ErrorCondition;

public class AddContentTest extends TestCase {

	private static final String COMMON_TARGET = DECLARATION + 
		"<doc>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"</doc>";  
	
	public void testAppendChildElement() throws Exception {		
		String newElement = "<foo id='ert4773'>This is a new child</foo>";		
		String diff = makeDiff("<add sel='doc'>" + newElement + "</add>");
		
		String expectedResult = DECLARATION + 
			"<doc>" + EOL + 
			"  <note>This is a sample document</note>" + EOL +
			newElement + "</doc>";
	
		doPatch(COMMON_TARGET, diff, expectedResult);
	}
	
	public void testPreppendChildElement() throws Exception {		
		String newElement = "<foo id='ert4773'>This is a new child</foo>";		
		String diff = makeDiff("<add sel='doc' pos='prepend'>" + newElement + "</add>");
		
		String expectedResult = DECLARATION + 
			"<doc>" + newElement + EOL + 
			"  <note>This is a sample document</note>" + EOL +
			"</doc>";
	
		doPatch(COMMON_TARGET, diff, expectedResult);
	}
	
	public void testAddBeforeElement() throws Exception {		
		String newElement = "<foo id='ert4773'>This is a new child</foo>";		
		String diff = makeDiff("<add sel='doc/note' pos='before'>" + newElement + "</add>");
		
		String expectedResult = DECLARATION + 
			"<doc>" + EOL + 
			"  " + newElement + "<note>This is a sample document</note>" + EOL +
			"</doc>";
	
		doPatch(COMMON_TARGET, diff, expectedResult);
	}
	
	public void testAddAfterElement() throws Exception {		
		String newElement = "<foo id='ert4773'>This is a new child</foo>";		
		String diff = makeDiff("<add sel='doc/note' pos='after'>" + newElement + "</add>");
		
		String expectedResult = DECLARATION + 
			"<doc>" + EOL + 
			"  <note>This is a sample document</note>" + newElement + EOL +
			"</doc>";
	
		doPatch(COMMON_TARGET, diff, expectedResult);
	}

	public void testAddCommentBeforeElement() throws Exception {		
		String newElement = "<!-- this is a comment -->";		
		String diff = makeDiff("<add sel='doc/note' pos='before'>" + newElement + "</add>");
		
		String expectedResult = DECLARATION + 
			"<doc>" + EOL + 
			"  " + newElement + "<note>This is a sample document</note>" + EOL +
			"</doc>";
	
		doPatch(COMMON_TARGET, diff, expectedResult);
	}
	
	public void testAddCommentBeforeRoot() throws Exception {		
		String newElement = "<!-- this is a comment -->";		
		String diff = makeDiff("<add sel='doc' pos='before'>" + newElement + "</add>");
		
		String expectedResult = DECLARATION + 
		    newElement + "<doc>" + EOL + 
			"  <note>This is a sample document</note>" + EOL +
			"</doc>";
	
		doPatch(COMMON_TARGET, diff, expectedResult);
	}
	
	public void testAddElementBeforeRoot() throws Exception {		
		String newElement = "<foo/>";		
		String diff = makeDiff("<add sel='doc' pos='before'>" + newElement + "</add>");
	
		doPatchExpectError(COMMON_TARGET, diff, ErrorCondition.INVALID_ROOT_ELEMENT_OPERATION);
	}
	
	public void testAddNamespaceQualifiedElement() throws Exception {
		String newElement = "<x:foo xmlns:x='http://example.com'/>";		

		String diff = makeDiff("<add sel='doc'>" + newElement + "</add>");
		
		String expectedResult = DECLARATION + 
	    "<doc>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		newElement + "</doc>";

	    doPatch(COMMON_TARGET, diff, expectedResult);
	}

	public void testAddNamespaceQualifiedElement2() throws Exception {
		String newElement = "<x:foo/>";				
		
		String diff = TestHelper.makeDiffWithNamespace("<add sel='doc'>" + newElement + "</add>", 
				"xmlns:x='http://example.com'");
		
		String expectedResult = DECLARATION + 
	    "<doc>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<x:foo xmlns:x='http://example.com'/></doc>";

	    doPatch(COMMON_TARGET, diff, expectedResult);
	}
	
	public void testAddNamespaceQualifiedElement3() throws Exception {
		
		String target = DECLARATION + 
		"<doc xmlns:x='http://example.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"</doc>";  
		
		String newElement = "<x:foo/>";				
		
		String diff = makeDiffWithNamespace("<add sel='doc'>" + newElement + "</add>", 
				"xmlns:x='http://example.com'");
		
		String expectedResult = DECLARATION + 
	    "<doc xmlns:x='http://example.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<x:foo/></doc>";

	    doPatch(target, diff, expectedResult);
	}

	public void testAddNamespaceQualifiedElement4() throws Exception {
		
		String target = DECLARATION + 
		"<doc xmlns:x='http://DIFFERENT.URL.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"</doc>";  
		
		String newElement = "<x:foo/>";				
		
		String diff = makeDiffWithNamespace("<add sel='doc'>" + newElement + "</add>", 
				"xmlns:x='http://example.com'");
		
		String expectedResult = DECLARATION + 
	    "<doc xmlns:x='http://DIFFERENT.URL.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<x:foo xmlns:x='http://example.com'/></doc>";

	    doPatch(target, diff, expectedResult);
	}

	public void testAddUnprefixedElementUnderDefaultPrefix() throws Exception {
		String target = DECLARATION + 
		"<doc xmlns='uri:default'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"</doc>";
		
		String newElement = "<foo><bar/></foo>";
		
		String diff = makeDiffWithNamespace("<add sel='*'>" + newElement + "</add>", 
		"xmlns:y='uri:default'");
		
				
		String expectedResult = DECLARATION + 
		"<doc xmlns='uri:default'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<foo xmlns=''><bar/></foo></doc>";
		
	    doPatch(target, diff, expectedResult);
		
	}
	
	
	public void testAddSameNamespaceDifferentPrefix() throws Exception {
		
		String target = DECLARATION + 
		"<doc xmlns:x='http://example.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"</doc>";  
		
		String newElement = "<y:foo color='blue' y:flavor='apricot'><y:bar/></y:foo>";				
		
		String diff = makeDiffWithNamespace("<add sel='doc'>" + newElement + "</add>", 
				"xmlns:y='http://example.com'");
		
		String expectedResult = DECLARATION + 
	    "<doc xmlns:x='http://example.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<x:foo color='blue' x:flavor='apricot'><x:bar/></x:foo></doc>";

	    doPatch(target, diff, expectedResult);
	}
	
	public void testAddSameNamespaceDefaultPrefix() throws Exception {
		System.out.println("*************");
		String target = DECLARATION + 
		"<doc xmlns='http://example.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"</doc>";  
		
		String newElement = "<y:foo color='blue' y:flavor='apricot'><y:bar/></y:foo>";				
		
		String diff = makeDiffWithNamespace("<add sel='*'>" + newElement + "</add>", 
				"xmlns:y='http://example.com'");
		
		String expectedResult = DECLARATION + 
	    "<doc xmlns='http://example.com'>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<foo xmlns:y='http://example.com' color='blue' y:flavor='apricot'><bar/></foo></doc>";

	    doPatch(target, diff, expectedResult);
	}
}
