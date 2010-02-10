//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/sos/SOService.java $
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
package org.deegree.services.sos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.services.sos.offering.ObservationOffering;

/**
 * This is the SOSService, a collection of ObservationOfferings.
 * <p>
 * This class represents the main entry point for access to observations. You can create a SOSService with the static
 * createService methods that takes a service configuration (see resources/schema/sos/sos_configuration.xsd and
 * resources/schema/example/conf/sos/sos_configuration.xml). An SOSService contains one or more ObservationOfferings,
 * which contain the actual observation data.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 18469 $, $Date: 2009-07-15 18:42:59 +0700 (Срд, 15 Июл 2009) $
 * 
 */
public class SOService {

    private final Map<String, ObservationOffering> offerings = new HashMap<String, ObservationOffering>();

    private final Map<String, ObservationOffering> offeringsById = new HashMap<String, ObservationOffering>();

    /**
     * @param offering
     */
    void addOffering( ObservationOffering offering ) {
        this.offerings.put( offering.getName(), offering );
        offeringsById.put( offering.getID(), offering );
    }

    /**
     * Check if the SOS contains an offering.
     * 
     * @param name
     *            the name or id of the offering
     * @return true if the offering exists
     */
    public boolean hasOffering( String name ) {
        return this.offerings.containsKey( name ) || offeringsById.containsKey( name );
    }

    /**
     * Get the named offering.
     * 
     * @param name
     *            the name or id of the offering
     * @return the offering, or <code>null</code> if it doesn't exists
     */
    public ObservationOffering getOffering( String name ) {
        return offerings.containsKey( name ) ? this.offerings.get( name ) : offeringsById.get( name );
    }

    /**
     * @return all offerings of this SOS configuration
     */
    public List<ObservationOffering> getAllOfferings() {
        return new ArrayList<ObservationOffering>( this.offerings.values() );
    }
}
