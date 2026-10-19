package io.github.intellij.dub.project.settings;

import consulo.disposer.Disposable;

public interface ImprovedDisposable extends Disposable {
    boolean isDisposed();
}
