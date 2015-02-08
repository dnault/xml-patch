package com.github.dnault.xmlpatch.gradle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.dnault.xmlpatch.ant.XmlPatchFilter;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.InputFile;

public class PatchXml extends Copy {
    private File patch;

    @InputFile
    File getPatch() {
        return patch;
    }

    void setPatch(File patch) {
        this.patch = patch;
    }

    @Override
    protected void copy() {
        Map<String, String> params = new HashMap<>();
        params.put("patch", patch.getAbsolutePath());

        filter(params, XmlPatchFilter.class);
        super.copy();

        //filter(XmlPatchFilter, patch: patch.getAbsolutePath())
        //super.copy()
    }
}