package com.github.dnault.xmlpatch.test;


import org.junit.Test;

public class Rfc7351Test {

    @Test
    public void compatibleWithRfc7351PatchOperationNamespace() throws Exception {
        String target = "<doc><note>This is a sample document</note></doc>";
        String patch = "<p:patch xmlns:p=\"urn:ietf:rfc:7351\">\n" +
                "    <p:add sel=\"doc\"><foo id=\"ert4773\">This is a new child</foo></p:add>\n" +
                "</p:patch>";
        String expected = "<doc><note>This is a sample document</note><foo id=\"ert4773\">This is a new child</foo></doc>";

        TestHelper.doPatch(target, patch, expected);
    }
}
