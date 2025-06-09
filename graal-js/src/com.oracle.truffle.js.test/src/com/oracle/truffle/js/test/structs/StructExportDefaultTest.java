/*
 * Copyright (c) 2018, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.truffle.js.test.structs;

import com.oracle.truffle.js.test.JSTest;
import com.oracle.truffle.js.test.polyglot.MockFileSystem;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.IOAccess;
import org.junit.Test;

import java.util.Map;

import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_NAME;
import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_STAGING;

/**
 * Basic tests for struct default export.
 */
public class StructExportDefaultTest {
    @Test
    public void testExportDefaultStructDeclaration() {
        Map<String, String> modules = Map.of(
                "point.mjs", """
            export default struct Point {
                x = 0;
                y = 0;
                getCoordinates() {
                    return `(${this.x}, ${this.y})`;
                }
            }
            """
        );

        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).allowIO(IOAccess.newBuilder().fileSystem(new MockFileSystem(modules)).build()).build()) {
            ctx.eval(Source.newBuilder("js", """
            import Point from 'point.mjs';

            'use strict';
            let point = new Point();
            if (point.x !== 0 || point.y !== 0) {
                throw new Error('Initial values should be 0');
            }
            if (point.getCoordinates() !== '(0, 0)') {
                throw new Error('Initial coordinates should be (0, 0)');
            }
            point.x = 5;
            point.y = 10;
            if (point.x !== 5 || point.y !== 10) {
                throw new Error('Modified values should be 5 and 10');
            }
            if (point.getCoordinates() !== '(5, 10)') {
                throw new Error('Modified coordinates should be (5, 10)');
            }
            try {
                Object.defineProperty(point, 'x', {value: 42, configurable: true});
                throw new Error('defineProperty should throw');
            } catch (e) {
                if (!(e instanceof TypeError)) {
                    throw e;
                }
            }
            """, "test.mjs").buildLiteral());
        }
    }

    @Test
    public void testExportDefaultAnonymousStructDeclaration() {
        Map<String, String> modules = Map.of(
                "point.mjs", """
            export default struct {
                x = 0;
                y = 0;
                getCoordinates() {
                    return `(${this.x}, ${this.y})`;
                }
            }
            """
        );

        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).allowIO(IOAccess.newBuilder().fileSystem(new MockFileSystem(modules)).build()).build()) {
            ctx.eval(Source.newBuilder("js", """
            import Point from 'point.mjs';

            'use strict';
            let point = new Point();
            if (point.x !== 0 || point.y !== 0) {
                throw new Error('Initial values should be 0');
            }
            if (point.getCoordinates() !== '(0, 0)') {
                throw new Error('Initial coordinates should be (0, 0)');
            }
            point.x = 5;
            point.y = 10;
            if (point.x !== 5 || point.y !== 10) {
                throw new Error('Modified values should be 5 and 10');
            }
            if (point.getCoordinates() !== '(5, 10)') {
                throw new Error('Modified coordinates should be (5, 10)');
            }
            try {
                Object.defineProperty(point, 'x', {value: 42, configurable: true});
                throw new Error('defineProperty should throw');
            } catch (e) {
                if (!(e instanceof TypeError)) {
                    throw e;
                }
            }
            """, "test.mjs").buildLiteral());
        }
    }
}
