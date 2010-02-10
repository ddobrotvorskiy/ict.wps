//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/gml/feature/StandardGMLFeatureProps.java $
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
package org.deegree.gml.feature;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.feature.Feature;
import org.deegree.feature.GenericProperty;
import org.deegree.feature.Property;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.props.GMLStdProps;

/**
 * Version-agnostic representation of the standard properties that any GML {@link Feature} allows for.
 * 
 * @see GMLStdProps
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 21659 $, $Date: 2009-12-23 19:29:54 +0600 (Срд, 23 Дек 2009) $
 */
public class StandardGMLFeatureProps extends GMLStdProps {

    /** GML 2 standard property type 'gml:boundedBy' */
    public static final PropertyType<Envelope> PT_BOUNDED_BY_GML2;

    /** GML 3.0/3.1 standard property type 'gml:boundedBy' */
    public static final PropertyType<Envelope> PT_BOUNDED_BY_GML31;

    /** GML 3.2 standard property type 'gml:boundedBy' */
    public static final PropertyType<Envelope> PT_BOUNDED_BY_GML32;

    private final static Map<QName, PropertyType<?>> GML2PropNameToPropType = new LinkedHashMap<QName, PropertyType<?>>();

    private final static Map<QName, PropertyType<?>> GML31PropNameToPropType = new LinkedHashMap<QName, PropertyType<?>>();

    private final static Map<QName, PropertyType<?>> GML32PropNameToPropType = new LinkedHashMap<QName, PropertyType<?>>();

    static {
        // TODO correct this (this should be a BoundingShapeType which permits BBOX or NULL)
        PT_BOUNDED_BY_GML2 = new EnvelopePropertyType( new QName( GMLNS, "boundedBy" ), 0, 1, false, null );

        // TODO correct this (this should be a BoundingShapeType which permits BBOX or NULL)
        PT_BOUNDED_BY_GML31 = new EnvelopePropertyType( new QName( GMLNS, "boundedBy" ), 0, 1, false, null );

        // TODO correct this (this should be a BoundingShapeType which permits BBOX or NULL)
        PT_BOUNDED_BY_GML32 = new EnvelopePropertyType( new QName( GML3_2_NS, "boundedBy" ), 0, 1, false, null );

        // fill lookup maps
        GML2PropNameToPropType.put( PT_DESCRIPTION_GML2.getName(), PT_DESCRIPTION_GML2 );
        GML2PropNameToPropType.put( PT_NAME_GML2.getName(), PT_NAME_GML2 );
        GML2PropNameToPropType.put( PT_BOUNDED_BY_GML2.getName(), PT_BOUNDED_BY_GML2 );

        GML31PropNameToPropType.put( PT_META_DATA_PROPERTY_GML31.getName(), PT_META_DATA_PROPERTY_GML31 );
        GML31PropNameToPropType.put( PT_DESCRIPTION_GML31.getName(), PT_DESCRIPTION_GML31 );
        GML31PropNameToPropType.put( PT_NAME_GML31.getName(), PT_NAME_GML31 );
        GML31PropNameToPropType.put( PT_BOUNDED_BY_GML31.getName(), PT_BOUNDED_BY_GML31 );

        GML32PropNameToPropType.put( PT_META_DATA_PROPERTY_GML32.getName(), PT_META_DATA_PROPERTY_GML32 );
        GML32PropNameToPropType.put( PT_DESCRIPTION_GML32.getName(), PT_DESCRIPTION_GML32 );
        GML32PropNameToPropType.put( PT_DESCRIPTION_REFERENCE_GML32.getName(), PT_DESCRIPTION_REFERENCE_GML32 );
        GML32PropNameToPropType.put( PT_IDENTIFIER_GML32.getName(), PT_IDENTIFIER_GML32 );
        GML32PropNameToPropType.put( PT_NAME_GML32.getName(), PT_NAME_GML32 );
        GML32PropNameToPropType.put( PT_BOUNDED_BY_GML32.getName(), PT_BOUNDED_BY_GML32 );
    }

    private Envelope boundedBy;

