package io.github.intellij.dub.tools;

import io.github.intellij.dub.project.DubProject;

import java.io.Reader;

/**
 * The code that was specific to actually parsing the json from 'dub describe' has
 * been pulled out from DubConfigurationParser.java
 *
 * @author Samael Bate (singingbush)
 * created on 06/02/18
 * @since v1.16.2
 */
public interface DescribeParser {

    /**
     * @param reader The 'dub describe' command dumps a load of json to stdout, allocate a Reader for the stdout and pass it in.
     * @return A DubProject object which contains much (not all) of the data that was in the json.
     * @since v1.28.1
     */
    DubProject parse(Reader reader) throws DescribeParserException;

    /**
     * @param json The 'dub describe' command dumps a load of json to stdout, use that output with this method.
     * @return A DubProject object which contains much (not all) of the data that was in the json.
     * @since v1.16.2
     */
    DubProject parse(String json) throws DescribeParserException;
}
