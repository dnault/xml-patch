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

import static com.github.dnault.xmlpatch.test.TestHelper.*;
import junit.framework.TestCase;

import com.github.dnault.xmlpatch.ErrorCondition;

public class AddAttributeTest extends TestCase {

	private static final String COMMON_TARGET = DECLARATION + 
		"<doc>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<foo id='ert4773'>This is a new child</foo></doc>";  
	
	public void testAddAttribute() throws Exception {		

		String diff = makeDiff("<add sel=\"doc/foo[@id='ert4773']\" type='@user'>Bob</add>");
		
		String expectedResult = DECLARATION + 
		"<doc>" + EOL + 
		"  <note>This is a sample document</note>" + EOL +
		"<foo id='ert4773' user='Bob'>This is a new child</foo></doc>";
	
		doPatch(COMMON_TARGET, diff, expectedResult);
	}
	
	public void testAddAttributeQualifiedWithInvalidPrefix() throws Exception {
        
        String diff = makeDiff("<add sel=\"doc/foo[@id='ert4773']\" type='@y:user'>Bob</add>");       

        doPatchExpectError(COMMON_TARGET, diff, ErrorCondition.INVALID_NAMESPACE_PREFIX);
	}

    public void testAddQualifiedAttributeWithoutDeclaration() throws Exception { 
        fail();
        // todo 
        // the reference implementation doesn't allow adding qualified attributes
        // unless the target document already declares the namespace. 
    }
    
	public void testAddQualifiedAttribute() throws Exception {      

	    String target = DECLARATION + 
        "<doc xmlns:x='urn:foo'>" + EOL + 
        "  <note>This is a sample document</note>" + EOL +
        "<foo id='ert4773'>This is a new child</foo></doc>";
	    
        String diff = makeDiffWithNamespace("<add sel=\"doc/foo[@id='ert4773']\" type='@y:user'>Bob</add>",
                "xmlns:y='urn:foo'");
        
        String expectedResult = DECLARATION + 
        "<doc xmlns:x='urn:foo'>" + EOL + 
        "  <note>This is a sample document</note>" + EOL +
        "<foo id='ert4773' x:user='Bob'>This is a new child</foo></doc>";
    
        doPatch(target, diff, expectedResult);
    }
	
	public void testAddNamespaceDeclaration() throws Exception {      

        String target = DECLARATION + 
        "<doc/>";
        
        String diff = makeDiff("<add sel='doc' type='namespace::pref'>urn:ns:xxx</add>");
        
        String expectedResult = DECLARATION + 
        "<doc xmlns:pref='urn:ns:xxx'/>";
        
        doPatch(target, diff, expectedResult);
    }
	public void testReplaceNamespaceDeclaration() throws Exception {
	    String target = DECLARATION +
	    "<doc xmlns:pref='urn:test'>" + EOL +
        "  <foo a='1'>This is a sample document</foo>" + EOL +
        "</doc>";

	    String diff = makeDiff("<replace sel='doc/namespace::pref'>urn:new:xxx</replace>");
        
	    String expectedResult = DECLARATION +
        "<doc xmlns:pref='urn:new:xxx'>" + EOL +
        "  <foo a='1'>This is a sample document</foo>" + EOL +
        "</doc>";
	    
	    doPatch(target, diff, expectedResult);
	}
	
	public void testReplaceNamespaceDeclarationError() throws Exception {
        String target = DECLARATION +
        "<doc xmlns:pref='urn:test'>" + EOL +
        "  <foo a='1'>This is a sample document</foo>" + EOL +
        "</doc>";

        String diff = makeDiff("<replace sel='doc/foo/namespace::pref'>urn:new:xxx</replace>");
                
        doPatchExpectError(target, diff, ErrorCondition.UNLOCATED_NODE);
    }
	
	
    public void testReplaceAttribute() throws Exception {
	    String target = DECLARATION + 
	    "<doc a='test'>" + EOL +
        "  <foo a='1'>This is a sample document</foo>" + EOL +
        "</doc>";

	    String diff = makeDiff("<replace sel='doc/@a'>new value</replace>");
	    
	    String expectedResult = DECLARATION + 
        "<doc a='new value'>" + EOL +
        "  <foo a='1'>This is a sample document</foo>" + EOL +
        "</doc>";        
	    
	    doPatch(target, diff, expectedResult); 
	}
	
	public void testReplaceAttributeWithEmptyString() throws Exception {
        String target = DECLARATION + 
        "<doc a='test'>" + EOL +
        "  <foo a='1'>This is a sample document</foo>" + EOL +
        "</doc>";

        String diff = makeDiff("<replace sel='doc/@a'/>");
        
        String expectedResult = DECLARATION + 
        "<doc a=''>" + EOL +
        "  <foo a='1'>This is a sample document</foo>" + EOL +
        "</doc>";        
        
        doPatch(target, diff, expectedResult); 
    }
}
