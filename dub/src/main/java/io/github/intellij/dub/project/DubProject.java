package io.github.intellij.dub.project;

import java.util.List;

/**
 * This object represents the data that is parsed when running 'dub describe'
 *
 * Do not remove anything from this file without annotating it as deprected for a few releases
 *
 * @author Samael Bate (singingbush)
 * created on 06/02/18
 * @since v1.16.2
 */
public record DubProject(
    String rootPackageName, // this is "rootPackage" in the json
    DubPackage rootPackage, // this is actually in the packages[] but filter it out and place here
    String configuration,
    String buildType,
    String compiler,
    List<String> architecture, // eg: [x86_64"],
    List<String> platform, // eg: ["linux","posix"],
    List<DubPackage> packages, // after having the "rootPackage" removed
    List<DubTarget> targets
) {
}
