//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.record.persistence.genericrecordstore;

/**
 * BoundingBox representation for records. This class encapsulates the data for representation only. 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class BoundingBox {
    
    private double westBoundLongitude;
    
    private double southBoundLatitude;
    
    private double eastBoundLongitude;
    
    private double northBoundLatitude;
    
    /**
     * 
     * @param westBoundLongitude
     * @param southBoundLatitude
     * @param eastBoundLongitude
     * @param northBoundLatitude
     */
    public BoundingBox(double westBoundLongitude, double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude){
        this.westBoundLongitude = westBoundLongitude;
        this.southBoundLatitude = southBoundLatitude;
        this.eastBoundLongitude = eastBoundLongitude;
        this.northBoundLatitude = northBoundLatitude;
    }

    /**
     * @return the westBoundLongitude
     */
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }

    /**
     * @param westBoundLongitude the westBoundLongitude to set
     */
    public void setWestBoundLongitude( double westBoundLongitude ) {
        this.westBoundLongitude = westBoundLongitude;
    }

    /**
     * @return the southBoundLatitude
     */
    public double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    /**
     * @param southBoundLatitude the southBoundLatitude to set
     */
    public void setSouthBoundLatitude( double southBoundLatitude ) {
        this.southBoundLatitude = southBoundLatitude;
    }

    /**
     * @return the eastBoundLongitude
     */
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * @param eastBoundLongitude the eastBoundLongitude to set
     */
    public void setEastBoundLongitude( double eastBoundLongitude ) {
        this.eastBoundLongitude = eastBoundLongitude;
    }

    /**
     * @return the northBoundLatitude
     */
    public double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    /**
     * @param northBoundLatitude the northBoundLatitude to set
     */
    public void setNorthBoundLatitude( double northBoundLatitude ) {
        this.northBoundLatitude = northBoundLatitude;
    }
    
    

}
