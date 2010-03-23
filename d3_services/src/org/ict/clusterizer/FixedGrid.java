package org.ict.clusterizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashSet;

/**
 * User: Yorik
 * Date: 12.03.2010
 * Time: 12:17:10
 */
public class FixedGrid {
  private final double RANGE = 256.0d;
  private ArrayList<Cell> cells;
  private int dimension;
  private int dimensionCellsCount;
  private double influenceDistance;

  public FixedGrid(ArrayList<Point> points, int step) {
    if (points == null)
      throw new IllegalArgumentException("Null parameter not allowed");

    dimension = points.get(0).getDim();
    dimensionCellsCount = (int)Math.ceil(RANGE / step);
    influenceDistance = (double)step / 2.0d;

    /*
    Creating grid
     */
    int cellsCount = (int)Math.pow(dimensionCellsCount, dimension);
    cells = new ArrayList<Cell>(cellsCount);
    for (int i = 0; i < cellsCount; i++)
      cells.add(new Cell());

    /*
    Inserting points into cells
     */
    for (Point p : points)
      getContainingCell(p).addPoint(p);
    
    /*
    Connecting neighboring cells
     */
    // Preparing shifts
    int tmp;
    int tmpValue;
    ArrayList<Integer> numbers = new ArrayList<Integer>((int)Math.pow(3.0d, dimension));
    numbers.add(0);
    for (int d = 0; d < dimension; d++) {
      tmp = (int)Math.pow(dimensionCellsCount, d);
      tmpValue = numbers.size();
      for (int j = 0; j < tmpValue; j++) {
        numbers.add(numbers.get(j) + tmp);
        numbers.add(numbers.get(j) - tmp);
      }
    }
    // Connecting
    for (int i = 0; i < cellsCount; i++) {
      if (cells.get(i).isEmpty())
        continue;
      for (int addon : numbers) {
        tmp = i + addon;
        if (tmp >= 0 && tmp < cellsCount && !(cells.get(tmp).isEmpty()))
          cells.get(i).addNeighboringCell(cells.get(tmp));
      }
    }
  }

  /**
   * Returns estimated density value in <point>.
   *
   * @param point - where to estimate density
   * @return - estimated density value (window size is equal to the cell size)
   */
  public double estimateDensity(Point point) {
    /*
    TODO: Density core should be selected HERE and nowhere else! <DensityEstimates> is just a core collection.
     */
    ArrayList<Point> nearestPoints = getContainingCell(point).getNearestPoints(point);
    if (nearestPoints == null) return 0.0d;
    double density = 0.0d;
    for (Point p : nearestPoints)
      density += DensityEstimates.MultiplicableTriangleCore(p, point, influenceDistance);

    return density;
  }

  private Cell getContainingCell(Point point) {
    int index = 0;
    int tmpVal;

    for (int d = point.getDim() - 1; d >= 0; d--) {
      tmpVal = (int) Math.floor(point.get(d) * dimensionCellsCount / RANGE);
      if (tmpVal >= dimensionCellsCount) tmpVal--;
      index = index * dimensionCellsCount + tmpVal;
    }

    return cells.get(index);
  }

  public Point getMean(Point point) {
    return Point.mean(getContainingCell(point).getNearestPoints(point));
  }

