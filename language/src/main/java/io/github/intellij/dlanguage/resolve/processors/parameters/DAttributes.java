package io.github.intellij.dlanguage.resolve.processors.parameters;

import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Declared in upstream {@code DAttributesFinder.kt}.
 */
public class DAttributes {
    private final boolean myStatic;
    private final DAttributesFinder.Visibility myVisibility;
    private final boolean myProperty;
    private final boolean myNoGC;
    private final boolean myExtern;
    private final boolean myPure;
    private final boolean myNothrow;
    private final boolean myConst;
    private final boolean myImmutable;
    private final boolean myEnum;

    public DAttributes(boolean isStatic,
                       DAttributesFinder.Visibility visibility,
                       boolean property,
                       boolean noGC,
                       boolean extern,
                       boolean pure,
                       boolean nothrow,
                       boolean isConst,
                       boolean immutable,
                       boolean isEnum) {
        myStatic = isStatic;
        myVisibility = visibility;
        myProperty = property;
        myNoGC = noGC;
        myExtern = extern;
        myPure = pure;
        myNothrow = nothrow;
        myConst = isConst;
        myImmutable = immutable;
        myEnum = isEnum;
    }

    public boolean getStatic() {
        return myStatic;
    }

    public DAttributesFinder.Visibility getVisibility() {
        return myVisibility;
    }

    public boolean getProperty() {
        return myProperty;
    }

    public boolean getNoGC() {
        return myNoGC;
    }

    public boolean getExtern() {
        return myExtern;
    }

    public boolean getPure() {
        return myPure;
    }

    public boolean getNothrow() {
        return myNothrow;
    }

    public boolean getConst() {
        return myConst;
    }

    public boolean getImmutable() {
        return myImmutable;
    }

    public boolean getEnum() {
        return myEnum;
    }

    public void write(StubOutputStream stream) throws IOException {
        stream.writeBoolean(myStatic);
        stream.writeBoolean(myProperty);
        stream.writeBoolean(myNoGC);
        stream.writeBoolean(myExtern);
        stream.writeBoolean(myPure);
        stream.writeBoolean(myNothrow);
        stream.writeBoolean(myConst);
        stream.writeBoolean(myImmutable);
        stream.writeBoolean(myEnum);
        myVisibility.write(stream);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DAttributes that)) {
            return false;
        }

        if (myStatic != that.myStatic) {
            return false;
        }
        if (myVisibility != that.myVisibility) {
            return false;
        }
        if (myProperty != that.myProperty) {
            return false;
        }
        if (myNoGC != that.myNoGC) {
            return false;
        }
        if (myExtern != that.myExtern) {
            return false;
        }
        if (myPure != that.myPure) {
            return false;
        }
        if (myNothrow != that.myNothrow) {
            return false;
        }
        if (myConst != that.myConst) {
            return false;
        }
        if (myImmutable != that.myImmutable) {
            return false;
        }
        if (myEnum != that.myEnum) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(myStatic);
        result = 31 * result + myVisibility.hashCode();
        result = 31 * result + Boolean.hashCode(myProperty);
        result = 31 * result + Boolean.hashCode(myNoGC);
        result = 31 * result + Boolean.hashCode(myExtern);
        result = 31 * result + Boolean.hashCode(myPure);
        result = 31 * result + Boolean.hashCode(myNothrow);
        result = 31 * result + Boolean.hashCode(myConst);
        result = 31 * result + Boolean.hashCode(myImmutable);
        result = 31 * result + Boolean.hashCode(myEnum);
        return result;
    }


    public static DAttributes read(StubInputStream stream) throws IOException {
        boolean isStatic = stream.readBoolean();
        boolean property = stream.readBoolean();
        boolean noGC = stream.readBoolean();
        boolean extern = stream.readBoolean();
        boolean pure = stream.readBoolean();
        boolean nothrow = stream.readBoolean();
        boolean isConst = stream.readBoolean();
        boolean immutable = stream.readBoolean();
        boolean isEnum = stream.readBoolean();
        DAttributesFinder.Visibility visibility = DAttributesFinder.Visibility.read(stream);
        return new DAttributes(isStatic, visibility, property, noGC, extern, pure, nothrow, isConst, immutable, isEnum);
    }


}
