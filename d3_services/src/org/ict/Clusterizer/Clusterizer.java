package org.ict.clusterizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for unsupervized clustering algorithms (for numerical data).
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 15:32:04
 */
public interface Clusterizer {

  /**
   * Returns clustering task (set of parameters)suitable for this algorithm.
   * 
   * @return expected clustering task
   */
  public boolean canProcess(ClusteringTask task);

  /**
   * Clusterizes set of points.
   *
   * @param points - set of points to be clusterized
   * @param task - clustering task (algorithm parameters)
   * @return discovered clusters
   */
  public ArrayList<Cluster> apply(ArrayList<Point> points, ClusteringTask task);

}
