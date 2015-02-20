package com.github.dnault.xmlpatch.gradle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;

import com.github.dnault.xmlpatch.XmlPatchFilter;
import com.github.dnault.xmlpatch.batch.AssembledPatch;
import com.github.dnault.xmlpatch.internal.DeferredInitFilterReader;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class BatchXmlPatchFilter extends DeferredInitFilterReader {
    XmlPatchSpec spec;
    String path;

    public BatchXmlPatchFilter(Reader in) {
        super(in);
    }

    @Override
    protected void initialize() {
        if (spec == null) {
            throw new RuntimeException("missing 'spec' parameter, value must be XmlPatchSpec instance");
        }

        if (path == null) {
            throw new RuntimeException("missing 'path' parameter, relative path to file being patched");
        }

        try {
            in = buildFilterChain(in, path, spec.resolve());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Reader buildFilterChain(Reader source, String sourcePath, AssembledPatch patch) throws IOException {
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
