package io.github.intellij.dlanguage.highlighting.exitpoint;

import io.github.intellij.dlanguage.psi.DLanguageFunctionLiteralExpression;
import io.github.intellij.dlanguage.psi.DLanguageReturnStatement;
import io.github.intellij.dlanguage.psi.DLanguageSpecifiedFunctionBody;
import io.github.intellij.dlanguage.psi.DLanguageThrowExpression;
import io.github.intellij.dlanguage.psi.named.DLanguageFunctionDeclaration;

import java.util.function.Consumer;

public abstract sealed class ExitPoint permits ExitPoint.Return, ExitPoint.Throw {
    private ExitPoint() {
    }

    public static final class Return extends ExitPoint {
        private final DLanguageReturnStatement myE;

        public Return(DLanguageReturnStatement e) {
            myE = e;
        }

        public DLanguageReturnStatement getE() {
            return myE;
        }
    }

    public static final class Throw extends ExitPoint {
        private final DLanguageThrowExpression myE;

        public Throw(DLanguageThrowExpression e) {
            myE = e;
        }

        public DLanguageThrowExpression getE() {
            return myE;
        }
    }

    public static void process(DLanguageFunctionDeclaration fn, Consumer<ExitPoint> sink) {
        fn.acceptChildren(new ExitPointVisitor(sink));
    }

    public static void process(DLanguageFunctionLiteralExpression fn, Consumer<ExitPoint> sink) {
        DLanguageSpecifiedFunctionBody specifiedFunctionBody = fn.getSpecifiedFunctionBody();
        if (specifiedFunctionBody != null) {
            specifiedFunctionBody.acceptChildren(new ExitPointVisitor(sink));
        }
    }
}
