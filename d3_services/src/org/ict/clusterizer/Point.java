package org.ict.clusterizer;

import java.util.Arrays;
import java.util.List;

/**
 * Cluster point with image coordinates.
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 17:14:48
 */
public class Point {

  private final double[] data;
  private final int weight;

  private double delegateDistance;
  private double delegateDensity;

  private final int xPos;
  private final int yPos;

  private Point(double [] data, int weight, int xPos, int yPos) {
    if (data == null || weight < 1 || xPos < 0 || yPos < 0)
      throw new IllegalArgumentException();

    this.weight = weight;
    this.data = data;
    this.xPos = xPos;
    this.yPos = yPos;
    this.delegateDistance = Double.MAX_VALUE;
    this.delegateDensity = 0.0d;
  }

  public static Point create(double [] data) {
    return create(data, 1, -1, -1);
  }

  public static Point create(double [] data, int weight, int xPos, int yPos) {

    double [] a = new double[data.length];
    System.arraycopy(data, 0, a, 0, data.length);

    return new Point(a, weight, xPos, yPos);
  }

  public double[] getArray() {
    double [] a = new double[data.length];
    System.arraycopy(data, 0, a, 0, data.length);
    return a;
  }

  public int getDim() {
    return data.length;
  }

  public int getWeight() {
    return weight;
  }

  public double get(int i) {
    return data[i];
  }

  public int getXPos(){
    return xPos;
  }

  public int getYPos(){
    return yPos;
  }

  public double getDelegateDistance() {
    return delegateDistance;
  }

  public double getDelegateDensity() {
    return delegateDensity;
  }

  public void setDelegateDistance(double distance) {
    this.delegateDistance = distance;
  }

  public void setDelegateDensity(double density) {
    this.delegateDensity = density;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append('[');
    for (double d : data)
      s.append(d).append(' ');
    s.append("; weight = ").append(weight).append(']');
    s.append("; position = (").append(xPos).append(" ").append(yPos).append(")]");
    return s.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if ( ! (o instanceof Point)) return false;

    Point point = (Point) o;

    if (weight != point.getWeight() || xPos != point.getXPos() || yPos != point.getYPos()) return false;
    if (!Arrays.equals(data, point.data)) return false;

    return true;
  }

  /**
   * Euclidean distance between two points p1, p2.
   * Points should have the same dimension.
   * Points should be not null.
   *
   * @param p1 point
   * @param p2 point
   * @return euclidean distance between p1 and p2
   * @throws IllegalArgumentException if points have different dimension
   */
  public static double distance(Point p1, Point p2) {
    if (p1 == p2)
      return 0.0d;

    if (p1.data.length != p2.data.length)
      throw new IllegalArgumentException("Incomparable points: " + p1 + "  " + p2);

    double d = 0.0d;
    for (int i = 0; i < p1.data.length; i++ )
      d += Math.pow(p1.data[i] - p2.data[i], 2.0d);

    return Math.sqrt(d);
  }

  /**
   * Returns mean point of passed points.
   * Weight of result is equal to passed points count.
   *
   * points should not be null.
   * points should not be empty.
   * all passed points should have equal dimension
   *
   *
   * @param points list of points
   * @return mean of passed point with weight equal to points.size()
   * @throws IllegalArgumentException if points is empty
   *         IllegalArgumentException if passed points have different dimension
   */
  public static Point mean(List<Point> points) {
    if (points.isEmpty())
      throw new IllegalArgumentException("Argument must be non-empty list");

    if (points.size() == 1)
      return points.get(0);

    int dim = points.get(0).getDim();
    int weight = 0;
    double [] data = new double[dim];

    for (int i = 0; i< dim; i++)
      data[i] = 0;

    for (Point p : points) {
      if (p.getDim() != dim)
        throw new IllegalArgumentException("All points should have equal dimension");

      weight += p.weight;
      for (int i = 0; i< dim; i++)
        data[i] += p.data[i];
    }

    for (int i = 0; i< dim; i++)
      data[i] /= (double) points.size();

    return new Point(data, weight, -1, -1);
  }

}
