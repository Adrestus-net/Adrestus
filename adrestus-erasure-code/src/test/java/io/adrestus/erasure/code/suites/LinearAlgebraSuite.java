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
package io.adrestus.erasure.code.suites;


import io.adrestus.erasure.code.util.linearalgebra.factory.LinearAlgebraFactorySuite;
import io.adrestus.erasure.code.util.linearalgebra.matrix.LinearAlgebraMatrixSuite;
import io.adrestus.erasure.code.util.linearalgebra.vector.LinearAlgebraVectorSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
        LinearAlgebraFactorySuite.class,
        LinearAlgebraMatrixSuite.class,
        LinearAlgebraVectorSuite.class
})
public class LinearAlgebraSuite {

    // placeholder class for inclusion of remaining test classes
}
