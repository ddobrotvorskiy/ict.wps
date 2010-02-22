package org.ict.clusterizer;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Test for comparing clustering tasks
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 16:27:34
 */
public class ClusteringTaskTest extends TestCase {

  @Test
  public void test_compartible() throws Exception {

    ClusteringTask one = new ClusteringTask(3);
    one.addParameter(new ClusteringParameter("par1",0.0));
    one.addParameter(new ClusteringParameter("par2",1.0));
    one.addParameter(new ClusteringParameter("par3",2.0));

    ClusteringTask two = new ClusteringTask(3);
    two.addParameter(new ClusteringParameter("par1",0.0));
    two.addParameter(new ClusteringParameter("par3",1.0));
    two.addParameter(new ClusteringParameter("par2",2.0));

    assertTrue(!one.equals(two) && one.isCompartible(two));
  }

  @Test
  public void test_incompartible() throws Exception {

    ClusteringTask one = new ClusteringTask(3);
    one.addParameter(new ClusteringParameter("par1",0.0));
    one.addParameter(new ClusteringParameter("par2",1.0));
    one.addParameter(new ClusteringParameter("par3",2.0));

    ClusteringTask two = new ClusteringTask(2);
    two.addParameter(new ClusteringParameter("par1",0.0));
    two.addParameter(new ClusteringParameter("par2",1.0));

    assertFalse(one.isCompartible(two));
  }

  @Test
  public void test_different() throws Exception {

    ClusteringTask one = new ClusteringTask(3);
    one.addParameter(new ClusteringParameter("par1",0.));
    one.addParameter(new ClusteringParameter("par2",1.));
    one.addParameter(new ClusteringParameter("par3",2.));

    ClusteringTask two = new ClusteringTask(3);
    two.addParameter(new ClusteringParameter("par1",10.));
    two.addParameter(new ClusteringParameter("par2",1.));
    two.addParameter(new ClusteringParameter("par3",2.));
    
    assertTrue(one.isCompartible(two));
  }
}
