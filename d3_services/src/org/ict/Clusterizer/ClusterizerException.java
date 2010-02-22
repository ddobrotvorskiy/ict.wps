package org.ict.clusterizer;

/**
 * Created by IntelliJ IDEA.
 * User: Yorik
 * Date: 22.02.2010
 * Time: 14:06:31
 */
public class ClusterizerException extends RuntimeException {

  public ClusterizerException(String message) {
    super(message);
  }

  public ClusterizerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClusterizerException(Throwable cause) {
    super(cause);
  }

}
