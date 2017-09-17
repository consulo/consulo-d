/*
 * (These files where modified from: https://bitbucket.org/spencercw/ideagdb/src
 * Original Copyright:
 * Copyright (c) 2013 Chris Spencer <spencercw@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.)
 */

package uk.co.cwspencer.gdb.gdbmi;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a result record from a GDB/MI stream.
 */
public class GdbMiResultRecord extends GdbMiRecord {
    /**
     * The result/async class.
     */
    public String className;

    /**
     * The results.
     */
    public List<GdbMiResult> results = new ArrayList<GdbMiResult>();

    /**
     * Constructor.
     *
     * @param type      The record type.
     * @param userToken The user token. May be null.
     */
    public GdbMiResultRecord(Type type, Long userToken) {
        this.type = type;
        this.userToken = userToken;
    }

    /**
     * Converts the record to a string.
     *
     * @return A string containing the class name and any results.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        if (!results.isEmpty()) {
            sb.append(": [");
            for (int i = 0; i != results.size(); ++i) {
                sb.append(results.get(i));
                if (i < results.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
