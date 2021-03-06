//$HeadURL$
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
package org.deegree.filter.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.SENS;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getAttributeValueAsBoolean;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredAttributeValue;
import static org.deegree.commons.xml.stax.StAXParsingHelper.require;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.types.GenericCustomPropertyValue;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.BinaryComparisonOperator;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.comparison.ComparisonOperator.SubType;
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Div;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.Mul;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.expression.Sub;
import org.deegree.filter.function.geometry.IsCurve;
import org.deegree.filter.function.geometry.IsPoint;
import org.deegree.filter.function.geometry.IsSurface;
import org.deegree.filter.function.se.Categorize;
import org.deegree.filter.function.se.ChangeCase;
import org.deegree.filter.function.se.Concatenate;
import org.deegree.filter.function.se.FormatDate;
import org.deegree.filter.function.se.FormatNumber;
import org.deegree.filter.function.se.Interpolate;
import org.deegree.filter.function.se.Recode;
import org.deegree.filter.function.se.StringLength;
import org.deegree.filter.function.se.StringPosition;
import org.deegree.filter.function.se.Substring;
import org.deegree.filter.function.se.Trim;
import org.deegree.filter.i18n.Messages;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.generic.GenericCustomPropertyReader;
import org.deegree.gml.geometry.GML3GeometryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes XML documents that comply to the OGC Filter Encoding Specification 1.1.0 as {@link Filter} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Filter110XMLDecoder {

    private static final Logger LOG = LoggerFactory.getLogger( Filter110XMLDecoder.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final QName FEATURE_ID_ELEMENT = new QName( OGC_NS, "FeatureId" );

    private static final QName FID_ATTR_NAME = new QName( "fid" );

    private static final QName GML_OBJECT_ID_ELEMENT = new QName( OGC_NS, "GmlObjectId" );

    private static final QName GML_ID_ATTR_NAME = new QName( GML_NS, "id" );

    private static final Map<Expression.Type, QName> expressionTypeToElementName = new HashMap<Expression.Type, QName>();

    private static final Map<QName, Expression.Type> elementNameToExpressionType = new HashMap<QName, Expression.Type>();

    private static final Map<QName, Operator.Type> elementNameToOperatorType = new HashMap<QName, Operator.Type>();

    private static final Map<QName, SpatialOperator.SubType> elementNameToSpatialOperatorType = new HashMap<QName, SpatialOperator.SubType>();

    private static final Map<SpatialOperator.SubType, QName> spatialOperatorTypeToElementName = new HashMap<SpatialOperator.SubType, QName>();

    private static final Map<QName, ComparisonOperator.SubType> elementNameToComparisonOperatorType = new HashMap<QName, ComparisonOperator.SubType>();

    private static final Map<ComparisonOperator.SubType, QName> comparisonOperatorTypeToElementName = new HashMap<ComparisonOperator.SubType, QName>();

    private static final Map<QName, LogicalOperator.SubType> elementNameToLogicalOperatorType = new HashMap<QName, LogicalOperator.SubType>();

    private static final Map<LogicalOperator.SubType, QName> logicalOperatorTypeToElementName = new HashMap<LogicalOperator.SubType, QName>();

    static {

        // element name <-> expression type
        addElementToExpressionMapping( new QName( OGC_NS, "Add" ), Expression.Type.ADD );
        addElementToExpressionMapping( new QName( OGC_NS, "Sub" ), Expression.Type.SUB );
        addElementToExpressionMapping( new QName( OGC_NS, "Mul" ), Expression.Type.MUL );
        addElementToExpressionMapping( new QName( OGC_NS, "Div" ), Expression.Type.DIV );
        addElementToExpressionMapping( new QName( OGC_NS, "PropertyName" ), Expression.Type.PROPERTY_NAME );
        addElementToExpressionMapping( new QName( OGC_NS, "Function" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( OGC_NS, "Literal" ), Expression.Type.LITERAL );
        // SE functions
        addElementToExpressionMapping( new QName( SENS, "FormatNumber" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "FormatDate" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "Substring" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "Concatenate" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "ChangeCase" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "Trim" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "StringPosition" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "StringLength" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "Categorize" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "Interpolate" ), Expression.Type.FUNCTION );
        addElementToExpressionMapping( new QName( SENS, "Recode" ), Expression.Type.FUNCTION );

        // element name <-> spatial operator type
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "BBOX" ), SpatialOperator.SubType.BBOX );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Beyond" ), SpatialOperator.SubType.BEYOND );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Contains" ), SpatialOperator.SubType.CONTAINS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Crosses" ), SpatialOperator.SubType.CROSSES );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Equals" ), SpatialOperator.SubType.EQUALS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Disjoint" ), SpatialOperator.SubType.DISJOINT );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "DWithin" ), SpatialOperator.SubType.DWITHIN );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Intersects" ), SpatialOperator.SubType.INTERSECTS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Overlaps" ), SpatialOperator.SubType.OVERLAPS );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Touches" ), SpatialOperator.SubType.TOUCHES );
        addElementToSpatialOperatorMapping( new QName( OGC_NS, "Within" ), SpatialOperator.SubType.WITHIN );

        // element name <-> logical operator type
        addElementToLogicalOperatorMapping( new QName( OGC_NS, "And" ), LogicalOperator.SubType.AND );
        addElementToLogicalOperatorMapping( new QName( OGC_NS, "Or" ), LogicalOperator.SubType.OR );
        addElementToLogicalOperatorMapping( new QName( OGC_NS, "Not" ), LogicalOperator.SubType.NOT );

        // element name <-> comparison operator type
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsBetween" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_BETWEEN );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsGreaterThan" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsGreaterThanOrEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsLessThan" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsLessThanOrEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_LESS_THAN_OR_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsLike" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_LIKE );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsNotEqualTo" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_NOT_EQUAL_TO );
        addElementToComparisonOperatorMapping( new QName( OGC_NS, "PropertyIsNull" ),
                                               ComparisonOperator.SubType.PROPERTY_IS_NULL );
    }

    private static void addElementToExpressionMapping( QName elementName, Expression.Type type ) {
        elementNameToExpressionType.put( elementName, type );
        expressionTypeToElementName.put( type, elementName );
    }

    private static void addElementToSpatialOperatorMapping( QName elementName, SpatialOperator.SubType type ) {
        elementNameToOperatorType.put( elementName, Operator.Type.SPATIAL );
        elementNameToSpatialOperatorType.put( elementName, type );
        spatialOperatorTypeToElementName.put( type, elementName );
    }

    private static void addElementToLogicalOperatorMapping( QName elementName, LogicalOperator.SubType type ) {
        elementNameToOperatorType.put( elementName, Operator.Type.LOGICAL );
        elementNameToLogicalOperatorType.put( elementName, type );
        logicalOperatorTypeToElementName.put( type, elementName );
    }

    private static void addElementToComparisonOperatorMapping( QName elementName, ComparisonOperator.SubType type ) {
        elementNameToOperatorType.put( elementName, Operator.Type.COMPARISON );
        elementNameToComparisonOperatorType.put( elementName, type );
        comparisonOperatorTypeToElementName.put( type, elementName );
    }

    /**
     * Returns the object representation for the given <code>wfs:Filter</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Filter&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wfs:Filter&gt;)</li>
     * </ul>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;wfs:Filter&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/wfs:Filter&gt;) afterwards
     * @return corresponding {@link Filter} object
     * @throws XMLParsingException
     *             if the element is not a valid "wfs:Filter" element
     * @throws XMLStreamException
     */
    public static Filter parse( XMLStreamReader xmlStream )
                            throws XMLParsingException, XMLStreamException {

        Filter filter = null;
        xmlStream.require( START_ELEMENT, OGC_NS, "Filter" );
        xmlStream.nextTag();
        if ( xmlStream.getEventType() != START_ELEMENT ) {
            throw new XMLParsingException( xmlStream, Messages.getMessage( "FILTER_PARSER_FILTER_EMPTY",
                                                                           new QName( OGC_NS, "Filter" ) ) );
        }
        QName elementName = xmlStream.getName();
        if ( GML_OBJECT_ID_ELEMENT.equals( elementName ) || FEATURE_ID_ELEMENT.equals( elementName ) ) {
            LOG.debug( "Building id filter" );
            filter = parseIdFilter( xmlStream );
        } else {
            LOG.debug( "Building operator filter" );
            Operator rootOperator = parseOperator( xmlStream );
            filter = new OperatorFilter( rootOperator );
            xmlStream.nextTag();
        }

        xmlStream.require( XMLStreamConstants.END_ELEMENT, OGC_NS, "Filter" );
        return filter;
    }

    /**
     * Returns the object representation for the given <code>ogc:expression</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>ogc:Add</li>
     * <li>ogc:Sub</li>
     * <li>ogc:Div</li>
     * <li>ogc:Mul</li>
     * <li>ogc:PropertyName</li>
     * <li>ogc:Literal</li>
     * <li>ogc:Function</li>
     * </ul>
     * </p>
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:expression" element
     * @throws XMLStreamException
     */
    public static Expression parseExpression( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Expression expression = null;

        // check if element name is a valid expression element
        require( xmlStream, START_ELEMENT );
        Expression.Type type = elementNameToExpressionType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( Expression.Type.class, expressionTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }
        switch ( type ) {
        case ADD: {
            xmlStream.nextTag();
            Expression param1 = parseExpression( xmlStream );
            xmlStream.nextTag();
            Expression param2 = parseExpression( xmlStream );
            expression = new Add( param1, param2 );
            xmlStream.nextTag();
            break;
        }
        case SUB: {
            xmlStream.nextTag();
            Expression param1 = parseExpression( xmlStream );
            xmlStream.nextTag();
            Expression param2 = parseExpression( xmlStream );
            expression = new Sub( param1, param2 );
            xmlStream.nextTag();
            break;
        }
        case MUL: {
            xmlStream.nextTag();
            Expression param1 = parseExpression( xmlStream );
            xmlStream.nextTag();
            Expression param2 = parseExpression( xmlStream );
            expression = new Mul( param1, param2 );
            xmlStream.nextTag();
            break;
        }
        case DIV: {
            xmlStream.nextTag();
            Expression param1 = parseExpression( xmlStream );
            xmlStream.nextTag();
            Expression param2 = parseExpression( xmlStream );
            expression = new Div( param1, param2 );
            xmlStream.nextTag();
            break;
        }
        case PROPERTY_NAME: {
            expression = parsePropertyName( xmlStream, false );
            break;
        }
        case LITERAL: {
            expression = parseLiteral( xmlStream );
            break;
        }
        case FUNCTION: {
            expression = parseFunction( xmlStream );
            break;
        }
        }
        return expression;
    }

    /**
     * Returns the object representation for the given <code>ogc:expression</code> element event that the cursor of the
     * associated <code>XMLStreamReader</code> points at.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>ogc:Add</li>
     * <li>ogc:Sub</li>
     * <li>ogc:Div</li>
     * <li>ogc:Mul</li>
     * <li>ogc:PropertyName</li>
     * <li>ogc:Literal</li>
     * <li>ogc:Function</li>
     * </ul>
     * </p>
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;)</li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:expression&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:expression&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:expression" element
     * @throws XMLStreamException
     */
    public static Function parseFunction( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        if ( xmlStream.getLocalName().equals( "FormatNumber" ) ) {
            FormatNumber fun = new FormatNumber();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "FormatDate" ) ) {
            FormatDate fun = new FormatDate();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "Substring" ) ) {
            Substring fun = new Substring();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "Concatenate" ) ) {
            Concatenate fun = new Concatenate();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "ChangeCase" ) ) {
            ChangeCase fun = new ChangeCase();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "Trim" ) ) {
            Trim fun = new Trim();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "StringPosition" ) ) {
            StringPosition fun = new StringPosition();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "StringLength" ) ) {
            StringLength fun = new StringLength();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "Categorize" ) ) {
            Categorize fun = new Categorize();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "Interpolate" ) ) {
            Interpolate fun = new Interpolate();
            fun.parse( xmlStream );
            return fun;
        }
        if ( xmlStream.getLocalName().equals( "Recode" ) ) {
            Recode fun = new Recode();
            fun.parse( xmlStream );
            return fun;
        }

        xmlStream.require( START_ELEMENT, OGC_NS, "Function" );
        String name = getRequiredAttributeValue( xmlStream, "name" );
        xmlStream.nextTag();
        List<Expression> params = new ArrayList<Expression>();
        while ( xmlStream.getEventType() == START_ELEMENT ) {
            params.add( parseExpression( xmlStream ) );
            xmlStream.nextTag();
        }
        xmlStream.require( END_ELEMENT, OGC_NS, "Function" );

        if ( name.equals( "IsPoint" ) ) {
            return new IsPoint( params );
        }
        if ( name.equals( "IsCurve" ) ) {
            return new IsCurve( params );
        }
        if ( name.equals( "IsSurface" ) ) {
            return new IsSurface( params );
        }

        return new Function( name, params );
    }

    /**
     * Returns the object representation for the given <code>ogc:comparisonOps</code> element event that the cursor of
     * the associated <code>XMLStreamReader</code> points at.
     * <p>
     * The element must be one of the following:
     * <ul>
     * <li>ogc:PropertyIsEqualTo</li>
     * <li>ogc:PropertyIsGreaterThan</li>
     * <li>ogc:PropertyIsGreaterThanOrEqualTo</li>
     * <li>ogc:PropertyIsLessThan</li>
     * <li>ogc:PropertyIsLessThanOrEqualTo</li>
     * <li>ogc:PropertyIsNotEqualTo</li>
     * <li>ogc:PropertyIsBetween</li>
     * <li>ogc:PropertyIsLike</li>
     * <li>ogc:PropertyIsNull</li>
     * </ul>
     * </p>
     * <p>
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:comparisonOps&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/ogc:comparisonOps&gt;)
     * </li>
     * </ul>
     * </p>
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event (&lt;ogc:comparisonOps&gt;), points at the
     *            corresponding <code>END_ELEMENT</code> event (&lt;/ogc:comparisonOps&gt;) afterwards
     * @return corresponding {@link Expression} object
     * @throws XMLParsingException
     *             if the element is not a valid "ogc:comparisonOps" element
     * @throws XMLStreamException
     */
    public static ComparisonOperator parseComparisonOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        ComparisonOperator comparisonOperator = null;

        // check if element name is a valid comparison operator element
        ComparisonOperator.SubType type = elementNameToComparisonOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( ComparisonOperator.SubType.class,
                                                         comparisonOperatorTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case PROPERTY_IS_EQUAL_TO:
        case PROPERTY_IS_GREATER_THAN:
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_LESS_THAN:
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
        case PROPERTY_IS_NOT_EQUAL_TO:
            comparisonOperator = parseBinaryComparisonOperator( xmlStream, type );
            break;
        case PROPERTY_IS_BETWEEN:
            comparisonOperator = parsePropertyIsBetweenOperator( xmlStream );
            break;
        case PROPERTY_IS_LIKE:
            comparisonOperator = parsePropertyIsLikeOperator( xmlStream );
            break;
        case PROPERTY_IS_NULL:
            comparisonOperator = parsePropertyIsNullOperator( xmlStream );
            break;
        }
        return comparisonOperator;
    }

    private static Operator parseOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Operator operator = null;

        // check if element name is a valid operator element
        Operator.Type type = elementNameToOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String expectedList = elemNames( Operator.Type.class, logicalOperatorTypeToElementName ) + ", "
                                  + elemNames( Operator.Type.class, spatialOperatorTypeToElementName ) + ", "
                                  + elemNames( Operator.Type.class, comparisonOperatorTypeToElementName );
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(), expectedList );
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case COMPARISON:
            LOG.debug( "Building comparison operator" );
            operator = parseComparisonOperator( xmlStream );
            break;
        case LOGICAL:
            LOG.debug( "Building logical operator" );
            operator = parseLogicalOperator( xmlStream );
            break;
        case SPATIAL:
            LOG.debug( "Building spatial operator" );
            operator = parseSpatialOperator( xmlStream );
            break;
        }
        return operator;
    }

    private static IdFilter parseIdFilter( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        Set<String> matchingIds = new HashSet<String>();

        // needed to check that only one type of identifier elements occur, OGC 04-095, 11.2, p.15
        QName lastIdElement = null;

        while ( xmlStream.getEventType() == START_ELEMENT ) {
            QName childElementName = xmlStream.getName();
            if ( GML_OBJECT_ID_ELEMENT.equals( childElementName ) ) {
                if ( lastIdElement != null && !lastIdElement.equals( GML_OBJECT_ID_ELEMENT ) ) {
                    String msg = Messages.getMessage( "FILTER_PARSER_ONLY_ONE_TYPE_OF_IDS" );
                    throw new XMLParsingException( xmlStream, msg );
                }
                lastIdElement = GML_OBJECT_ID_ELEMENT;
                String id = xmlStream.getAttributeValue( GML_NS, "id" );
                if ( id == null || id.length() == 0 ) {
                    String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_NO_ID", GML_OBJECT_ID_ELEMENT,
                                                      GML_ID_ATTR_NAME );
                    throw new XMLParsingException( xmlStream, msg );
                }
                matchingIds.add( id );
                xmlStream.nextTag();
                xmlStream.require( XMLStreamConstants.END_ELEMENT, OGC_NS, "GmlObjectId" );
            } else if ( FEATURE_ID_ELEMENT.equals( childElementName ) ) {
                if ( lastIdElement != null && !lastIdElement.equals( FEATURE_ID_ELEMENT ) ) {
                    String msg = Messages.getMessage( "FILTER_PARSER_ONLY_ONE_TYPE_OF_IDS" );
                    throw new XMLParsingException( xmlStream, msg );
                }
                lastIdElement = FEATURE_ID_ELEMENT;
                String id = xmlStream.getAttributeValue( null, "fid" );
                if ( id == null || id.length() == 0 ) {
                    String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_NO_ID", FEATURE_ID_ELEMENT,
                                                      FID_ATTR_NAME );
                    throw new XMLParsingException( xmlStream, msg );
                }
                matchingIds.add( id );
                xmlStream.nextTag();
                xmlStream.require( XMLStreamConstants.END_ELEMENT, OGC_NS, "FeatureId" );
            } else {
                String msg = Messages.getMessage( "FILTER_PARSER_ID_FILTER_UNEXPECTED_ELEMENT", childElementName,
                                                  GML_OBJECT_ID_ELEMENT, FEATURE_ID_ELEMENT );
                throw new XMLParsingException( xmlStream, msg );
            }
            xmlStream.nextTag();
        }
        return new IdFilter( matchingIds );
    }

    private static ComparisonOperator parseBinaryComparisonOperator( XMLStreamReader xmlStream, SubType type )
                            throws XMLStreamException {

        BinaryComparisonOperator comparisonOperator = null;

        boolean matchCase = getAttributeValueAsBoolean( xmlStream, null, "matchCase", true );

        StAXParsingHelper.requireNextTag( xmlStream, START_ELEMENT );
        Expression parameter1 = parseExpression( xmlStream );
        StAXParsingHelper.requireNextTag( xmlStream, START_ELEMENT );
        Expression parameter2 = parseExpression( xmlStream );
        StAXParsingHelper.requireNextTag( xmlStream, END_ELEMENT );

        switch ( type ) {
        case PROPERTY_IS_EQUAL_TO:
            comparisonOperator = new PropertyIsEqualTo( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_NOT_EQUAL_TO:
            comparisonOperator = new PropertyIsNotEqualTo( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_LESS_THAN:
            comparisonOperator = new PropertyIsLessThan( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
            comparisonOperator = new PropertyIsLessThanOrEqualTo( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_GREATER_THAN:
            comparisonOperator = new PropertyIsGreaterThan( parameter1, parameter2, matchCase );
            break;
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
            comparisonOperator = new PropertyIsGreaterThanOrEqualTo( parameter1, parameter2, matchCase );
            break;
        default:
            assert false;
        }
        return comparisonOperator;
    }

    private static Literal<?> parseLiteral( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        // TODO outfactor generic XML representation and parser to commons
        GenericCustomPropertyReader literalParser = new GenericCustomPropertyReader();
        GenericCustomPropertyValue value = literalParser.parse( new XMLStreamReaderWrapper( xmlStream, null ) );

        List<GenericCustomPropertyValue> childNodes = value.getChildNodes();
        if ( childNodes.size() == 0 ) {
            List<String> textNodes = value.getTextNodes();
            if ( textNodes.size() >= 1 ) {
                return new Literal<String>( textNodes.get( 0 ) );
            }
        }
        return new Literal<GenericCustomPropertyValue>( value );
    }

    private static PropertyName parsePropertyName( XMLStreamReader xmlStream, boolean permitEmpty )
                            throws XMLStreamException {

        NamespaceContext nsc = StAXParsingHelper.getDeegreeNamespaceContext( xmlStream );
        String propName = xmlStream.getElementText().trim();
        if ( !permitEmpty && propName.isEmpty() ) {
            // TODO filter encoding guy: use whatever exception shall be used here. But make sure that the
            // GetObservation100XMLAdapter gets an exception from here as the compliance of the SOS hangs on it's thread
            throw new XMLParsingException( xmlStream, Messages.getMessage( "FILTER_PARSER_PROPERTY_NAME_EMPTY",
                                                                           new QName( OGC_NS, "PropertyName" ) ) );
        }
        return new PropertyName( propName, nsc );
    }

    private static PropertyIsBetween parsePropertyIsBetweenOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        xmlStream.nextTag();
        Expression expression = parseExpression( xmlStream );

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, OGC_NS, "LowerBoundary" );
        xmlStream.nextTag();
        Expression lowerBoundary = parseExpression( xmlStream );
        xmlStream.nextTag();

        xmlStream.nextTag();
        xmlStream.require( START_ELEMENT, OGC_NS, "UpperBoundary" );
        xmlStream.nextTag();
        Expression upperBoundary = parseExpression( xmlStream );
        xmlStream.nextTag();
        xmlStream.nextTag();

        return new PropertyIsBetween( expression, lowerBoundary, upperBoundary );
    }

    private static PropertyIsLike parsePropertyIsLikeOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        String wildCard = getRequiredAttributeValue( xmlStream, "wildCard" );
        String singleChar = getRequiredAttributeValue( xmlStream, "singleChar" );
        String escapeChar = getRequiredAttributeValue( xmlStream, "escapeChar" );

        xmlStream.nextTag();
        PropertyName propName = parsePropertyName( xmlStream, false );

        xmlStream.nextTag();
        Literal<?> literal = parseLiteral( xmlStream );
        xmlStream.nextTag();
        return new PropertyIsLike( propName, literal, wildCard, singleChar, escapeChar );
    }

    private static PropertyIsNull parsePropertyIsNullOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        xmlStream.nextTag();
        PropertyName propName = parsePropertyName( xmlStream, false );
        xmlStream.nextTag();
        return new PropertyIsNull( propName );
    }

    private static LogicalOperator parseLogicalOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {

        LogicalOperator logicalOperator = null;

        // check if element name is a valid logical operator element
        LogicalOperator.SubType type = elementNameToLogicalOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( LogicalOperator.SubType.class,
                                                         logicalOperatorTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        switch ( type ) {
        case AND: {
            List<Operator> innerOperators = new ArrayList<Operator>();
            while ( xmlStream.nextTag() == START_ELEMENT ) {
                innerOperators.add( parseOperator( xmlStream ) );
            }
            if ( innerOperators.size() < 2 ) {
                String msg = "Error while parsing And operator. Must have at least two arguments.";
                throw new XMLParsingException( xmlStream, msg );
            }
            logicalOperator = new And( innerOperators.toArray( new Operator[innerOperators.size()] ) );
            break;
        }
        case OR: {
            List<Operator> innerOperators = new ArrayList<Operator>();
            while ( xmlStream.nextTag() == START_ELEMENT ) {
                innerOperators.add( parseOperator( xmlStream ) );
            }
            if ( innerOperators.size() < 2 ) {
                String msg = "Error while parsing Or operator. Must have at least two arguments.";
                throw new XMLParsingException( xmlStream, msg );
            }
            logicalOperator = new Or( innerOperators.toArray( new Operator[innerOperators.size()] ) );
            break;
        }
        case NOT: {
            xmlStream.nextTag();
            Operator parameter = parseOperator( xmlStream );
            logicalOperator = new Not( parameter );
            xmlStream.nextTag();
            break;
        }
        }
        return logicalOperator;
    }

    private static SpatialOperator parseSpatialOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        SpatialOperator spatialOperator = null;

        require( xmlStream, START_ELEMENT );
        // check if element name is a valid spatial operator element name
        SpatialOperator.SubType type = elementNameToSpatialOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( SpatialOperator.SubType.class,
                                                         spatialOperatorTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        xmlStream.nextTag();

        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlStream, null );
        GML3GeometryReader geomParser = new GML3GeometryReader( GMLVersion.GML_31, null, null );

        try {
            switch ( type ) {
            case BBOX: {
                // first parameter: 'ogc:PropertyName' (can be empty)
                PropertyName param1 = parsePropertyName( xmlStream, true );
                xmlStream.nextTag();
                // second parameter: 'gml:Envelope'
                xmlStream.require( START_ELEMENT, GML_NS, "Envelope" );
                Envelope param2 = geomParser.parseEnvelope( wrapper );
                spatialOperator = new BBOX( param1, param2 );
                break;
            }
            case BEYOND: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                // third parameter: 'ogc:Distance'
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, OGC_NS, "Distance" );
                String distanceUnits = getRequiredAttributeValue( xmlStream, "units" );
                xmlStream.nextTag();
                // In Filter 1.1.0 (with distinction to 1.0.0) the <Distance> element DOES NOT contain text
                // and the value is in the "units" attribute. The UOM is taken from the enclosing CRS.
                Measure distance = new Measure( distanceUnits, null );
                spatialOperator = new Beyond( param1, param2, distance );
                break;
            }
            case INTERSECTS: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Intersects( param1, param2 );
                break;
            }
            case CONTAINS: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Contains( param1, param2 );
                break;
            }
            case CROSSES: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Crosses( param1, param2 );
                break;
            }
            case DISJOINT: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Disjoint( param1, param2 );
                break;
            }
            case DWITHIN: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                // third parameter: 'ogc:Distance'
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, OGC_NS, "Distance" );
                String distanceUnits = getRequiredAttributeValue( xmlStream, "units" );
                // In Filter 1.1.0 (with distinction to 1.0.0) the <Distance> element DOES NOT contain text
                // and the value is in the "units" attribute. The UOM is taken from the enclosing CRS.
                Measure distance = new Measure( distanceUnits, null );
                spatialOperator = new DWithin( param1, param2, distance );
                xmlStream.nextTag();
                break;
            }
            case EQUALS: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Equals( param1, param2 );
                break;
            }
            case OVERLAPS: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Overlaps( param1, param2 );
                break;
            }
            case TOUCHES: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Touches( param1, param2 );
                break;
            }
            case WITHIN: {
                // first parameter: 'ogc:PropertyName' (cannot be empty)
                PropertyName param1 = parsePropertyName( xmlStream, false );
                xmlStream.nextTag();
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Within( param1, param2 );
            }
            }
        } catch ( UnknownCRSException e ) {
            throw new XMLParsingException( xmlStream, e.getMessage() );
        }
        xmlStream.nextTag();
        return spatialOperator;
    }

    /**
     * Return a String with all element names of the given enum class.
     * 
     * @param enumClass
     * @param map
     *            the operator type -> element name map
     * @return a coma separated list of element names
     */
    private static String elemNames( Class<? extends Enum<?>> enumClass, Map<? extends Enum<?>, QName> map ) {
        List<String> names = new LinkedList<String>();
        for ( Enum<?> e : enumClass.getEnumConstants() ) {
            QName qname = map.get( e );
            System.out.println( e );
            names.add( qname.toString() );
        }
        return ArrayUtils.join( ", ", names );
    }
}
