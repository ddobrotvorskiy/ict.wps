package org.ict.clusterizer;

import java.util.ArrayList;
import java.util.LinkedList;

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

  public FixedGrid(ArrayList<Point> points, int step) {
    if (points == null)
      throw new IllegalArgumentException("Null parameter not allowed");

    dimension = points.get(0).getDim();
    dimensionCellsCount = (int)(RANGE / step);

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
    ArrayList<Point> nearestPoints = getContainingCell(point).getNearestPoints(point, RANGE / dimensionCellsCount);

    /*
    TODO: Density core should be selected HERE and nowhere else! <DensityEstimates> is just a core collection.
     */
    double density = 0.0d;
    for (Point p : nearestPoints)
      density += DensityEstimates.MultiplicableTriangleCore(p, point, RANGE / dimensionCellsCount);

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

  public Point getMean(Point point, double radius) {
    return Point.mean(getContainingCell(point).getNearestPoints(point, radius));
  }


  private class Cell {
    private ArrayList<Point> points;
    private int weight;
    private ArrayList<Cell> neighbors;

    public Cell() {
      points = null;
      weight = 0;
      neighbors = null;
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
          || (weight > 0);
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

    public ArrayList<Point> getNearestPoints(Point point, double maxDistance) {
      ArrayList<Point> result = new ArrayList<Point>(100);

      for (Point p : points) {
        if (Point.distance(point, p) <= maxDistance)
          result.add(p);
      }
      for (Cell c : neighbors) {
        for (Point p : c.getPoints()) {
          if (Point.distance(point, p) <= maxDistance)
            result.add(p);
        }
      }

      return result;
    }
  }
}
