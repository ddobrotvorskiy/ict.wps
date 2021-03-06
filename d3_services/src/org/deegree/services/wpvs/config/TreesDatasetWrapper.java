//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/config/TreesDatasetWrapper.java $
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

package org.deegree.services.wpvs.config;

import static org.deegree.services.wpvs.PerspectiveViewService.createEnvelope;

import java.util.List;

import org.deegree.commons.configuration.BoundingBoxType;
import org.deegree.commons.configuration.ScaleDenominatorsType;
import org.deegree.commons.datasource.configuration.AbstractGeospatialDataSourceType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.services.wpvs.configuration.DEMTextureDataset;
import org.deegree.services.wpvs.configuration.DatasetDefinitions;
import org.deegree.services.wpvs.configuration.Trees;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackendInfo;

/**
 * The <code>ModelDatasetWrapper</code> class initilializes the trees from the backend.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 18171 $, $Date: 2009-06-17 21:00:07 +0700 (Срд, 17 Июн 2009) $
 *
 */
public class TreesDatasetWrapper extends ModelDatasetWrapper<TreeRenderer> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( TreesDatasetWrapper.class );

    /**
     * Construct a new model dataset wrapper for trees.
     *
     * @param defaultCRS
     * @param toLocalCRS
     * @param configAdapter
     *            to resolve any file urls against
     * @param dsd
     *            to get the {@link DEMTextureDataset} from.
     */
    public TreesDatasetWrapper( CRS defaultCRS, double[] toLocalCRS, XMLAdapter configAdapter, DatasetDefinitions dsd ) {
        super( defaultCRS, toLocalCRS, configAdapter );
        fillTreesFromDatasetDefinitions( dsd );
    }

    /**
     * Analyzes the {@link ModelDataset} from the {@link DatasetDefinitions}, fills the renderers with data from the
     * defined modelbackends and builds up a the constraint vectors for retrieval of the appropriate renderers.
     *
     * @param dsd
     */
    private void fillTreesFromDatasetDefinitions( DatasetDefinitions dsd ) {
        List<Trees> treesDatsets = dsd.getTrees();
        if ( !treesDatsets.isEmpty() ) {
            analyseAndExtractConstraints( treesDatsets, dsd.getBoundingBox(), dsd.getScaleDenominators(),
                                          dsd.getMaxPixelError() );
        } else {
            LOG.info( "No model dataset has been configured, no buildings, trees and prototypes will be available." );
        }
    }

    private void analyseAndExtractConstraints( List<Trees> treesDatasets, BoundingBoxType parentBBox,
                                               ScaleDenominatorsType parentScale, double parentMaxPixelError ) {
        if ( treesDatasets != null && !treesDatasets.isEmpty() ) {
            for ( Trees treeDS : treesDatasets ) {
                if ( treeDS != null ) {
                    // ModelDataset t = configuredModelDatasets.put( tds.getTitle(), tds );
                    if ( isUnAmbiguous( treeDS.getName() ) ) {
                        LOG.info( "The feature dataset with name: " + treeDS.getName() + " and title: "
                                  + treeDS.getTitle() + " had multiple definitions in your service configuration." );
                    } else {
                        clarifyInheritance( treeDS, parentBBox, parentScale, parentMaxPixelError );
                        loadTextureDirs( treeDS );
                        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends = initializeDatasources( treeDS );
                        initTrees( treeDS, backends );
                        analyseAndExtractConstraints( treeDS.getTrees(), treeDS.getBoundingBox(),
                                                      treeDS.getScaleDenominators(), treeDS.getMaxPixelError() );
                    }
                }
            }
        }
    }

    /**
     * Add the prototypes and building ordinates to the given backendinfo
     *
     * @param result
     *            to add the information to
     * @param backends
     *            to get the information from.
     */
    private void updateBackendInfo( ModelBackendInfo result,
                                    List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends,
                                    ModelBackend.Type infoType ) {
        for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
            if ( pair != null && pair.second != null ) {
                ModelBackend<?> mb = pair.second;
                ModelBackendInfo backendInfo = mb.getBackendInfo( infoType );
                result.add( backendInfo );
            }
        }
    }

    /**
     * Read and add the trees from the modelbackend and fill the renderer with them.
     *
     * @param configuredTreeDatasets
     * @param backends
     */
    private void initTrees( Trees configuredTreeDatasets,
                            List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
        ModelBackendInfo info = new ModelBackendInfo();
        updateBackendInfo( info, backends, ModelBackend.Type.TREE );

        // RB: Todo configure this value.
        int numberOfObjectsInLeaf = 250;
        if ( configuredTreeDatasets != null ) {
            Envelope buildingDomain = createEnvelope( configuredTreeDatasets.getBoundingBox(), getDefaultCRS(), null );
            TreeRenderer configuredRender = new TreeRenderer( buildingDomain, numberOfObjectsInLeaf,
                                                              configuredTreeDatasets.getMaxPixelError() );
            // Iterate over all configured datasources and add the trees from the datasources which match the scale
            // and envelope of the configured tree dataset.
            for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
                if ( pair != null && pair.first != null ) {
                    AbstractGeospatialDataSourceType ds = pair.first;
                    Envelope dsEnv = createEnvelope( ds.getBBoxConstraint().getBoundingBox(), getDefaultCRS(), null );
                    if ( buildingDomain.intersects( dsEnv )
                         && scalesFit( configuredTreeDatasets.getScaleDenominators(),
                                       ds.getScaleConstraint().getScaleDenominators() ) ) {
                        ModelBackend<?> modelBackend = pair.second;
                        if ( modelBackend != null ) {
                            modelBackend.loadTrees( configuredRender );
                        }

                    }
                }
            }
            addConstraint( configuredTreeDatasets.getName(), configuredRender, configuredTreeDatasets.getBoundingBox(),
                           configuredTreeDatasets.getScaleDenominators() );
        }
    }
}
