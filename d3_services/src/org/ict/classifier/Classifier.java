package org.ict.classifier;

import Jama.Matrix;

/**
 *
 * Interface for supervised classifiers.
 *
 *
 * User: mityok
 * Date: 24.01.2010
 * Time: 15:52:37
 */
public interface Classifier {

  /**
   * Classifies point according classification task.
   * Returns id of result class
   *
   * Point should have the same dimension as classification task.
   * Point should not be null.
   *
   * @param point not null instance of point
   * @return id of most probable class
   */
  public int classify(Point point);



  /**
   * Returns classification task, to be solved.
   *
   * Never returns null.
   *
   * @return classification task to be solved.
   */
  public ClassificationTask getTask();


  /**
   * TODO: Написать внятный контракт 
   * Returns classifier recounted in passed features.
   *
   * @param features not null features matrix
   * @return classification task to be solved.
   */
  public Classifier recountInNewFeatures(Matrix features);


}
