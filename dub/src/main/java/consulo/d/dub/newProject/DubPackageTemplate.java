package consulo.d.dub.newProject;

import consulo.d.localize.DLocalize;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import io.github.intellij.dlanguage.DLanguage;
import io.github.intellij.dub.project.wizard.DubNewProjectWizardStep.DubProjectType;

/**
 * The entries of the D group of the new project wizard: the {@code dub init} type of each, and whether the package is a
 * library (DUB has no init type for one: a {@code minimal} package is turned into a library) or an application, which
 * gets a "Run DUB" configuration.
 */
public enum DubPackageTemplate {
    APPLICATION(DubProjectType.MINIMAL, false),
    LIBRARY(DubProjectType.MINIMAL, true),
    VIBE(DubProjectType.VIBE, false),
    DEIMOS(DubProjectType.DEIMOS, true);

    private final DubProjectType myProjectType;
    private final boolean myLibrary;

    DubPackageTemplate(DubProjectType projectType, boolean library) {
        myProjectType = projectType;
        myLibrary = library;
    }

    public DubProjectType getProjectType() {
        return myProjectType;
    }

    public boolean isLibrary() {
        return myLibrary;
    }

    public LocalizeValue getTitle() {
        return switch (this) {
            case APPLICATION -> DLocalize.newProjectDubApplication();
            case LIBRARY -> DLocalize.newProjectDubLibrary();
            case VIBE -> DLocalize.newProjectDubVibe();
            case DEIMOS -> DLocalize.newProjectDubDeimos();
        };
    }

    public Image getIcon() {
        return switch (this) {
            case APPLICATION -> DLanguage.Icons.FILE;
            case LIBRARY, DEIMOS -> PlatformIconGroup.nodesPplib();
            case VIBE -> PlatformIconGroup.nodesPpweb();
        };
    }
}
