package io.github.intellij.dlanguage.messagebus;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;

/**
 * Port of upstream {@code listeners.kt}.
 */
@TopicAPI(ComponentScope.PROJECT)
public interface DubChangeNotifier {

    /**
     * @param project
     * @param module
     * @param dubFile the dub.json or dub.sdl that has been changed
     */
    void onDubFileChange(Project project, Module module, VirtualFile dubFile);

    /**
     * @param project
     * @param module
     * @param dubFile the dub.selections.json that has been changed
     */
    void onDubSelectionsFileChange(Project project, Module module, VirtualFile dubFile);
}