    /**
     * Creates a new {@link StandardGMLFeatureProps} instance.
     * 
     * @param metadata
     *            metadata values, may be <code>null</code>
     * @param description
     *            description, may be <code>null</code>
     * @param identifier
     *            identifier, may be <code>null</code>
     * @param names
     *            names, may be <code>null</code>
     * @param boundedBy
     *            bounding box, may be <code>null</code>
     */
    public StandardGMLFeatureProps( Object[] metadata, StringOrRef description, CodeType identifier, CodeType[] names,
                                    Envelope boundedBy ) {
        super( metadata, description, identifier, names );
        this.boundedBy = boundedBy;
    }

    /**
     * Returns the <code>boundedBy</code> property value.
     * 
     * @return <code>boundedBy</code> property value, or <code>null</code> if not defined
     */
    public Envelope getBoundedBy() {
        return boundedBy;
    }

    /**
     * Sets the value for the <code>boundedBy</code> property.
     * 
     * @param boundedBy
     *            the value for the <code>boundedBy</code> property (or <code>null</code> to clear it)
     */
    public void setBoundedBy( Envelope boundedBy ) {
        this.boundedBy = boundedBy;
    }

    /**
     * Returns the corresponding {@link Property} instances for a specific GML version.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @return corresponding {@link Property} instances, may be empty, but never <code>null</code>
     */
    public Collection<Property<?>> getProperties( GMLVersion version ) {
        List<Property<?>> props = new ArrayList<Property<?>>();
        switch ( version ) {
        case GML_2:
            if ( description != null ) {
                props.add( new GenericProperty<String>( PT_DESCRIPTION_GML2, description.getString() ) );
            }
            if ( names.length > 0 ) {
                props.add( new GenericProperty<String>( PT_NAME_GML2, names[0].getCode() ) );
            }
            if ( boundedBy != null ) {
                props.add( new GenericProperty<Envelope>( PT_BOUNDED_BY_GML2, boundedBy ) );
            }
            break;
        case GML_30:
        case GML_31:
            if ( metadata != null ) {
                for ( Object metadataItem : metadata ) {
                    props.add( new GenericProperty<Object>( PT_META_DATA_PROPERTY_GML31, metadataItem ) );
                }
            }
            if ( description != null ) {
                props.add( new GenericProperty<StringOrRef>( PT_DESCRIPTION_GML31, description ) );
            }
            if ( names != null ) {
                for ( CodeType name : names ) {
                    props.add( new GenericProperty<CodeType>( PT_NAME_GML31, name ) );
                }
            }
            if ( boundedBy != null ) {
                props.add( new GenericProperty<Envelope>( PT_BOUNDED_BY_GML31, boundedBy ) );
            }
            break;
        case GML_32:
            throw new UnsupportedOperationException( "Not implemented yet." );
        }
        return props;
    }

