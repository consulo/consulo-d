package io.github.intellij.dlanguage;

import consulo.d.icon.DIconGroup;
import consulo.language.Language;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;

/**
 * Port of upstream {@code core.kt}.
 */
public final class DLanguage extends Language {
    public static final DLanguage INSTANCE = new DLanguage();

    public static final String MODULE_TYPE_ID = "DLANGUAGE_MODULE";

    private DLanguage() {
        super("D");
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }

    public static final class Icons {
        public static final Image FILE = DIconGroup.dlang();
        public static final Image MODULE = FILE;
        public static final Image LANGUAGE = FILE;
        public static final Image SDK = FILE;
        public static final Image SDK_ADD = FILE;
        public static final Image RUN = FILE;
        public static final Image LIBRARY = FILE;
        public static final Image SRC_FILE = DIconGroup.dfile();
        public static final Image SRC_FILE_RUNNABLE = ImageEffects.layered(SRC_FILE, PlatformIconGroup.nodesRunnablemark());
        public static final Image SRC_FILE_PACKAGE = DIconGroup.dfilepackage();
        public static final Image CLASS = FILE;
        public static final Image NODE_CLASS = PlatformIconGroup.nodesClass();
        public static final Image NODE_INTERFACE = PlatformIconGroup.nodesInterface();
        public static final Image NODE_STRUCT = DIconGroup.nodesStruct();
        public static final Image NODE_UNION = DIconGroup.nodesUnion();
        public static final Image NODE_ENUM = PlatformIconGroup.nodesEnum();
        public static final Image NODE_ENUM_MEMBER =
            ImageEffects.layered(PlatformIconGroup.nodesField(), PlatformIconGroup.nodesStaticmark(), PlatformIconGroup.nodesFinalmark());
        public static final Image NODE_FUNCTION = DIconGroup.nodesFunction();
        public static final Image NODE_METHOD = PlatformIconGroup.nodesMethod();
        public static final Image NODE_FIELD = PlatformIconGroup.nodesField();
        public static final Image NODE_PROPERTY = PlatformIconGroup.nodesProperty();
        public static final Image NODE_PROPERTY_GETTER = DIconGroup.nodesPropertygetter();
        public static final Image NODE_PROPERTY_SETTER = DIconGroup.nodesPropertysetter();
        public static final Image NODE_ALIAS = DIconGroup.nodesAlias();
        public static final Image NODE_MIXINDECL = DIconGroup.nodesMixindecl();
        public static final Image NODE_MIXIN = DIconGroup.nodesMixin();

        private Icons() {
        }
    }
}
