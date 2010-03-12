package org.ict.clusterizer;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ArrayList;


/**
 * User: Yorik
 * Date: 22.02.2010
 * Time: 13:24:27
 */
public class Cluster {

  private static final Color[] defaultColors = {Color.blue, Color.green, Color.red, Color.yellow, Color.cyan, Color.DARK_GRAY, Color.magenta};

  private LinkedList<Point> points;

  private int weight;
  private int dimension;

  private Point delegate;
  private double delegateDensity;

  public Cluster(){
    points = new LinkedList<Point>();
    weight = 0;
    dimension = -1;
    delegate = null;
    delegateDensity = 0.0d;
  }

  public Cluster(Iterable<Point> points){
    addPoints(points);
  }

  public LinkedList<Point> getPoints() {
    return points;
  }

  public int getWeight() {
    return weight;
  }

  public int getDimension() {
    return dimension;
  }

  public Point getDelegate() {
    return delegate;
  }

  public double getDelegateDensity() {
    return delegateDensity;
  }

  public void setDelegate(Point delegate, double density) {
    this.delegate = delegate;
    this.delegateDensity = density;
  }

  public void clear() {
    points.clear();
    delegate = null;
    delegateDensity = 0.0d;
  }

  public void merge(Cluster from) {
    addPoints(from.getPoints());
    if (getDelegateDensity() < from.getDelegateDensity())
      setDelegate(from.getDelegate(), from.getDelegateDensity());
    from.clear();
  }

  public void addPoints(Iterable<Point> pts) {
    if (pts == null)
      throw new IllegalArgumentException("Null argument not allowed");

    for (Point p : pts) {
      addPoint(p);
    }
  }

  public void addPoint(Point point) {
    if (point == null)
      throw new IllegalArgumentException("Null argument not allowed");

    points.add(point);
    if (dimension == -1) {
      dimension = point.getDim();
    } else {
      if (dimension != point.getDim())
        throw new IllegalArgumentException("All points should have the same dimension");
    }
    weight += point.getWeight();
  }

  /**
   * Returns index of point in the cluster.
   * Returns -1 if cluster doesn't contain this point.
   *
   * @param point
   */
  public int indexOf(Point point){
    if (point == null)
      throw new IllegalArgumentException("Null argument not allowed");

    if (points == null) return -1;
    return points.indexOf(point);
  }

  public void removePoint(Point point){
    if (point == null)
      throw new IllegalArgumentException("Null argument not allowed");

    if (points != null && points.contains(point)) {
      weight -= point.getWeight();
      points.remove(point);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || ! (o instanceof Cluster)) return false;

    Cluster c = (Cluster) o;

    return weight == c.weight &&
           dimension == c.dimension &&
           points.equals(c.points);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Cluster { ").append('\'');
    builder.append(", points= [");
    for (Point p : points) {
      builder.append('\n').append(p);
    }
    builder.append("] }");
    return builder.toString();
  }

  /**
   * Порождает последовательность цветов, различных для каждого id для id от 0 до 1000000
   *
   * @param classId clazz id
   * @return Color unique for classId between 0..1000000
   */
  public static Color getDefaultColor(int classId) {
    int delta = 0x16A56F4E; // Число от балды
    return new Color(0x00FFFFFF & (defaultColors[classId % defaultColors.length].getRGB() + (classId / defaultColors.length) * delta));
  }
}