    /**
     * Returns the standard GML property with the given name.
     * 
     * @param propName
     *            name of the requested property, must not be <code>null</code>
     * @param version
     *            GML version, must not be <code>null</code>
     * @return corresponding {@link Property} instance or <code>null</code> if no such property exists
     */
    public Property<?> getProperty( QName propName, GMLVersion version ) {
        Property<?> prop = null;
        switch ( version ) {
        case GML_2:
            if ( PT_DESCRIPTION_GML2.getName().equals( propName ) && description != null ) {
                prop = new GenericProperty<String>( PT_DESCRIPTION_GML2, description.getString() );
            } else if ( PT_NAME_GML2.getName().equals( propName ) && names != null && names.length > 0 ) {
                prop = new GenericProperty<String>( PT_NAME_GML2, names[0].getCode() );
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) && boundedBy != null ) {
                prop = new GenericProperty<Envelope>( PT_BOUNDED_BY_GML2, boundedBy );
            }
            break;
        case GML_30:
        case GML_31:
            if ( PT_META_DATA_PROPERTY_GML31.getName().equals( propName ) && metadata.length > 0 ) {
                prop = new GenericProperty<Object>( PT_META_DATA_PROPERTY_GML31, metadata[0] );
            } else if ( PT_DESCRIPTION_GML31.getName().equals( propName ) && description != null ) {
                prop = new GenericProperty<StringOrRef>( PT_DESCRIPTION_GML31, description );
            } else if ( PT_NAME_GML31.getName().equals( propName ) && names.length > 0 ) {
                prop = new GenericProperty<CodeType>( PT_NAME_GML31, names[0] );
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) ) {
                if ( boundedBy != null ) {
                    prop = new GenericProperty<Envelope>( PT_BOUNDED_BY_GML31, boundedBy );
                }
            }
            break;
        case GML_32:
            throw new UnsupportedOperationException( "Not implemented yet." );
        }
        return prop;
    }

    /**
     * Returns the value of the standard GML property with the given name.
     * 
     * @param propName
     *            name of the requested property, must not be <code>null</code>
     * @param version
     *            GML version, must not be <code>null</code>
     * @return corresponding value or <code>null</code> if no such property exists
     */
    public Object getPropertyValue( QName propName, GMLVersion version ) {
        Object value = null;
        switch ( version ) {
        case GML_2:
            if ( PT_DESCRIPTION_GML2.getName().equals( propName ) && description != null ) {
                value = description;
            } else if ( PT_NAME_GML2.getName().equals( propName ) && names != null && names.length > 0 ) {
                value = names[0].getCode();
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) && boundedBy != null ) {
                value = boundedBy;
            }
            break;
        case GML_30:
        case GML_31:
            if ( PT_META_DATA_PROPERTY_GML31.getName().equals( propName ) && metadata.length > 0 ) {
                value = metadata[0];
            } else if ( PT_DESCRIPTION_GML31.getName().equals( propName ) && description != null ) {
                value = description;
            } else if ( PT_NAME_GML31.getName().equals( propName ) && names.length > 0 ) {
                value = names[0];
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) ) {
                if ( boundedBy != null ) {
                    value = boundedBy;
                }
            }
            break;
        case GML_32:
            throw new UnsupportedOperationException( "Not implemented yet." );
        }
        return value;
    }

    /**
     * Returns the standard GML properties with the given name.
     * 
     * @param propName
     *            name of the requested properties, must not be <code>null</code>
     * @param version
     *            GML version, must not be <code>null</code>
     * @return corresponding {@link Property} instances or <code>null</code> if no such property exists
     */
    public Property<?>[] getProperties( QName propName, GMLVersion version ) {
        Property<?>[] props = null;
        switch ( version ) {
        case GML_2:
            if ( PT_DESCRIPTION_GML2.getName().equals( propName ) && description != null ) {
                props = new Property<?>[] { new GenericProperty<String>( PT_DESCRIPTION_GML2, description.getString() ) };
            } else if ( PT_NAME_GML2.getName().equals( propName ) && names != null && names.length > 0 ) {
                props = new Property<?>[] { new GenericProperty<String>( PT_NAME_GML2, names[0].getCode() ) };
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) && boundedBy != null ) {
                props = new Property<?>[] { new GenericProperty<Envelope>( PT_BOUNDED_BY_GML2, boundedBy ) };
            }
            break;
        case GML_30:
        case GML_31:
            if ( PT_META_DATA_PROPERTY_GML31.getName().equals( propName ) && metadata.length > 0 ) {
                props = new Property<?>[metadata.length];
                for ( int i = 0; i < metadata.length; i++ ) {
                    props[i] = new GenericProperty<Object>( PT_NAME_GML31, metadata[i] );
                }
            } else if ( PT_DESCRIPTION_GML31.getName().equals( propName ) && description != null ) {
                props = new Property<?>[] { new GenericProperty<StringOrRef>( PT_DESCRIPTION_GML31, description ) };
            } else if ( PT_NAME_GML31.getName().equals( propName ) && names.length > 0 ) {
                props = new Property<?>[names.length];
                for ( int i = 0; i < names.length; i++ ) {
                    props[i] = new GenericProperty<CodeType>( PT_NAME_GML31, names[i] );
                }
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) ) {
                if ( boundedBy != null ) {
                    props = new Property<?>[] { new GenericProperty<Envelope>( PT_BOUNDED_BY_GML31, boundedBy ) };
                }
            }
            break;
        case GML_32:
            throw new UnsupportedOperationException( "Not implemented yet." );
        }
        return props;
    }

    /**
     * Returns the values of the standard GML properties with the given name.
     * 
     * @param propName
     *            name of the requested properties, must not be <code>null</code>
     * @param version
     *            GML version, must not be <code>null</code>
     * @return corresponding {@link Property} values or <code>null</code> if no such property exists
     */
    public Object[] getPropertiesValues( QName propName, GMLVersion version ) {
        Object[] values = null;
        switch ( version ) {
        case GML_2:
            if ( PT_DESCRIPTION_GML2.getName().equals( propName ) && description != null ) {
                values = new Object[] { description };
            } else if ( PT_NAME_GML2.getName().equals( propName ) && names != null && names.length > 0 ) {
                values = new Object[] { names[0].getCode() };
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) && boundedBy != null ) {
                values = new Object[] { boundedBy };
            }
            break;
        case GML_30:
        case GML_31:
            if ( PT_META_DATA_PROPERTY_GML31.getName().equals( propName ) && metadata.length > 0 ) {
                values = metadata;
            } else if ( PT_DESCRIPTION_GML31.getName().equals( propName ) && description != null ) {
                values = new Object[] { description };
            } else if ( PT_NAME_GML31.getName().equals( propName ) && names.length > 0 ) {
                values = names;
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) ) {
                if ( boundedBy != null ) {
                    values = new Object[] { boundedBy };
                }
            }
            break;
        case GML_32:
            throw new UnsupportedOperationException( "Not implemented yet." );
        }
        return values;
    }

    /**
     * Returns the declaration for standard properties that any GML feature allows for.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @return standard GML property types, in expected order
     */
    public static Collection<PropertyType<?>> getPropertyTypes( GMLVersion version ) {

        Collection<PropertyType<?>> pts = null;
        switch ( version ) {
        case GML_2:
            pts = GML2PropNameToPropType.values();
            break;
        case GML_30:
        case GML_31:
            pts = GML31PropNameToPropType.values();
            break;
        case GML_32:
            pts = GML32PropNameToPropType.values();
            break;
        }
        return pts;
    }

    /**
     * Returns the standard GML property declaration with the given name for the specified GML version.
     * 
     * @param propName
     *            qualified name of the property
     * @param version
     *            GML version, must not be <code>null</code>
     * @return standard GML property type, or <code>null</code> if no such property type exists
     */
    public static PropertyType<?> getPropertyType( QName propName, GMLVersion version ) {
        PropertyType<?> pt = null;
        switch ( version ) {
        case GML_2:
            pt = GML2PropNameToPropType.get( propName );
            break;
        case GML_30:
        case GML_31:
            pt = GML31PropNameToPropType.get( propName );
            break;
        case GML_32:
            pt = GML32PropNameToPropType.get( propName );
            break;

        }
        return pt;
    }

    // TODO respect occurence
    public boolean setPropertyValue( QName propName, int occurence, Object value, GMLVersion version ) {

        switch ( version ) {
        case GML_2: {
            if ( PT_DESCRIPTION_GML2.getName().equals( propName ) ) {
                description = new StringOrRef( (String) value, null );
            } else if ( PT_NAME_GML2.getName().equals( propName ) ) {
                if ( names == null || names.length == 0 ) {
                    names = new CodeType[1];
                }
                if ( value != null ) {
                    names[0] = new CodeType( (String) value, null );
                } else {
                    // remove first if present
                    List<CodeType> subList = Arrays.asList( names ).subList( names.length > 0 ? 1 : 0, names.length );
                    names = subList.toArray( new CodeType[subList.size()] );
                }
            } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) ) {
                boundedBy = (Envelope) value;
            }
            break;
        }
        case GML_30:
        case GML_31: {
            if ( PT_META_DATA_PROPERTY_GML31.getName().equals( propName ) ) {
                if ( metadata == null || metadata.length == 0 ) {
                    metadata = new Object[1];
                }
                if ( value != null ) {
                    metadata[0] = value;
                } else {
                    // remove first if present
                    List<Object> subList = Arrays.asList( metadata ).subList( metadata.length > 0 ? 1 : 0,
                                                                              metadata.length );
                    metadata = subList.toArray( new CodeType[subList.size()] );
                }
            } else if ( PT_DESCRIPTION_GML31.getName().equals( propName ) ) {
                description = (StringOrRef) value;
            } else if ( PT_NAME_GML31.getName().equals( propName ) ) {
                if ( names == null || names.length == 0 ) {
                    names = new CodeType[1];
                }
                if ( value != null ) {
                    names[0] = (CodeType) value;
                } else {
                    // remove first if present
                    List<CodeType> subList = Arrays.asList( names ).subList( names.length > 0 ? 1 : 0, names.length );
                    names = subList.toArray( new CodeType[subList.size()] );
                }
            } else if ( PT_BOUNDED_BY_GML31.getName().equals( propName ) ) {
                boundedBy = (Envelope) value;
            }
            break;
        }
        case GML_32: {
            throw new UnsupportedOperationException( "Not implemented yet." );
        }
        }
        return false;
    }

    /**
     * Creates a {@link StandardGMLFeatureProps} instances from the given list of {@link Property} objects and returns
     * the remaining properties that are not GML default properties.
     * 
     * @param props
     * @param version
     *            GML version, must not be <code>null</code>
     * @return created instance and remaining (non-default) properties
     */
    public static Pair<StandardGMLFeatureProps, List<Property<?>>> create( List<Property<?>> props, GMLVersion version ) {

        List<Object> metadata = new LinkedList<Object>();
        StringOrRef description = null;
        CodeType identifier = null;
        List<CodeType> names = new LinkedList<CodeType>();
        Envelope boundedBy = null;
        int firstCustomPropIndex = 0;

        switch ( version ) {
        case GML_2: {
            for ( Property<?> property : props ) {
                QName propName = property.getName();
                if ( CommonNamespaces.GMLNS.equals( propName.getNamespaceURI() ) ) {
                    if ( PT_DESCRIPTION_GML2.getName().equals( propName ) ) {
                        description = new StringOrRef( (String) property.getValue(), null );
                        firstCustomPropIndex++;
                    } else if ( PT_NAME_GML2.getName().equals( propName ) ) {
                        names.add( new CodeType( (String) property.getValue() ) );
                        firstCustomPropIndex++;
                    } else if ( PT_BOUNDED_BY_GML2.getName().equals( propName ) ) {
                        boundedBy = (Envelope) property.getValue();
                        firstCustomPropIndex++;
                    }
                } else {
                    break;
                }
            }
            break;
        }
        case GML_30:
        case GML_31: {
            for ( Property<?> property : props ) {
                QName propName = property.getName();
                if ( CommonNamespaces.GMLNS.equals( propName.getNamespaceURI() ) ) {
                    if ( PT_META_DATA_PROPERTY_GML31.getName().equals( propName ) ) {
                        metadata.add( property.getValue() );
                        firstCustomPropIndex++;
                    } else if ( PT_DESCRIPTION_GML31.getName().equals( propName ) ) {
                        description = (StringOrRef) property.getValue();
                        firstCustomPropIndex++;
                    } else if ( PT_NAME_GML31.getName().equals( propName ) ) {
                        names.add( (CodeType) property.getValue() );
                        firstCustomPropIndex++;
                    } else if ( PT_BOUNDED_BY_GML31.equals( propName ) ) {
                        boundedBy = (Envelope) property.getValue();
                        firstCustomPropIndex++;
                    }
                } else {
                    break;
                }
            }
            break;
        }
        case GML_32: {
            throw new UnsupportedOperationException( "Not implemented yet." );
        }
        }
        StandardGMLFeatureProps gmlProps = new StandardGMLFeatureProps(
                                                                        metadata.toArray( new Object[metadata.size()] ),
                                                                        description, identifier,
                                                                        names.toArray( new CodeType[names.size()] ),
                                                                        boundedBy );
        List<Property<?>> nonGMLProps = props.subList( firstCustomPropIndex, props.size() );
        return new Pair<StandardGMLFeatureProps, List<Property<?>>>( gmlProps, nonGMLProps );
    }

    @Override
    public String toString() {
        String s = "";
        for ( int i = 0; i < names.length; i++ ) {
            s += "name={" + names[i] + "}";
            if ( i != names.length - 1 ) {
                s += ',';
            }
        }
        if ( boundedBy != null ) {
            if ( s.length() > 0 ) {
                s += ",";
            }
            s += "boundedBy={" + boundedBy + "}";
        }
        return s;
    }
}
