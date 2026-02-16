package testrunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import dal.EditorDBDAOBoundaryTest;
import dal.PMICalculatorBoundaryTest;
import dal.PKLCalculatorBoundaryTest;
import dal.TFIDFCalculatorBoundaryTest;

/**
 * Data Access Layer Test Suite
 * Tests all DAL components for boundary and limit conditions
 */
@RunWith(Suite.class)
@SuiteClasses({
    EditorDBDAOBoundaryTest.class,
    TFIDFCalculatorBoundaryTest.class,
    PMICalculatorBoundaryTest.class,
    PKLCalculatorBoundaryTest.class
})
public class DALTestSuite {
    // Empty class - suite configuration only
}
