package org.ict.clusterizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * MeanSC clustering algorithm.
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 15:45:52
 */
public class MeanSCClusterizer implements Clusterizer {
  public static final String CELL_SIZE = "H";
  public static final String N_MIN = "N_MIN";
  public static final String THRESHOLD = "T";

  private int cellSize;
  private int nMin;
  private double unionThreshold;

  private FixedGrid grid;

  public MeanSCClusterizer(){
    cellSize = 0;
    nMin = 0;
    unionThreshold = 0.0d;
    grid = null;
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
  public LinkedList<Cluster> apply(ArrayList<Point> points,  ClusteringTask task){
    if (points == null || task == null)
      throw new IllegalArgumentException("Null argument not allowed");
    if (points.isEmpty())
      throw new IllegalArgumentException("At least one point required for clustering");

    /*
    Parsing clustering task
     */
    List<ClusteringParameter> pars = task.getParameters();
    for (ClusteringParameter p : pars) {
      if (CELL_SIZE.equals(p.getName())) cellSize = (int)p.getValue();
      if (N_MIN.equals(p.getName())) nMin = (int)p.getValue();
      if (THRESHOLD.equals(p.getName())) unionThreshold = p.getValue();
    }

    /*
    Creating grid
     */
    grid = new FixedGrid(points, cellSize);

    /*
    Applying mean shift procedure
     */
    LinkedList<Cluster> clusters = grid.applyMeanShiftProcedure(nMin);

    /*
    Connecting clusters through areas with high density
     */
    grid.connectClusters(unionThreshold);

    /*
    Collecting noise into one cluster
     */
    Cluster cl = new Cluster();
    for (Point p : points) {
      if (p.getDelegateDensity() == 0.0d)
        cl.addPoint(p);
    }
    clusters.add(cl);

    return clusters;
  }
}
