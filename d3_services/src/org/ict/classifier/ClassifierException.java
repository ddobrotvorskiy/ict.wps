package org.ict.classifier;

/**
 * User: mityok
 * Date: 07.02.2010
 * Time: 21:02:40
 */
public class ClassifierException extends RuntimeException {

  public ClassifierException(String message) {
    super(message);
  }

  public ClassifierException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClassifierException(Throwable cause) {
    super(cause);
  }

}

