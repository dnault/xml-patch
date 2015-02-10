package com.github.dnault.xmlpatch.gradle

import com.github.dnault.xmlpatch.batch.AssembledPatch
import com.github.dnault.xmlpatch.batch.BatchXmlPatchFilter
import com.github.dnault.xmlpatch.batch.PatchAssembler
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input

public class BatchPatchXml extends Copy {
    private AssembledPatch patch;

    @Input
    boolean failOnMissingSource = true

    @Input
    boolean failOnMissingPatch = true

    Set<String> processedSourcePaths = new HashSet<>();

    void setPatch(File patch) throws Exception {
        if (!patch.exists()) {
            getInputs().file(patch);
            return;
        }

        AssembledPatch assembled = new PatchAssembler().assemble(patch);
        this.patch = assembled;

        for (String patchFragment : assembled.getPatchFiles()) {
            getInputs().file(patchFragment);
        }
    }

    AssembledPatch getPatch() {
        return patch
    }

    @Override
    protected void copy() {
        if (this.patch == null && !failOnMissingPatch) {
            println "patch file is missing, but 'failOnMissingPatch = false' so skipping patch filter"
            super.copy();
            return;
        }

        eachFile {
            filter([patch: this.patch, sourcePath: it.sourcePath], BatchXmlPatchFilter)
            processedSourcePaths.add(it.sourcePath)
        }
        super.copy();

        if (failOnMissingSource) {
            Set<String> missingSourcePaths = this.patch.sourcePaths;
            missingSourcePaths.removeAll(processedSourcePaths);
            if (!missingSourcePaths.isEmpty()) {
                throw new RuntimeException("patch document specified non-existent source paths: " + missingSourcePaths);
            }
        }
    }
}