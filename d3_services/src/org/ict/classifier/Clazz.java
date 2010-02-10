package org.ict.classifier;

import Jama.Matrix;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * Clazz is IMMUTABLE description of class in supervised classification task.
 * Id`s of classes in task should be distinct
 * points - is training sample
 *
 *
 * User: mityok
 * Date: 24.01.2010
 * Time: 20:55:14
 */
public final class Clazz {

  private static final Color[] defaultColors = {Color.blue, Color.green, Color.red, Color.yellow, Color.cyan, Color.DARK_GRAY, Color.magenta};

  private final int id;
  private final List<Point> points;
  private final String legend;
  private final Color color;

  private final int weight;
  private final int dimension;

  private Clazz(Builder builder) {
    id = builder.id;
    points = new ArrayList<Point>(builder.points);
    legend = builder.legend;
    color = builder.color;
    weight = builder.weight;
    dimension = builder.dimension;
  }

  public int getId() {
    return id;
  }

  public List<Point> getPoints() {
    return Collections.unmodifiableList(points);
  }

  public String getLegend() {
    return legend;
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

  Clazz recountInNewFeatures(Matrix features) {

    Builder builder = new Builder(id, points.size());
    builder.setColor(color);
    builder.setLegend(legend);

    for (Point p : points) {
      builder.addPoint(p.recount(features));
    }

    return new Clazz(builder);
  }

  public boolean deepEquals(Object o) {
    if (!equals(o))
      return false;

    Clazz c = (Clazz) o;

    return id == c.id &&
           weight == c.weight &&
           dimension == c.dimension &&
           (color == null ? c.color == null : color.equals(c.color)) &&
           (legend == null ? c.legend == null : legend.equals(c.legend)) &&
           points.equals(c.points);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Clazz clazz = (Clazz) o;

    return id == clazz.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Clazz { ").append("id=").append(id)
            .append(", legend='").append(legend).append('\'')
            .append(", color=").append(color);
    builder.append(", points= [");
    for (Point p : points) {
      builder.append('\n').append(p);
    }
    builder.append("] }");
    return builder.toString();
  }


  public static final class Builder {
    private final int id;
    private final List<Point> points;
    private String legend;
    private Color color;

    private int weight = 0;
    private int dimension = -1;

    private Builder(int id, int capacity) {
      this.id = id;
      points = new ArrayList<Point>(capacity);
    }

    public Builder(int id) {
      this(id, 100);
    }

    public Clazz createClass() {
      if (points.isEmpty()) {
        throw new IllegalArgumentException("At least one point should be specified");
      }

      if (color == null) {
        color = getDefaultColor(id);
      }

      if (legend == null) {
        legend = "class " + id;
      }

      return new Clazz(this);
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

    public void setLegend(String legend) {
      this.legend = legend;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    /**
     * Порождает последовательность цветов различных для каждого id для id от 0 до 1000000
     *
     * @param classId clazz id
     * @return Color unique for classId between 0..1000000
     */

    static Color getDefaultColor(int classId) {
      int delta = 0x16A56F4E; // Число от балды
      return new Color(0x00FFFFFF & (defaultColors[classId % defaultColors.length].getRGB() + (classId / defaultColors.length) * delta));      
    }
  }
}