  public LinkedList<Cluster> applyMeanShiftProcedure(int minWeight) {
    LinkedList<Cluster> clusters = new LinkedList<Cluster>();

    // Calculating weight centers of cells containing more then <minWeight> points
    int clustersCount = 0;
    for (Cell c : cells) {
      if (!c.isFilled(minWeight)) continue;

      clusters.add(new Cluster(c.getPoints()));
      c.setDescendant(clusters.get(clustersCount));
      Point m = Point.mean(c.getPoints());
      clusters.get(clustersCount).setDelegate(m);
      clusters.get(clustersCount).setDelegateDensity(estimateDensity(clusters.get(clustersCount).getDelegate()));
      clustersCount++;
    }

    // Assigning each point to the center of cluster representing point's cell
    for (Cell c : cells) {
      if (c.isFilled(minWeight)) {
        for (Point p : c.getPoints()) {
          p.setDelegateDensity(c.getDescendant().getDelegateDensity());
        }
      }
    }

    // Reassigning points to close enough weight center with highest density
    for (Cell c : cells) {
      if (c.isEmpty() || !c.isFilled(minWeight)) continue;

      for (Cell neighbor : c.getNeighboringCells()) {
        if (neighbor == c) continue;
        for (Point p : neighbor.getPoints()) {
          if (p.getDelegateDensity() < c.getDescendant().getDelegateDensity() && Point.distance(p, c.getDescendant().getDelegate()) <= influenceDistance) {
            c.getDescendant().addPoint(p);
            if(p.getCluster()!= null) p.getCluster().removePoint(p);
          }
        }
      }
    }

    // Applying mean shift procedure to each cluster separately
    Point nextStep;
    double nextStepDensity = 0.0d;
    for (Cluster cl : clusters) {
      nextStep = getMean(cl.getDelegate());
      nextStepDensity = estimateDensity(nextStep);
      // While next step density is higher then current...
      while (nextStepDensity > cl.getDelegateDensity()) {
        // Moving delegate of current cluster
        cl.setDelegate(nextStep, nextStepDensity);
        // Trying to reassign points close enough to new delegate
        for (Point p : getContainingCell(nextStep).getNearestPoints(nextStep)) {
          if (p.getDelegateDensity() < nextStepDensity) {
            p.setDelegateDensity(nextStepDensity);
            if (cl == p.getCluster()) continue;
            if (p.getCluster() != null) p.getCluster().removePoint(p);
            cl.addPoint(p);
          }
        }
        // Calculating next step of mean shift procedure
        nextStep = getMean(cl.getDelegate());
        nextStepDensity = estimateDensity(nextStep);
      }
    }

    // Connecting close clusters
    for (Cluster cl : clusters) {
      if (cl.getWeight() == 0) continue;
      for (Cluster cl1 : clusters) {
        if (cl1.getWeight() == 0) continue;
        if (!cl.equals(cl1) && Point.distance(cl.getDelegate(), cl1.getDelegate()) <= influenceDistance) {
          if (cl1.getDelegateDensity() > cl.getDelegateDensity()) {
            cl1.merge(cl);
          }
          else {
            cl.merge(cl1);
          }
        }
      }
    }

    // Removing empty clusters
    for (int i = clusters.size() - 1; i >= 0; i--) {
      if (clusters.get(i).getWeight() == 0)
        clusters.remove(i);
    }

    return clusters;
  }

  /*
  If neighboring cells (or one cell) contain different clusters these clusters should be checked for connectivity
   */
  public void connectClusters (LinkedList<Cluster> clusters, double unionThreshold) {
    HashSet<ClusterPare> pares = new HashSet<ClusterPare>();
    HashSet<Cluster> met = new HashSet<Cluster>();

    // Searching for neighboring clusters
    for (Cell center : cells){
      if (center.isEmpty()) continue;
      met.clear();
      for (Cell neighbor : center.getNeighboringCells()) {
        for (Point p : neighbor.getPoints()) {
          if (p.getDelegateDensity() == 0.0d) continue;
          met.add(p.getCluster());
        }
      }
      // Less then two clusters met in neighboring cells => nothing to check for connectivity
      if (met.size() < 2) continue;
      for (Cluster cl : met) {
        for (Cluster cl1 : met) {
          if (cl == cl1) continue;
          pares.add(new ClusterPare(cl, cl1));
        }
      }
    }

    // Checking found cluster pares for connectivity
    Cluster from;
    Cluster to;
    double maxWayDensity;
    Point shift;
    Point current;
    int iterationCount;
    boolean isHole;

    for (ClusterPare cp : pares) {

      // TODO: !!!some clusters are already connected to others and have NULL delegate!!!

      // Getting moving direction (from lower to higher density)
      if (cp.getOne().getDelegateDensity() <= cp.getTwo().getDelegateDensity()) {
        from = cp.getOne();
        to = cp.getTwo();
      } else {
        to = cp.getOne();
        from = cp.getTwo();
      }

      // Checking for dense way from one cluster to another
      isHole = false;
      maxWayDensity = from.getDelegateDensity();
      iterationCount = (int)Math.ceil(Point.distance(from.getDelegate(), to.getDelegate()) / influenceDistance);
      shift = Point.getDirection(from.getDelegate(), to.getDelegate(), influenceDistance);
      current = from.getDelegate();
      for (int i = 1; i < iterationCount && !isHole; i++) {
        // Moving along the line connecting cluster centers
        current = Point.move(current, shift);
        // Estimating density in current point
        current.setDelegateDensity(estimateDensity(current));
        // Searching maximum density along the way
        if (maxWayDensity < current.getDelegateDensity()) {
          maxWayDensity = current.getDelegateDensity();
          continue;
        }
        // Checking for density hole
        isHole = (maxWayDensity > unionThreshold * current.getDelegateDensity());
      }
      // No hole found => connecting clusters
      if (!isHole)
        from.connectTo(to);
    }

    // Connecting clusters
    for (Cluster cl : clusters) {
      if (cl.getNext() == null) continue;
      to = cl.getNext();
      while (to.getNext() != null)
        to = to.getNext();
      to.merge(cl);
    }

    // Removing empty clusters
    for (int i = clusters.size() - 1; i >= 0; i--) {
      if (clusters.get(i).getNext() != null)
        clusters.remove(i);
    }
  }


