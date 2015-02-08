package com.github.dnault.xmlpatch;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.filters.BaseFilterReader;
import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.Parameter;

public class XmlPatchFilter extends BaseParamFilterReader implements ChainableReader {
    private String patch;

    private File output;

    public void setPatch(String patch) {
        this.patch = new File(patch).getAbsolutePath();
    }

    public String getPatch() {
        return patch;
    }

    /**
     * Constructor for "dummy" instances.
     *
     * @see BaseFilterReader#BaseFilterReader()
     */
    @SuppressWarnings("unused")
    public XmlPatchFilter() {
    }

    public XmlPatchFilter(Reader in) {
        super(in);
    }

    public int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }

        return super.read();
    }

    protected void initialize() {
        if (getParameters() != null) {
            for (Parameter p : getParameters()) {
                if (p.getName().equalsIgnoreCase("patch")) {
                    setPatch(p.getValue());
                } else {
                    throw new RuntimeException("unrecognized parameter: " + p.getName());
                }
            }
        }

        if (patch == null) {
            throw new RuntimeException("missing 'patch' parameter, path to patch file");
        }

        try {
            File input = File.createTempFile("xml-patch-input-", ".xml");
            try {
                output = File.createTempFile("xml-patch-result-", ".xml");

                try (FileOutputStream os = new FileOutputStream(input);
                     Writer writer = new OutputStreamWriter(os, UTF_8);
                     Reader reader = in) {
                    IOUtils.copy(reader, writer);
                }

                CommandLineDriver.main(input.getAbsolutePath(), patch, output.getAbsolutePath());
                in = new FileReader(output);

            } finally {
                FileUtils.deleteQuietly(input);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Reader chain(Reader reader) {
        XmlPatchFilter r = new XmlPatchFilter(reader);
        r.setPatch(getPatch());
        r.setInitialized(true);
        return r;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            FileUtils.deleteQuietly(output);
        }
    }
}
