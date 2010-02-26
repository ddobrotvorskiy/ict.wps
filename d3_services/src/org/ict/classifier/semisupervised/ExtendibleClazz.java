/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ict.classifier.semisupervised;

import org.ict.classifier.Point;
import org.ict.classifier.Clazz;

/**
 * Clazz that can be extended with new points in the process of classification
 * Used im some semi-supervised algorythms.
 * Note: After adding new points {weight} member will be incorrect,
 *   since it is declared as final in Clazz
 *
 * @author positron
 */
public class ExtendibleClazz extends Clazz {

	public ExtendibleClazz(Clazz clazz) {
		super(clazz);
	}

	public void addPoint(Point point) {
		if (point == null)	throw new IllegalArgumentException("Null argument not allowed");

		if (dimension != point.getDim())
		  throw new IllegalArgumentException("All points should have the same dimension");
		points.add(point);
		//weight += point.getWeight();
    }

}
