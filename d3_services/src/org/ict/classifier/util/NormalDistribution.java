package org.ict.classifier.util;

import java.util.Random;

/**
 * User: mityok
 * Date: 09.02.2010
 * Time: 1:21:04
 */
public final class NormalDistribution {

  private final Random random;
  private final double [] mu;
  private final double [] sigma;
  private final int dim;

  public NormalDistribution() {
    this(new double[] {0.}, new double[] {1.});
  }

  public NormalDistribution(double [] mu, double [] sigma ) {
    if (sigma.length != mu.length) {
      throw new IllegalArgumentException("Mu and sigma have different dimensions");
    }

    dim = mu.length;

    random = new Random();
    this.mu = new double[dim];
    System.arraycopy(mu, 0, this.mu, 0, dim);

    this.sigma = new double[dim];
    System.arraycopy(sigma, 0, this.sigma, 0, dim);
  }

  public double[] next() {
    double[] value = new double[dim];

    for (int i = 0 ; i < dim ; i++) {
      value[i] = mu[i] + sigma[i] * random.nextGaussian();
    }

    return value;
  }
}
