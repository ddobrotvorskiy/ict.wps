//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/protocol/sos/filter/BeginFilter.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.protocol.sos.filter;

import java.util.Date;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: aionita $
 *
 * @version $Revision: 19378 $, $Date: 2009-08-28 15:26:53 +0700 (Птн, 28 Авг 2009) $
 *
 */
public class BeginFilter implements TimeFilter {
    private final Date begin;
    private final boolean inclusiveBegin;

    /**
     * @param begin
     */
    public BeginFilter( Date begin ) {
        this.begin = begin;
        this.inclusiveBegin = false;
    }

    /**
     * @param begin
     * @param inclusiveBegin if the date is included
     */
    public BeginFilter( Date begin, boolean inclusiveBegin ) {
        this.begin = begin;
        this.inclusiveBegin = inclusiveBegin;
    }

    /**
     * @return the begin date
     */
    public Date getBegin() {
        return begin;
    }

    /**
     * @return true if the begin date is included
     */
    public boolean isInclusiveBegin() {
        return inclusiveBegin;
    }
}
