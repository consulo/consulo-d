package io.github.intellij.dub.project.settings;

import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.ui.ModalityState;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Consulo: upstream's pooled-thread {@code Alarm} is AWT-only, the debouncing is done with a scheduled future.
 */
public class UiDebouncer {
    private final ImprovedDisposable myParentDisposable;
    private final int myDelayMillis;

    private @Nullable Future<?> myRequest;

    public UiDebouncer(ImprovedDisposable parentDisposable) {
        this(parentDisposable, 200);
    }

    public UiDebouncer(ImprovedDisposable parentDisposable, int delayMillis) {
        myParentDisposable = parentDisposable;
        myDelayMillis = delayMillis;
    }

    /**
     * @param onUiThread: callback to be executed in EDT with **any** modality state.
     * Use it only for UI updates
     */
    @RequiredUIAccess
    public synchronized <T extends @Nullable Object> void run(Supplier<T> onPooledThread, Consumer<T> onUiThread) {
        if (myParentDisposable.isDisposed()) {
            return;
        }
        Future<?> request = myRequest;
        if (request != null) {
            request.cancel(false);
        }
        UIAccess uiAccess = UIAccess.current();
        myRequest = AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
            T r = onPooledThread.get();
            uiAccess.giveLater(() -> {
                if (!myParentDisposable.isDisposed()) {
                    onUiThread.accept(r);
                }
            }, ModalityState.any());
        }, myDelayMillis, TimeUnit.MILLISECONDS);
    }
}
