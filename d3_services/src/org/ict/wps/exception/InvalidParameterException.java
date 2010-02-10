package org.ict.wps.exception;

import org.deegree.services.wps.ProcessletException;

/**
 * User: mityok
 * Date: 16.01.2010
 * Time: 14:22:54
 */
public class InvalidParameterException  extends ProcessletException {
    public InvalidParameterException(String msg) {
        super(msg);
    }
}
