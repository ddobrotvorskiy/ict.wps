package org.ict.clusterizer;

import java.awt.Color;
import java.util.List;
import java.util.LinkedList;


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

  private Cluster connectTo;

  public Cluster(){
    points = new LinkedList<Point>();
    weight = 0;
    dimension = -1;
    delegate = null;
    delegateDensity = 0.0d;
    connectTo = null;
  }

  public Cluster(List<Point> points){
    this();
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

  public void connectTo(Cluster connectTo) {
    this.connectTo = connectTo;
  }

  public Cluster getNext() {
    return connectTo;
  }

  public Point getDelegate() {
    return delegate;
  }

  public double getDelegateDensity() {
    return delegateDensity;
  }

  public void setDelegate(Point delegate) {
    this.delegate = Point.create(delegate.getArray(), delegate.getWeight(), delegate.getXPos(), delegate.getYPos());
  }

  public void setDelegateDensity(double density) {
    this.delegateDensity = density;
  }

  public void setDelegate(Point delegate, double density) {
    setDelegate(delegate);
    setDelegateDensity(density);
  }

  public void clear() {
    points.clear();
    weight = 0;
  }

  public void merge(Cluster from) {
    addPoints(from.getPoints());
    for (Point p : from.getPoints())
      p.setCluster(this);
    if (getDelegateDensity() < from.getDelegateDensity())
      setDelegate(from.getDelegate(), from.getDelegateDensity());
    from.clear();
    from.connectTo(this);
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

    if (dimension == -1) {
      dimension = point.getDim();
    } else {
      if (dimension != point.getDim())
        throw new IllegalArgumentException("All points should have the same dimension");
    }
    weight += point.getWeight();
    point.setDelegateDensity(delegateDensity);
    point.setCluster(this);
    points.add(point);
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

    if (points != null && points.remove(point))
      weight -= point.getWeight();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || ! (o instanceof Cluster)) return false;
    return delegate.equals(((Cluster) o).getDelegate());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Cluster { Weight = ").append(weight).append(" ").append('\'');
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
