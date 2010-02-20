//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wfs;

import static org.deegree.services.i18n.Messages.get;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.datasource.configuration.FeatureStoreType;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.types.FeatureType;
import org.deegree.services.wfs.configuration.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WFService {

    private static final Logger LOG = LoggerFactory.getLogger( WFService.class );

    private final Map<QName, FeatureStore> ftNameToStore = new HashMap<QName, FeatureStore>();

    private final Collection<FeatureStore> stores = new LinkedHashSet<FeatureStore>();

    private final Map<String, String> prefixToNs = new LinkedHashMap<String, String>();

    private Set<String> hintedNamespaces;

    private int indexPrefix = 0;

    private Map<String, String> targetNsToPrefix = new LinkedHashMap<String, String>();

    /**
     * @param sc
     * @param baseURL
     * @throws FeatureStoreException
     */
    public void init( ServiceConfiguration sc, String baseURL )
                            throws FeatureStoreException {
        LOG.info( "Initializing/looking up configured feature stores." );

        // filling prefix map with the provided NamespaceHints
        hintedNamespaces = new HashSet<String>();

        for ( JAXBElement<? extends FeatureStoreType> fsConfigEl : sc.getFeatureStore() ) {
            FeatureStoreType fsConfig = fsConfigEl.getValue();
            FeatureStore fs = FeatureStoreManager.create( fsConfig, baseURL );
            addStore( fs );

            addNotYetHintedNamespaces( fs.getSchema().getFeatureTypes() );
        }

        LOG.debug( "The following prefix-to-namespace and namespace-to-prefix bindings are used for resolution..." );
        for ( String prefix : prefixToNs.keySet() ) {
            LOG.debug( prefix + " --> " + prefixToNs.get( prefix ) );
        }
        for ( String ns : targetNsToPrefix.keySet() ) {
            LOG.debug( ns + " <-- " + targetNsToPrefix.get( ns ) );
        }
    }

    private void addNotYetHintedNamespaces( FeatureType[] featureTypes ) {
        for ( int i = 0; i < featureTypes.length; i++ ) {
            if ( !hintedNamespaces.contains( featureTypes[i].getName().getNamespaceURI() ) ) {
                hintedNamespaces.add( featureTypes[i].getName().getNamespaceURI() );

                if ( featureTypes[i].getName().getPrefix() != null
                     && !featureTypes[i].getName().getPrefix().equals( "" ) ) {
                    // add the prefixes that were forgotten to be added in the NamespaceHint elements from the
                    // configuration
                    prefixToNs.put( featureTypes[i].getName().getPrefix(), featureTypes[i].getName().getNamespaceURI() );
                } else {
                    // the elements that have no prefix must be in an application schema namespace
                    targetNsToPrefix.put( featureTypes[i].getName().getNamespaceURI(), "app" + indexPrefix );
                    indexPrefix++;
                }
            }
        }
    }

    /**
     * 
     */
    public void destroy() {
        // TODO
    }

    /**
     * Get the prefix-to-namespace map that is constructed from the NamespaceHints in the configuration
     * 
     * @return the prefix-to-namespace map
     */
    public Map<String, String> getPrefixToNs() {
        return prefixToNs;
    }

    /**
     * Get the namespace-to-prefix bindings for the namespaces of the application schemas.
     * 
     * @return the namespace-to-prefix map
     */
    public Map<String, String> getTargetNsToPrefix() {
        return targetNsToPrefix;
    }

    /**
     * Returns the {@link FeatureStore} instance which is responsible for the specified feature type.
     * 
     * @param ftName
     *            name of the {@link FeatureType}
     * @return the responsible {@link FeatureStore} or <code>null</code> if no such store exists, i.e. the specified
     *         feature type is not served
     */
    public FeatureStore getStore( QName ftName ) {
        return ftNameToStore.get( ftName );
    }

    /**
     * Returns all registered {@link FeatureStore} instances.
     * 
     * @return all registered feature stores
     */
    public FeatureStore[] getStores() {
        Set<FeatureStore> stores = new HashSet<FeatureStore>( ftNameToStore.values() );
        return stores.toArray( new FeatureStore[stores.size()] );
    }

    /**
     * Registers a new {@link FeatureStore} to the WFS.
     * 
     * @param fs
     *            store to be registered
     */
    public void addStore( FeatureStore fs ) {
        synchronized ( this ) {
            if ( stores.contains( fs ) ) {
                String msg = get( "WFS_FEATURESTORE_ALREADY_REGISTERED", fs );
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }
            for ( FeatureType ft : fs.getSchema().getFeatureTypes() ) {
                if ( ftNameToStore.containsKey( ft.getName() ) ) {
                    String msg = get( "WFS_FEATURETYPE_ALREADY_SERVED", ft.getName() );
                    LOG.error( msg );
                    throw new IllegalArgumentException( msg );
                }
            }
            stores.add( fs );
            for ( FeatureType ft : fs.getSchema().getFeatureTypes() ) {
                ftNameToStore.put( ft.getName(), fs );
            }
        }
    }

    /**
     * Deregisters the specified {@link FeatureStore} from the WFS.
     * 
     * @param fs
     *            store to be registered
     */
    public void removeStore( FeatureStore fs ) {
        synchronized ( this ) {

        }
    }

    public QName[] getFeatureTypeNames() {
        return ftNameToStore.keySet().toArray( new QName[ftNameToStore.size()] );
    }

    /**
     * Returns the {@link FeatureType} with the given name.
     * 
     * @param ftName
     *            feature type to look up
     * @return feature type with the given name, or <code>null</code> if this feature type is not served
     */
    public FeatureType getFeatureType( QName ftName ) {
        FeatureType ft = null;
        FeatureStore fs = ftNameToStore.get( ftName );
        if ( fs != null ) {
            ft = fs.getSchema().getFeatureType( ftName );
        }
        return ft;
    }

    /**
     * Returns all {@link FeatureType}s.
     * 
     * @return served feature types, may be empty, but never <code>null</code>
     */
    public Collection<FeatureType> getFeatureTypes() {
        List<FeatureType> fts = new ArrayList<FeatureType>( ftNameToStore.size() );
        for ( QName ftName : ftNameToStore.keySet() ) {
            fts.add( getFeatureType( ftName ) );
        }
        return fts;
    }
}
