package org.ict.classifier;

import Jama.Matrix;

/**
 * User: mityok
 * Date: 10.02.2010
 * Time: 0:04:33
 */
public class ClassifierWithFeatures implements Classifier {

  private final Classifier classifier;
  private final Matrix features;

  public ClassifierWithFeatures(Classifier classifier, Matrix features) {
    this.classifier = classifier;
    this.features = features;
  }

  @Override
  public int classify(Point point) {
    return classifier.classify(point.recount(features));
  }

  @Override
  public ClassificationTask getTask() {
    return classifier.getTask();
  }


  @Override
  public Classifier recountInNewFeatures(Matrix features) {
    throw new UnsupportedOperationException("");
//
//    // TODO: test and remove this throw
//    //if (true)
//      throw new UnsupportedOperationException("Unreliable piece of code");
//
//    // possible solution
//    //return new ClassifierWithFeatures(classifier.recountInNewFeatures(features), this.features.times(features));
  }

  @Override
  public final int getSlidingExamError() {
    return classifier.getSlidingExamError();
  }
}
