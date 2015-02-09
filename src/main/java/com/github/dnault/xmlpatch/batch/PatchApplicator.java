package com.github.dnault.xmlpatch.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;

import com.github.dnault.xmlpatch.XmlPatchFilter;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class PatchApplicator {

    public Reader buildFilterChain(Reader source, String sourcePath, AssembledPatch patch) throws IOException {
        for (Element diff : patch.getDiffs(sourcePath)) {
            diff = (Element) diff.clone();
            diff.removeAttribute("file");

            final File tempDiff = saveToTempFile(diff);

            source = new XmlPatchFilter(source) {
                {
                    setPatch(tempDiff.getAbsolutePath());
                }

                @Override
                public void close() throws IOException {
                    super.close();
                    FileUtils.deleteQuietly(tempDiff);
                }
            };
        }

        return source;
    }

    private File saveToTempFile(Element diff) throws IOException {
        Format format = Format.getRawFormat();
        format.setOmitDeclaration(true);
        XMLOutputter outputter = new XMLOutputter(format);

        File temp = File.createTempFile("xml-patch-diff-", ".xml");
        try (FileOutputStream os = new FileOutputStream(temp)) {
            outputter.output(diff, os);
        }

        System.out.println("created temp file: " + temp.getAbsolutePath());

        return temp;
    }
}
