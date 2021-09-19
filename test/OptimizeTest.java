
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class OptimizeTest {

    @Test
    public void testQuickSort() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
        TestUtils.noErrors(result.getReports());
    }
    
    @Test
    public void testTicTacToe() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testFindMaximum() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testHelloWorld() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testLazysort() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testLife() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Life.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testMonteCarloPi() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testSimple() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Simple.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testWhileAndIF() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/WhileAndIF.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testMathUtils() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Math.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testNests() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Nests.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testFunctionsTests() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/FunctionsTests.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testLogicalOperations() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/LogicalOperations.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testWaterJug() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/WaterJug.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testFibonacci() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Fibonacci.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testAreasShapes() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/AreasShapes.jmm"));
        TestUtils.noErrors(result.getReports());
    }

    @Test
    public void testArrayManipulator() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/ArrayManipulator.jmm"));
        TestUtils.noErrors(result.getReports());
    }

}