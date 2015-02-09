package com.github.dnault.xmlpatch;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class XmlPatchFilter extends FilterReader {
    private String patch;
    private boolean initialized;
    private File output;

    public XmlPatchFilter(Reader in) {
        super(in);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public void setPatch(String patch) {
        if (patch != null) {
            patch = new File(patch).getAbsolutePath();
        }
        this.patch = patch;
    }

    public String getPatch() {
        return patch;
    }

    protected void initialize() {
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

    protected void initializeIfNecessary() {
        if (!isInitialized()) {
            initialize();
            setInitialized(true);
        }
    }

    public int read() throws IOException {
        initializeIfNecessary();
        return super.read();
    }

    /**
     * Reads characters into a portion of an array.
     *
     * @throws IOException If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        initializeIfNecessary();
        return super.read(cbuf, off, len);
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
