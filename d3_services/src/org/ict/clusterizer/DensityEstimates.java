package org.ict.clusterizer;

/**
 * Nonparametric density estimates.
 * Includes:
 *
 *
 * User: Yorik
 * Date: 12.03.2010
 * Time: 12:37:42
 */
public class DensityEstimates {
  public static double MultiplicableTriangleCore(Point from, Point to, double maxDistance) {
    if (from.equals(to)) return 1.0d;

    double inf = 1.0 / maxDistance;
    double dist;
    for (int i = 0; i < from.getDim(); i++) {
      dist = Math.abs(from.get(i) - to.get(i));
      if (dist >= maxDistance) return 0.0d;
      inf *= dist;
    }
    return inf;
  }
  /**
   * TODO: Add more density estimates.
   */
}
