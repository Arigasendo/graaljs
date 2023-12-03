/*
 * Copyright (c) 2023, 2023, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.js.scriptengine.test;

import static org.junit.Assert.assertEquals;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class GR49209 {

    private static ScriptEngine getEngine(boolean nashornCompat) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(TestEngine.TESTED_ENGINE_NAME);
        if (nashornCompat) {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.nashorn-compat", true);
        }
        return engine;
    }

    @Test
    public void testCollectionsMin() throws ScriptException {
        ScriptEngine engine = getEngine(true);
        Object result = engine.eval("java.util.Collections.min([42, 211]);");
        assertEquals(42, ((Number) result).intValue());
    }

    @Test
    public void testSetAddAll() throws ScriptException {
        ScriptEngine engine = getEngine(true);
        String code = """
                        var set = new java.util.HashSet();
                        set.addAll(['foo', 'bar']);
                        set.size();
                        """;
        Object result = engine.eval(code);
        assertEquals(2, ((Number) result).intValue());
    }

    @Test
    public void testArray() throws ScriptException {
        ScriptEngine engine = getEngine(true);
        // Check that as Collection conversion applies to Java arrays.
        String code = """
                        var ObjectArray = Java.type('java.lang.Object[]');
                        var array = new ObjectArray(3);
                        java.util.Collections.unmodifiableCollection(array).size();
                        """;
        Object result = engine.eval(code);
        assertEquals(3, ((Number) result).intValue());
    }

    @Test
    public void testJavaMapEntry() throws ScriptException {
        ScriptEngine engine = getEngine(true);
        // Check that as Collection conversion applies to Map.Entry (that hasArrayElements()).
        String code = """
                        var entry = java.util.Map.of('key', 'value').entrySet().iterator().next();
                        java.util.Collections.unmodifiableCollection(entry).size();
                        """;
        Object result = engine.eval(code);
        assertEquals(2, ((Number) result).intValue());
    }

}
