package com.github.dnault.xmlpatch.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class AbstractPatchTask extends Task {
    private String src;
    private String dest;
    private String patch;
    private String inlinePatch;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = new File(src).getAbsolutePath();
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = new File(dest).getAbsolutePath();
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }

    public String getInlinePatch() {
        return inlinePatch;
    }

    public void addText(String text) {
        this.inlinePatch = getProject().replaceProperties(text);
    }

    protected void checkArgs() {
        if (src == null) {
            throw new BuildException("missing 'src' attribute");
        }

        if (patch == null && inlinePatch == null) {
            throw new BuildException("missing 'patch' attribute or nested text");
        }

        if (patch != null && inlinePatch != null) {
            throw new BuildException("must not provide both 'patch' attribute and nested text");
        }

        if (dest == null) {
            dest = src;
        }
    }

    public void execute() {
        checkArgs();

        File tempFile = null;
        try {
            if (getInlinePatch() != null) {
                try {
                    tempFile = File.createTempFile("inline-patch-", ".xml");
                    setPatch(tempFile.getAbsolutePath());

                    try (OutputStream os = new FileOutputStream(tempFile)) {
                        os.write(getInlinePatch().getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    throw new BuildException(e);
                }
            }

            try {
                log("Applying XML patch");
                log("      src: " + getSrc());
                log("    patch: " + getPatch());
                log("     dest: " + getDest());

                doPatch();

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

    protected abstract void doPatch() throws Exception;
}
