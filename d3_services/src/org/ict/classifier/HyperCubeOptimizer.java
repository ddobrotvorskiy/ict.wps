package org.ict.classifier;

import java.util.*;

/**
 * User: mityok
 * Date: 28.01.2010
 * Time: 22:06:24
 */
class HyperCubeOptimizer {

  private HyperCubeOptimizer() {
    throw new IllegalStateException("Not instantiable");
  }

  /**
   * cube edges bounds. belong to (0, 1].
   *
   */
  private static double MIN_CUBE_EDGE = 0.001;
  private static double MAX_CUBE_EDGE = 0.1;

  public static ClassificationTask optimizeTask(ClassificationTask task) {
    Cube cube;
    { // determine hypercube parameters
      int dim = task.getDimension();
      double [] mins = new double [dim];
      double [] maxes = new double [dim];
      for (int i = 0; i < dim; i++) {
        mins[i] = Double.MAX_VALUE;
        maxes[i] = Double.MIN_VALUE;
      }
      for (Clazz c : task.getClasses()) {
        for (Point p : c.getPoints()) {
          for (int j = 0; j < dim; j++) {
            mins[j] = Math.min(mins[j], p.get(j));
            maxes[j] = Math.max(maxes[j], p.get(j));
          }
        }
      }
      double edge = 0.0;
      for (int i = 0; i < dim; i++) {
        edge = Math.max(edge, maxes[i] - mins[i]);
        mins[i] -= 0.5;
      }

      edge = edge + 1.;  // небольшой запас, чтобы не было точек на границе куба.

      cube = new Cube(mins, edge);
    }

    Map<Integer, Clazz.Builder> classBuilders = new HashMap<Integer, Clazz.Builder>(task.getClasses().size());

    for (Clazz c : task.getClasses()) {
      Clazz.Builder b = new Clazz.Builder(c.getId());
      b.setColor(c.getColor());
      b.setLegend(c.getLegend());
      classBuilders.put(c.getId(), b);
    }

    List<PointInfo> points = new LinkedList<PointInfo>();
    for (Clazz c : task.getClasses()) {
      for (Point p : c.getPoints()) {
        points.add(new PointInfo(p, c));
      }
    }

    optimizeTaskRecursively(cube, points, classBuilders, cube.edge * MIN_CUBE_EDGE, cube.edge * MAX_CUBE_EDGE);

    ClassificationTask.Builder taskBuilder = new ClassificationTask.Builder();
    for (Clazz.Builder b : classBuilders.values()) {
      taskBuilder.addClass(b.createClass());
    }
    return taskBuilder.createTask();
  }

  private static void optimizeTaskRecursively(Cube cube, List<PointInfo> points, Map<Integer, Clazz.Builder> classBuilders,
                                              double min, double max) {

    if (points == null || points.isEmpty())
      return;

    Map<Clazz, List<Point>> map = new HashMap<Clazz, List<Point>>();
    List<PointInfo> localPoints = new LinkedList<PointInfo>();

    for (PointInfo pointInfo : points) {
      if (cube.contains(pointInfo.point)) {
        localPoints.add(pointInfo);
        List<Point> list = map.get(pointInfo.clazz);
        if (list == null) {
          list = new LinkedList<Point>();
          map.put(pointInfo.clazz, list);
        }
        list.add(pointInfo.point);
      }
    }

    if ((map.size() <= 1 || cube.edge <= min) && cube.edge < max) {  // stop recursion
      for (Map.Entry<Clazz, List<Point>> entry : map.entrySet()) {
        Clazz.Builder builder = classBuilders.get(entry.getKey().getId());
        builder.addPoint(Point.mean(entry.getValue()));
      }
    } else {        // continue recursion
      for (Cube subCube : cube.divide())
        optimizeTaskRecursively(subCube, localPoints, classBuilders, min, max);
    }
  }

  private static class Cube {
    private final double[] minPoint;
    private final double edge;

    public Cube(double[] minPoint, double edge) {
      this.minPoint = minPoint;
      this.edge = edge;
    }

    public boolean contains(Point point) {
      if (point.getDim() != minPoint.length)
        throw new IllegalArgumentException("Dimensions mismatch");

      for (int i = 0; i < minPoint.length; i++ ) {
        if ((minPoint[i] > point.get(i)) || (point.get(i) >= minPoint[i] + edge))
          return false;
      }

      return true;
    }

    public List<Cube> divide() {
      List<Cube> list = new LinkedList<Cube>();
      // "2 << (minPoint.length -1)" not appropriate here where minpoint.length == 1. Math.pow() used instead.
      for (int i = 0; i < Math.pow(2, minPoint.length); i++ ) {
        double [] newMinPoint = new double [minPoint.length];
        for (int j = 0; j < minPoint.length; j++ ) {
          newMinPoint[j] = minPoint[j] + ((i >> j) & 1) * (edge / 2.0);
        }
        list.add(new Cube(newMinPoint, edge / 2.0));
      }
      return list;
    }
  }

  private static class PointInfo {
    private final Point point;
    private final Clazz clazz;

    private PointInfo(Point point, Clazz clazz) {
      this.point = point;
      this.clazz = clazz;
    }
  }
}
