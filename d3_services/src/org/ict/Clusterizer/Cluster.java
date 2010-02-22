package org.ict.clusterizer;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


/**
 * User: Yorik
 * Date: 22.02.2010
 * Time: 13:24:27
 */
public class Cluster {

  private static final Color[] defaultColors = {Color.blue, Color.green, Color.red, Color.yellow, Color.cyan, Color.DARK_GRAY, Color.magenta};

  private int id;
  private List<Point> points;
  private Color color;

  private int weight;
  private int dimension;

  public Cluster(int id){
    this.id = id;
    points = new ArrayList<Point>(100);
    weight = 0;
    dimension = -1;
  }

  public Cluster(int id, Iterable<Point> points){
    this(id);
    addPoints(points);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<Point> getPoints() {
    return Collections.unmodifiableList(points);
  }

  public Color getColor() {
    return color;
  }

  public int getWeight() {
    return weight;
  }

  public int getDimension() {
    return dimension;
  }

  public void addPoints(Iterable<Point> points) {
    if (points == null)
      throw new IllegalArgumentException("Null argument not allowed");

    for (Point p : points) {
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

    points.remove(point);
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public boolean deepEquals(Object o) {
    if (!equals(o))
      return false;

    Cluster c = (Cluster) o;

    return id == c.id &&
           weight == c.weight &&
           dimension == c.dimension &&
           (color == null ? c.color == null : color.equals(c.color)) &&
           points.equals(c.points);
  }

  @Override
  public int hashCode() {
    return id;
  }
 
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || ! (o instanceof Cluster)) return false;

    Cluster cluster = (Cluster) o;

    return id == cluster.id;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Cluster { ").append("id=").append(id).append('\'')
            .append(", color=").append(color);
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
