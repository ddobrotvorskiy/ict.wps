package org.ict.classifier;

import Jama.Matrix;

/**
 * User: mityok
 * Date: 10.02.2010
 * Time: 0:04:33
 */
public class ClassifierWithFeatures implements Classifier {

  private final Classifier originalClassifier;

  private final Classifier innerClassifier;

  private final ClassificationTask originalTask;

  private final Matrix features;

  public ClassifierWithFeatures(Classifier classifier, Matrix features) {
    this.originalClassifier = classifier;
    this.innerClassifier = classifier.recountInNewFeatures(features);
    this.originalTask = classifier.getTask();
    this.features = features;
  }

  @Override
  public int classify(Point point) {
    return innerClassifier.classify(point.recount(features));
  }

  @Override
  public ClassificationTask getTask() {
    return originalTask;
  }


  @Override
  public Classifier recountInNewFeatures(Matrix features) {

    // TODO: test and remove this throw
    if (true)
      throw new UnsupportedOperationException("Unreliable piece of code");

    // possible solution
    return new ClassifierWithFeatures(originalClassifier.recountInNewFeatures(features), this.features.times(features));
  }
}
