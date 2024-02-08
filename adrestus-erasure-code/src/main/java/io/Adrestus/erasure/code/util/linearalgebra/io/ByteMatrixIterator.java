/*
 * Copyright 2014 OpenRQ Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
 *
 * This file is part of la4j project (http://la4j.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributor(s): -
 */
package io.Adrestus.erasure.code.util.linearalgebra.io;

public abstract class ByteMatrixIterator extends CursorIterator {

    protected final int rows;
    protected final int columns;


    public ByteMatrixIterator(int rows, int columns) {

        this.rows = rows;
        this.columns = columns;
    }

    public abstract int rowIndex();

    public abstract int columnIndex();

    public ByteMatrixIterator orElseAdd(final ByteMatrixIterator those) {

        return new CursorToMatrixIterator(super.orElse(those, JoinFunction.ADD), rows, columns);
    }

    public ByteMatrixIterator orElseSubtract(final ByteMatrixIterator those) {

        return new CursorToMatrixIterator(super.orElse(those, JoinFunction.SUB), rows, columns);
    }

    public ByteMatrixIterator andAlsoMultiply(final ByteMatrixIterator those) {

        return new CursorToMatrixIterator(super.andAlso(those, JoinFunction.MUL), rows, columns);
    }

    public ByteMatrixIterator andAlsoDivide(final ByteMatrixIterator those) {

        return new CursorToMatrixIterator(super.andAlso(those, JoinFunction.DIV), rows, columns);
    }

    @Override
    protected int cursor() {

        return rowIndex() * columns + columnIndex();
    }
}
