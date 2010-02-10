package org.ict.classifier;

import junit.framework.TestCase;
import org.junit.Test;

import java.awt.*;
import java.util.HashSet;

/**
 * User: mityok
 * Date: 09.02.2010
 * Time: 0:08:18
 */
public class ClazzTest extends TestCase {

  @Test
  public void test_getColor() throws Exception {

    HashSet<Color> set = new HashSet<Color>(1000000);
    int id = 0;
    for ( ; id < 1000000; id ++ ) {
      if (!set.add(Clazz.Builder.getDefaultColor(id))) {
        System.out.println("FAIL: Color returned for id = " + id + " already passed");
        break;
      }
    }

    assertEquals(1000000, id);
  }
}
