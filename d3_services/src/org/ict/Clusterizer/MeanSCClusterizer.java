package org.ict.clusterizer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * MeanSC clustering algorithm.
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 15:45:52
 */
public class MeanSCClusterizer implements Clusterizer {
  public static final String CELL_SIZE = "cell size";
  public static final String N_MIN = "minimum points count";
  public static final String THRESHOLD = "union threshold";

  public MeanSCClusterizer(){
  }

  @Override
  public boolean canProcess(ClusteringTask task){
    ClusteringTask expectedTask = new ClusteringTask(3);

    expectedTask.addParameter(new ClusteringParameter(CELL_SIZE,0.));
    expectedTask.addParameter(new ClusteringParameter(N_MIN,0.));
    expectedTask.addParameter(new ClusteringParameter(THRESHOLD,0.));

    return task.isCompartible(expectedTask);
  }

  @Override
  public ArrayList<Cluster> apply(ArrayList<Point> points,  ClusteringTask task){
    if (points == null || task == null)
      throw new IllegalArgumentException("Null argument not allowed");
    if (points.isEmpty())
      throw new IllegalArgumentException("At least one point required for clustering");

    ArrayList<Cluster> clusters = new ArrayList<Cluster>();

    return clusters;
  }
}
