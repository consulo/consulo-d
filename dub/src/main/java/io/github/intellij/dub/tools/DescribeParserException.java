package io.github.intellij.dub.tools;

import org.jspecify.annotations.Nullable;

/**
 * @author Samael Bate (singingbush)
 * created on 06/02/18
 * @since v1.16.2
 */
public class DescribeParserException extends Throwable {
    public DescribeParserException(String message) {
        super(message);
    }

    public DescribeParserException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
