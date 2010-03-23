package org.ict.clusterizer;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Interface for unsupervized clustering algorithms (for numerical data).
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 15:32:04
 */
public interface Clusterizer {

  /**
   * Checks if the algorithm could process passed task.
   */
  public boolean canProcess(ClusteringTask task);

  /**
   * Clusterizes set of points.
   *
   * @param points - set of points to be clusterized
   * @param task - clustering task (algorithm parameters)
   * @return clusters discovered (the last cluster contains all noisy points)
   */
  public LinkedList<Cluster> apply(ArrayList<Point> points, ClusteringTask task, Logger LOG);

}
