//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/rendering/r2d/se/unevaluated/Continuation.java $
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

package org.deegree.rendering.r2d.se.unevaluated;

import org.deegree.filter.MatchableObject;

/**
 * <code>Continuation</code> is not a real continuation...
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 21726 $, $Date: 2010-01-05 15:07:05 +0600 (Втр, 05 Янв 2010) $
 * @param <T>
 */
public abstract class Continuation<T> {

    private Continuation<T> next;

    /**
     *
     */
    public Continuation() {
        // enable next to be null
    }

    /**
     * @param next
     */
    public Continuation( Continuation<T> next ) {
        this.next = next;
    }

    /**
     * @param base
     * @param f
     */
    public abstract void updateStep( T base, MatchableObject f );

    /**
     * @param base
     * @param f
     */
    public void evaluate( T base, MatchableObject f ) {
        updateStep( base, f );
        if ( next != null ) {
            next.evaluate( base, f );
        }
    }

    /**
     * <code>Updater</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: aschmitz $
     * 
     * @version $Revision: 21726 $, $Date: 2010-01-05 15:07:05 +0600 (Втр, 05 Янв 2010) $
     * @param <T>
     */
    public static interface Updater<T> {
        /**
         * @param obj
         * @param val
         */
        void update( T obj, String val );
    }

    /**
     * Updater for a string buffer.
     */
    public static final Updater<StringBuffer> SBUPDATER = new Updater<StringBuffer>() {
        public void update( StringBuffer obj, String val ) {
            obj.append( val );
        }
    };

}
