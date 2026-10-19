package io.github.intellij.dlanguage.unittest;

import consulo.execution.action.Location;
import consulo.execution.test.sm.runner.SMTestLocator;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import java.util.LinkedList;
import java.util.List;

public class DUnitTestLocationProvider implements SMTestLocator {
    private static final DUnitTestLocationProvider INSTANCE = new DUnitTestLocationProvider();

    public static final String PROTOCOL_ID = "D";
    public static final String PROTOCOL_PREFIX = PROTOCOL_ID + "://";

    public static DUnitTestLocationProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Location> getLocation(final String protocol,
                                      final String path,
                                      final Project project,
                                      final GlobalSearchScope scope) {
        final List<Location> locations = new LinkedList<>();

        if (!StringUtil.equals(PROTOCOL_ID, protocol) || StringUtil.isEmpty(path))
        {
            return locations;
        }

        // TODO: Process the path to find the corresponding source

        return locations;
    }
}
