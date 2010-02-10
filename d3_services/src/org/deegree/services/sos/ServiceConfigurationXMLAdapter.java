//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/sos/SOSService.java $
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.services.sos.configuration.ServiceConfiguration;

/**
 * This is an xml adapter for the deegree SOS ServiceConfiguration.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class ServiceConfigurationXMLAdapter extends XMLAdapter {

    /**
     * @return the parsed ServiceConfiguration
     * @throws XMLProcessingException
     */
    public ServiceConfiguration parse()
                            throws XMLProcessingException {
        ServiceConfiguration sosConf = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.sos.configuration" );
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            sosConf = (ServiceConfiguration) unmarshaller.unmarshal( rootElement.getXMLStreamReaderWithoutCaching() );
        } catch ( JAXBException e ) {
            throw new XMLProcessingException (e.getMessage(), e);
        }
        return sosConf;
    }
}
