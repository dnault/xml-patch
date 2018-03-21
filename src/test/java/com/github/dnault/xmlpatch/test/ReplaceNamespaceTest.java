package com.github.dnault.xmlpatch.test;

import com.github.dnault.xmlpatch.ErrorCondition;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.dnault.xmlpatch.test.TestHelper.DECLARATION;
import static com.github.dnault.xmlpatch.test.TestHelper.EOL;
import static com.github.dnault.xmlpatch.test.TestHelper.doPatch;
import static com.github.dnault.xmlpatch.test.TestHelper.doPatchExpectError;
import static com.github.dnault.xmlpatch.test.TestHelper.makeDiff;

@Ignore("known failure")
public class ReplaceNamespaceTest {

    @Test
    public void replaceNamespaceDeclarationUri() throws Exception {
        String target = DECLARATION +"<doc xmlns:pref='urn:test'><pref:foo a='1'>sample</pref:foo></doc>";
        String diff = makeDiff("<replace sel='doc/namespace::pref'>urn:new:xxx</replace>");
        String expectedResult = DECLARATION + "<doc xmlns:pref='urn:new:xxx'><pref:foo a='1'>sample</pref:foo></doc>";
        doPatch(target, diff, expectedResult);
    }

    @Test
    public void replaceNamespaceUriWithEmptyString() throws Exception {
        String target = DECLARATION + "<doc xmlns:pref='urn:test'><pref:foo a='1'>sample</pref:foo></doc>";
        String diff = makeDiff("<replace sel='doc/namespace::pref'></replace>");
        doPatchExpectError(target, diff, ErrorCondition.INVALID_NAMESPACE_URI);
    }

    @Test
    public void replaceElementsOwnNamespaceDeclarationUri() throws Exception {
        String target = DECLARATION +"<pref:doc xmlns:pref='urn:test'><pref:foo a='1'>sample</pref:foo></pref:doc>";
        String diff = makeDiff("<replace sel='*/namespace::pref'>urn:new:xxx</replace>");
        String expectedResult = DECLARATION + "<pref:doc xmlns:pref='urn:new:xxx'><pref:foo a='1'>sample</pref:foo></pref:doc>";
        doPatch(target, diff, expectedResult);
    }

    @Test
    public void replaceParentsNamespaceDeclarationUri() throws Exception {
        String target = DECLARATION + "<doc xmlns:pref='urn:test'><foo a='1'>sample</foo></doc>";
        String diff = makeDiff("<replace sel='doc/foo/namespace::pref'>urn:new:xxx</replace>");
        doPatchExpectError(target, diff, ErrorCondition.UNLOCATED_NODE);
    }

    @Test
    public void removeNamespaceDeclaration() throws Exception {
        String target = DECLARATION +"<doc xmlns:pref='urn:test'/>";
        String diff = makeDiff("<remove sel='doc/namespace::pref'></remove>");
        String expectedResult = DECLARATION + "<doc/>";
        doPatch(target, diff, expectedResult);
    }
    
    @Test
    public void removeInUseNamespaceDeclaration() throws Exception {
        // spec says of the node to be removed,
        // "this prefix MUST NOT be associated with any node
        // prior to the removal of this namespace node."

        String target = DECLARATION +
                "<doc xmlns:pref='urn:test'><pref:foo/></doc>";
        String diff = makeDiff("<remove sel='doc/namespace::pref'></remove>");
        doPatchExpectError(target, diff, ErrorCondition.INVALID_PATCH_DIRECTIVE);
    }
    
    @Test
    public void replaceNamespaceDeclaration() throws Exception {
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

    @Test
    public void replaceNamespaceDeclarationError() throws Exception {
        String target = DECLARATION +
                "<doc xmlns:pref='urn:test'>" + EOL +
                "  <foo a='1'>This is a sample document</foo>" + EOL +
                "</doc>";

        String diff = makeDiff("<replace sel='doc/foo/namespace::pref'>urn:new:xxx</replace>");

        doPatchExpectError(target, diff, ErrorCondition.UNLOCATED_NODE);
    }
}
