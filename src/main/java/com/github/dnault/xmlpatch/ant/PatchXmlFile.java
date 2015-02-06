package com.github.dnault.xmlpatch.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.github.dnault.xmlpatch.CommandLineDriver;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PatchXmlFile extends Task {
    private String srcFile;
    private String patchFile;
    private String destFile;
    private String inlinePatch;

    public String getSrcFile() {
        return srcFile;
    }

    public void setSrcFile(String srcFile) {
        this.srcFile = srcFile;
    }

    public String getPatchFile() {
        return patchFile;
    }

    public void setPatchFile(String patchFile) {
        this.patchFile = patchFile;
    }

    public String getDestFile() {
        return destFile;
    }

    public void setDestFile(String destFile) {
        this.destFile = destFile;
    }

    public void addText(String text) {
        this.inlinePatch = getProject().replaceProperties(text);
    }

    public void execute() {
        if (srcFile == null) {
            throw new BuildException("missing 'srcFile' attribute");
        }

        if (patchFile == null && inlinePatch == null) {
            throw new BuildException("missing 'patchFile' attribute or nested text");
        }

        if (patchFile != null && inlinePatch != null) {
            throw new BuildException("must not provide both 'patchFile' attribute and nested text");
        }

        if (destFile == null) {
            destFile = srcFile;
        }

        File tempFile = null;
        try {
            if (inlinePatch != null) {
                try {
                    tempFile = File.createTempFile("inline-patch-", ".xml");
                    patchFile = tempFile.getAbsolutePath();

                    try (OutputStream os = new FileOutputStream(tempFile)) {
                        os.write(inlinePatch.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    throw new BuildException(e);
                }
            }

            try {
                log("Applying XML patch");
                log("   source: " + srcFile);
                log("    patch: " + patchFile);
                log("     dest: " + destFile);

                CommandLineDriver.main(srcFile, patchFile, destFile);
            } catch (Exception e) {
                throw new BuildException(e);
            }
        } finally {
            if (tempFile != null) {
                if (!tempFile.delete()) {
                    log("Failed to delete temp file for inline XML patch: " + tempFile.getAbsolutePath());
                }
            }
        }
    }
}

