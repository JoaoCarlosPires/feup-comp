/*
Copyright 2021 SPeCS.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License. under the License.
*/

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

public class BackendTest {
    
    @Test public void testSimple() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Simple.jmm"));
        TestUtils.noErrors(result.getReports());
    
        var output = result.run(); 
        //assertEquals("Hello, World!", output.trim()); 
    }
    
    @Test public void testWhileAndIf() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/WhileAndIf.jmm"));
        TestUtils.noErrors(result.getReports());
        
        System.out.println(result.getJasminCode());
        
        var output = result.run(); 
        //assertEquals("Hello, World!", output.trim()); 
    }
    
    @Test public void testTicTacToe() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
        TestUtils.noErrors(result.getReports());
        
        System.out.println(result.getJasminCode());
        
        var output = result.run();
        //assertEquals("Hello, World!", output.trim());
    }
    
    @Test public void testWaterJug() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/WaterJug.jmm"));
        TestUtils.noErrors(result.getReports());
        
        System.out.println(result.getJasminCode());
        
        var output = result.run(); 
        //assertEquals("Hello, World!", output.trim());
    }
    
    @Test public void testNests() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Nests.jmm"));
        TestUtils.noErrors(result.getReports());
    
        System.out.println(result.getJasminCode());
    
        var output = result.run(); 
        //assertEquals("Hello, World!", output.trim());
    }
    
    @Test public void testMonteCarloPi() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        TestUtils.noErrors(result.getReports());
    
        System.out.println(result.getJasminCode());
    
        var output = result.run(); 
        //assertEquals("Hello, World!", output.trim()); 
    }
    
    @Test public void testMath() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Math.jmm"));
        TestUtils.noErrors(result.getReports());
        
        System.out.println(result.getJasminCode());
        
        var output = result.run(); 
        //assertEquals("Hello, World!", output.trim());
    }
    
    @Test public void testLogicalOperations() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/LogicalOperations.jmm")); 
        TestUtils.noErrors(result.getReports());
    
        System.out.println(result.getJasminCode());
        
        var output = result.run(); 
        //assertEquals("Hello, World!", output.trim());
    }
    
    @Test public void testLife() { 
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Life.jmm"));
        TestUtils.noErrors(result.getReports());
        
        System.out.println(result.getJasminCode());
        
        var output = result.run();
        //assertEquals("Hello, World!", output.trim());
    }
    

    @Test
    public void testLazySort() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/LazySort.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getJasminCode());

        var output = result.run();
        // assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testHelloWorld() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getJasminCode());

        var output = result.run();
        // assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testFunctionsTests() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/FunctionsTests.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getJasminCode());

        var output = result.run();
        // assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testFindMaximum() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getJasminCode());

        var output = result.run();
        // assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testFibonacci() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Fibonacci.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getJasminCode());

        var output = result.run();
        // assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testArrayManipulator() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/ArrayManipulator.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getJasminCode());

        var output = result.run();
        // assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testAreasShapes() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/AreasShapes.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getJasminCode());

        var output = result.run();
        // assertEquals("Hello, World!", output.trim());
    }

}