  private class Cell {
    private ArrayList<Point> points;
    private int weight;
    private ArrayList<Cell> neighbors;
    private Cluster descendant;

    public Cell() {
      points = null;
      weight = 0;
      neighbors = null;
      descendant = null;
    }

    public Cluster getDescendant() {
      return descendant;
    }

    public void setDescendant(Cluster descendant) {
      this.descendant = descendant;
    }

    public ArrayList<Cell> getNeighboringCells() {
      return neighbors;
    }

    public void addPoint(Point point) {
      if (point == null)
        throw new IllegalArgumentException("Null parameter not allowed");

      try {
      points.add(point);
      weight += point.getWeight();
      } catch (NullPointerException e) {
        points = new ArrayList<Point>(100);
        addPoint(point);
      }
    }

    public boolean isEmpty() {
      return (points == null)
          || (weight <= 0);
    }

    public boolean isFilled(int minWeight) {
      return (points != null)
          && (weight >= minWeight);
    }

    public int getWeight() {
      return weight;
    }

    public ArrayList<Point> getPoints() {
      return points;
    }

    public void addNeighboringCell(Cell cell) {
      if (cell == null)
        throw new IllegalArgumentException("Null parameter not allowed");

      try {
        neighbors.add(cell);
      } catch (NullPointerException e) {
        neighbors = new ArrayList<Cell>((int)Math.pow(3.0d, dimension));
        addNeighboringCell(cell);
      }
    }

    public ArrayList<Point> getNearestPoints(Point point) {
      if (neighbors == null) return null;
      
      ArrayList<Point> result = new ArrayList<Point>(100);

      for (Cell c : neighbors) {
        for (Point p : c.getPoints()) {
          if (Point.distance(point, p) <= influenceDistance)
            result.add(p);
        }
      }

      return result;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Points count = ").append(points == null ? "0" : points.size());
      sb.append(", Neighbors count = ").append(neighbors == null ? "0" : neighbors.size());

      return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (!(o instanceof Cell)) return false;

      return points.equals(((Cell) o).getPoints());
    }
  }


  private class ClusterPare {
    private Cluster one;
    private Cluster two;

    public ClusterPare(Cluster one, Cluster two) {
      this.one = one;
      this.two = two;
    }

    public Cluster getOne() {
      return one;
    }

    public void setOne(Cluster one) {
      this.one = one;
    }

    public Cluster getTwo() {
      return two;
    }

    public void setTwo(Cluster two) {
      this.two = two;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ClusterPare)) return false;

      ClusterPare cp = (ClusterPare) o;
      if (getOne().equals(cp.getOne()) && getTwo().equals(cp.getTwo())) return true;
      if (getTwo().equals(cp.getOne()) && getOne().equals(cp.getTwo())) return true;

      return false;
    }

    @Override
    public int hashCode() {
      return getOne().getDelegate().hashCode() + getTwo().getDelegate().hashCode();
    }
  }
}
