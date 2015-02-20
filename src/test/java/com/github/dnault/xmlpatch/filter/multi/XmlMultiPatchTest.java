package com.github.dnault.xmlpatch.filter.multi;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import com.github.dnault.xmlpatch.batch.AssembledPatch;
import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.junit.Test;

public class XmlMultiPatchTest {
    @Test
    public void testBuildFilterChain() throws Exception {
        StringReader source = new StringReader("<doc/>");

        AssembledPatch patch = new AssembledPatch();
        patch.addDif(new Element("diff").setAttribute("file", "foo/bar.xml")
                .addContent(new Element("add").setAttribute("sel", "doc").addContent(new Element("child"))));

        patch.addDif(new Element("diff").setAttribute("file", "foo/bar.xml")
                .addContent(new Element("add").setAttribute("sel", "doc/child").addContent(new Element("grandchild"))));

        patch.addDif(new Element("diff").setAttribute("file", "other/bar.xml")
                .addContent(new Element("add").setAttribute("sel", "doc/child").addContent(new Element("grandchild"))));

        Reader patched = new XmlMultiPatch(source).buildFilterChain(source, "foo/bar.xml", patch);

        StringWriter result = new StringWriter();

        IOUtils.copy(patched, result);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<doc><child><grandchild /></child></doc>\n", result.toString());
    }
}
