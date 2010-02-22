package org.ict.clusterizer;

/**
 * Clustering algorithms collection
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 17:38:16
 */
public final class Clusterizers {

  private Clusterizers() {
    throw new IllegalStateException("Not instantiable");
  }

  public static Clusterizer createMeanSCClusterizer(ClusteringTask task) throws ClusterizerException {

    return new MeanSCClusterizer();
  }

}
