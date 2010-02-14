package org.ict.classifier;

import Jama.Matrix;
import junit.framework.TestCase;
import org.ict.classifier.util.NormalDistribution;
import org.junit.Test;

/**
 * User: mityok
 * Date: 14.02.2010
 * Time: 23:19:08
 */
public class PointTest extends TestCase {

  @Test
  public void testRecount() throws Exception {
    Matrix m = new Matrix(2, 1, 0);
    m.set(0, 0, 1);

    Point p = Point.create(new double[] {2, 3} );
    Point expected = Point.create(new double[] {2} );
    Point actual = p.recount(m);
        
    System.out.println("Original = " + p);
    System.out.println("Expected = " + expected);
    System.out.println("Actual   = " + actual);

    assertEquals("", expected, actual);
  }

}
