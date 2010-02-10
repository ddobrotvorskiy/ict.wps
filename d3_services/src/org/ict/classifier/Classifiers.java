package org.ict.classifier;

import Jama.Matrix;

/**
 * User: mityok
 * Date: 01.02.2010
 * Time: 21:42:31
 */
public final class Classifiers {

  private Classifiers() {
    throw new IllegalStateException("Not instantiable");
  }


  public static Classifier createSimpleClassifier(ClassificationTask task) {
    if (task.getClasses().size() == 1)
      return new TrivialClassifier(task);


    ClassificationTask optimizedTask = HyperCubeOptimizer.optimizeTask(task);

    return ParzenClassifier.createClassifier(optimizedTask);
  }


  public static Classifier createClassifier(ClassificationTask task) {
    
    if (task.getClasses().size() == 1)
      return new TrivialClassifier(task);


    ClassificationTask optimizedTask = HyperCubeOptimizer.optimizeTask(task);

    BayesClassifier classifier = ParzenClassifier.createClassifier(optimizedTask);

    //TODO debug LandgrebeFeatures
    Matrix features = LandgrebeFeatures.extractFeatures(classifier);

    //TODO debug ClassifierWithFeatures
    return new ClassifierWithFeatures(classifier, features);
  }

  public static Classifier createTreeClassifier(ClassificationTask task) {
    return new TreeClassifier(task);
  }
}
