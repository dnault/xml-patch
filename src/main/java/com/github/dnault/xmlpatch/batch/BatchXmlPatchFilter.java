package com.github.dnault.xmlpatch.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;

import com.github.dnault.xmlpatch.XmlPatchFilter;
import com.github.dnault.xmlpatch.internal.DeferredInitFilterReader;
import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class BatchXmlPatchFilter extends DeferredInitFilterReader {
    private AssembledPatch patch;
    private String sourcePath;

    public AssembledPatch getPatch() {
        return patch;
    }

    public void setPatch(AssembledPatch patch) {
        this.patch = patch;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public BatchXmlPatchFilter(Reader in) {
        super(in);
    }

    protected void initialize() {
        if (patch == null) {
            throw new RuntimeException("missing 'patch' parameter, path to patch file");
        }

        if (sourcePath == null) {
            throw new RuntimeException("missing 'sourcePath' parameter, relative path to file being patched");
        }

        try {
            in = buildFilterChain(in, sourcePath, patch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
