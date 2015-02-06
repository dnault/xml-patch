package com.github.dnault.xmlpatch.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.github.dnault.xmlpatch.BatchPatcher;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PatchXmlDir extends Task {
    private String srcDir;
    private String patchFile;
    private String destDir;
    private String inlinePatch;

    public String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    public String getPatchFile() {
        return patchFile;
    }

    public void setPatchFile(String patchFile) {
        this.patchFile = patchFile;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public String getInlinePatch() {
        return inlinePatch;
    }

    public void setInlinePatch(String inlinePatch) {
        this.inlinePatch = inlinePatch;
    }

    public void addText(String text) {
        this.inlinePatch = getProject().replaceProperties(text);
    }

    public void execute() {
        if (srcDir == null) {
            throw new BuildException("missing 'srcDir' attribute");
        }

        if (patchFile == null && inlinePatch == null) {
            throw new BuildException("missing 'patchFile' attribute or nested text");
        }

        if (patchFile != null && inlinePatch != null) {
            throw new BuildException("must not provide both 'patchFile' attribute and nested text");
        }

        if (destDir == null) {
            destDir = srcDir;
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
                log("Applying XML patch to directory");
                log("   source: " + srcDir);
                log("    patch: " + patchFile);
                log("     dest: " + destDir);

                BatchPatcher.patch("--patch=" + patchFile, "--srcdir=" + srcDir, "--destdir=" + destDir);
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

