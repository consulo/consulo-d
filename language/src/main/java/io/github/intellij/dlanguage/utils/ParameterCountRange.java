package io.github.intellij.dlanguage.utils;

/**
 * Created by francis on 7/22/2017.
 */
public class ParameterCountRange {
    private final int myMin;
    private final int myMax;

    public ParameterCountRange(int min, int max) {
        myMin = min;
        myMax = max;
    }

    public int getMin() {
        return myMin;
    }

    public int getMax() {
        return myMax;
    }

    public boolean matches(int num) {
        return num >= myMin && num <= myMax;
    }
}
