package org.ict.classifier;

/**
 * User: mityok
 * Date: 31.01.2010
 * Time: 21:07:51
 */
abstract class BayesClassifier implements Classifier {

  protected final ClassificationTask task;

  protected BayesClassifier(ClassificationTask task) {
    this.task = task;
  }

  /**
   * returns id of class with maximum probability
   *
   * @param point not null instance of point
   * @return
   */
  @Override
  public final int classify(Point point) {
    double maxProbability = Double.MIN_VALUE;
    Clazz maxClass = task.getClasses().get(0);

    for (Clazz c : task.getClasses()) {
      double p = getProbability(point, c);
      if (p > maxProbability) {
        maxProbability = p;
        maxClass = c;
      }
    }

    return maxClass.getId();
  }

  @Override
  public final ClassificationTask getTask() {
    return task;
  }


  public final int getSlidingExamError() {
    int errors = 0;
    for (Clazz c : task.getClasses()) {
      for (Point p : c.getPoints()) {
        if (c.getId() != classify(p)) {
          errors ++;
        }
      }
    }
    return errors;
  }

  /**
   * Returns probability of statement that "point belongs to clazz"
   * It is not probability in mathematical definition.
   * Returned value is non-negative.
   *
   *
   * @return non-negative probability that "point belongs to clazz"
   */
  public abstract double getProbability(Point point, Clazz clazz);


  /**
   *
   * @return insignificantly small value
   */
  public abstract double getEpsilon();
}
