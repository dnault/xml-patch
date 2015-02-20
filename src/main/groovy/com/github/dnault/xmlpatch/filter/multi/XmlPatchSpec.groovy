package com.github.dnault.xmlpatch.filter.multi

import com.github.dnault.xmlpatch.batch.AssembledPatch
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.collections.FileCollectionAdapter
import org.gradle.api.internal.file.collections.MinimalFileSet


class XmlPatchSpec {
    File patch;
    XmlPatchOptions options;

    AssembledPatch assembled;

    XmlPatchSpec(Map options = [:], File patch) {
        this.patch = patch
        this.options = new XmlPatchOptions(options)
    }

    XmlPatchSpec(Map options = [:], String patch) {
        this(options, new File(patch))
    }

    AssembledPatch resolve() {
        if (assembled == null) {
            assembled = new AssembledPatch(patch)
        }
        return assembled
    }

    FileCollection getPatchFragments() {
        return new FileCollectionAdapter(new MinimalFileSet() {
            @Override
            public Set<File> getFiles() {
                return resolve().getPatchFiles();
            }

            @Override
            public String getDisplayName() {
                return "Patch fragments referenced by ${patch.absolutePath}"
            }
        });
    }

    @Override
    public String toString() {
        return "PatchSpec{" +
                "patch=" + patch +
                ", options=" + options +
                '}';
    }

    void assertFullyApplied() {
        assert false: "Some source files referenced by ${patch.absolutePath} were not processed"
    }
}
