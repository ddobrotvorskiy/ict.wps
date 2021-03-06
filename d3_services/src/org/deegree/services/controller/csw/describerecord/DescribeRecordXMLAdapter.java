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
package org.deegree.services.controller.csw.describerecord;

import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.i18n.Messages;
import org.deegree.services.controller.csw.AbstractCSWRequestXMLAdapter;

/**
 * XML-Requesthandling for the {@link DescribeRecord}
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeRecordXMLAdapter extends AbstractCSWRequestXMLAdapter {
    
    public DescribeRecord parse( Version version ) {

        if ( version == null ) {
            version = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version", nsContext ) ) );
        }

        DescribeRecord result = null;

        if ( VERSION_202.equals( version ) ) {
            result = parse202( );
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_202 ) );
            throw new InvalidParameterValueException( msg );
        }

        return result;

    }

    /**
     * @version CSW 2.0.2
     * @return DescribeRecord
     */
    private DescribeRecord parse202( ) {

        // optional: '@outputFormat'
        String outputFormat = rootElement.getAttributeValue( new QName( "outputFormat" ) );
        if ( outputFormat == null ) {
            outputFormat = "application/xml";
        }

        // optional: '@schemaLanguage'
        String schemaLanguage = rootElement.getAttributeValue( new QName( "schemaLanguage" ) );
        if ( schemaLanguage == null ) {
            schemaLanguage = "http://www.w3.org/XML/Schema";
        }

        // 'TypeName' elements (minOccurs=0, maxOccurs=unbounded)
        QName[] typeNames = getNodesAsQNames( rootElement, new XPath( "csw:TypeName", nsContext ) );

        return new DescribeRecord( VERSION_202, null, typeNames, outputFormat, schemaLanguage );
    }

}
