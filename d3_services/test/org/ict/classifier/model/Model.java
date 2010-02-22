package org.ict.classifier.model;

import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Clazz;
import org.ict.classifier.Point;

/**
 * User: mityok
 * Date: 21.02.2010
 * Time: 16:48:16
 */
public interface Model extends Iterable<Model.ControlPoint> {

  public ClassificationTask getTask();

  public double getExpectedError();

  public double getAcceptableError();


  public static final class ControlPoint {
    public final Point p;
    public final Clazz c;

    protected ControlPoint(Point p, Clazz c) {
      this.p = p;
      this.c = c;
    }
  }
}
