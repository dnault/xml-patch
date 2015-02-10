package com.github.dnault.xmlpatch.gradle

import com.github.dnault.xmlpatch.batch.AssembledPatch
import com.github.dnault.xmlpatch.batch.BatchXmlPatchFilter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.internal.AbstractTask

class XmlPatchPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.getConvention().add("augmentWithXmlPatch", { CopySpec copySpec,
                                                             File patchFile,
                                                             XmlPatchOptions options = new XmlPatchOptions() ->
            if (!patchFile.exists()) {
                def message = "Patch file does not exist: ${patchFile.absolutePath}"
                if (options.failOnMissingPatch) {
                    throw new FileNotFoundException(message)
                }
                project.logger.info message
                return;
            }

            project.configure(copySpec) {
                def assembledPatch = new AssembledPatch(patchFile);

                eachFile {
                    filter(BatchXmlPatchFilter, patch: assembledPatch, sourcePath: it.sourcePath)
                }

                if (copySpec instanceof AbstractTask) {
                    def seenSourcePaths = [] as Set
                    def missingSourcePaths = [] as Set

                    eachFile {
                        seenSourcePaths += [it.sourcePath]
                    }

                    inputs.files assembledPatch.patchFiles

                    doLast {
                        missingSourcePaths = assembledPatch.sourcePaths - seenSourcePaths
                        if (!missingSourcePaths.isEmpty()) {
                            String message = "XML patch ${patchFile} references missing source paths: " + missingSourcePaths

                            if (options.failOnMissingSourcePath) {
                                throw new FileNotFoundException(message);
                            }
                            project.logger.info message
                        }
                    }

                    // store some properties in the task to help diagnose issues
                    //ext.xmlPatch = new XmlPatchExtraProperties()
                    //xmlPatch.assembledPatch = assembledPatch
                    //xmlPatch.seenSourcePaths = [] as Set
                    //xmlPatch.missingSourcePaths = [] as Set
                }
            }

        })
    }
}
