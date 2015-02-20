package com.github.dnault.xmlpatch.filter;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import com.github.dnault.xmlpatch.CommandLineDriver;
import com.github.dnault.xmlpatch.internal.DeferredInitFilterReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class XmlPatch extends DeferredInitFilterReader {
    private String patch;
    private File output;

    public XmlPatch(Reader in) {
        super(in);
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

    @Override
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

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            FileUtils.deleteQuietly(output);
        }
    }
}
