package testrunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import bll.EditorBOBoundaryTest;
import bll.SearchWordBoundaryTest;
import dal.EditorDBDAOBoundaryTest;
import dal.PMICalculatorBoundaryTest;
import dal.PKLCalculatorBoundaryTest;
import dal.TFIDFCalculatorBoundaryTest;
import integration.LayerIntegrationBoundaryTest;

/**
 * Master Test Suite - Runs all boundary and limit condition tests
 * 
 * Test Categories:
 * 1. Business Logic Layer (BLL) - 2 test classes
 * 2. Data Access Layer (DAL) - 4 test classes
 * 3. Integration Tests - 1 test class
 * 
 * Total: 7 test classes, 200+ individual test methods
 */
@RunWith(Suite.class)
@SuiteClasses({
    // BLL Tests
    EditorBOBoundaryTest.class,
    SearchWordBoundaryTest.class,
    
    // DAL Tests
    EditorDBDAOBoundaryTest.class,
    TFIDFCalculatorBoundaryTest.class,
    PMICalculatorBoundaryTest.class,
    PKLCalculatorBoundaryTest.class,
    
    // Integration Tests
    LayerIntegrationBoundaryTest.class
})
public class AllBoundaryTestsSuite {
    // This class remains empty, used only as a holder for the above annotations
}
