package testrunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import bll.EditorBOBoundaryTest;
import bll.SearchWordBoundaryTest;

/**
 * Business Logic Layer Test Suite
 * Tests all BLL components for boundary and limit conditions
 */
@RunWith(Suite.class)
@SuiteClasses({
    EditorBOBoundaryTest.class,
    SearchWordBoundaryTest.class
})
public class BLLTestSuite {
    // Empty class - suite configuration only
}
