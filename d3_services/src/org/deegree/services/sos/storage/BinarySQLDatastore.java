//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/sos/storage/BinarySQLDatastore.java $
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
package org.deegree.services.sos.storage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.deegree.commons.utils.JDBCUtils;
import org.deegree.protocol.sos.filter.BeginFilter;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.EndFilter;
import org.deegree.protocol.sos.filter.FilterCollection;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.deegree.protocol.sos.filter.TimeInstantFilter;
import org.deegree.protocol.sos.time.SamplingTime;
import org.deegree.protocol.sos.time.TimePeriod;
import org.deegree.services.sos.SOServiceExeption;
import org.deegree.services.sos.model.MeasurementBase;
import org.deegree.services.sos.model.Observation;
import org.deegree.services.sos.model.Procedure;
import org.deegree.services.sos.model.Property;
import org.deegree.services.sos.model.Result;
import org.deegree.services.sos.model.SimpleIntegerResult;
import org.deegree.services.sos.model.SimpleMeasurement;
import org.deegree.services.sos.model.SimpleNullResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This datastore is able to manage data with a high sampling rate. The data is read from DB records with SQL byte
 * arrays. The current implementation allows each record to store measurements that occured within one second. The
 * number of measurements per seconds and the time interval between them can be configured with the ms_per_record and
 * number_of_records options. (e.g. if you have 10ms sampling rate: number_of_records = 100, ms_per_record=10)
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: aionita $
 * 
 * @version $Revision: 19378 $, $Date: 2009-08-28 15:26:53 +0700 (Птн, 28 Авг 2009) $
 * 
 */
public class BinarySQLDatastore extends SimpleObservationDatastore {

    private static final Logger LOG = LoggerFactory.getLogger( BinarySQLDatastore.class );

    private static final Calendar GMT_TIMEZONE = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );

    private final int samplingPeriodMS;

    private final int numOfMeasurements;

    private static final int T_SECOND = 1000; // * MILLISECONDS

    private static final int T_MINUTE = 60 * T_SECOND;

    private static final int T_HOUR = 60 * T_MINUTE;

    private static final int T_DAY = 24 * T_HOUR;

    /**
     * @param dsConfig
     */
    public BinarySQLDatastore( DatastoreConfiguration dsConfig ) {
        super( dsConfig );
        this.samplingPeriodMS = Integer.parseInt( dsConfig.getOption( "ms_sampling_period" ) );
        this.numOfMeasurements = Integer.parseInt( dsConfig.getOption( "number_of_measurements" ) );

        int msPerRecord = this.samplingPeriodMS * this.numOfMeasurements;
        if ( msPerRecord != T_SECOND && msPerRecord != T_MINUTE && msPerRecord != T_HOUR && msPerRecord != T_DAY ) {
            LOG.warn(
                      "BinarySQLDatastore ({}:{}) is not aligned to seconds, minutes, hours, or days (ms_sampling_period * number_of_measurements = {}ms). Time filter may not work properly.",
                      new Object[] { dsConfig.getJdbcConnection(), dsConfig.getTableName(), msPerRecord } );
        }
    }

    @Override
    public SamplingTime getSamplingTime() {
        SamplingTime time = super.getSamplingTime();
        if ( time instanceof TimePeriod ) {
            TimePeriod timePeriod = (TimePeriod) time;
            extendTime( timePeriod );
        }
        return time;
    }

    /**
     * extend the time period for one second, to include all internal binary samples
     */
    private void extendTime( TimePeriod timePeriod ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( timePeriod.getEnd() );
        cal.add( Calendar.MILLISECOND, samplingPeriodMS * numOfMeasurements );
        timePeriod.extend( cal.getTime() );
    }

    @Override
    public Observation getObservation( FilterCollection filter )
                            throws SOServiceExeption {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Map<Property, String> properties = getPropertyMap( filter );

            Observation measurements = new Observation( properties.keySet() );

            MeasurementBase measurementBase = new MeasurementBase( "", // TODO
                                                                   properties.keySet() );

            conn = getConnection();
            List<String> columns = buildColumnList( properties );

            stmt = getStatement( filter, columns, conn );
            resultSet = stmt.executeQuery();

            TimestampFilter timestampFilter = new TimestampFilter( filter.getTimeFilter() );

            List<ByteArrayResult> results = new ArrayList<ByteArrayResult>( properties.size() );
            while ( resultSet.next() ) {
                results.clear();
                for ( Entry<Property, String> property : properties.entrySet() ) {
                    byte[] value = resultSet.getBytes( property.getValue() );
                    results.add( new ByteArrayResult( property.getKey(), value ) );
                }
                Date date = resultSet.getTimestamp( timeColumn, GMT_TIMEZONE );
                Procedure p = getProcedure( resultSet );
                if ( p != null ) {
                    generateMeasurements( date, p, results, measurementBase, measurements, timestampFilter );
                } else {
                    LOG.error( "no procedure found for result set {}, {}", date, results );
                }
            }

            return measurements;
        } catch ( SQLException e ) {
            LOG.error( "error while retrieving on observation", e );
            throw new SOServiceExeption( "internal error, unable to retrieve observation from datastore" );
        } catch ( FilterException e ) {
            throw new SOServiceExeption( "unable to evaluate filter", e );
        } finally {
            JDBCUtils.close( resultSet );
            JDBCUtils.close( stmt );
            JDBCUtils.close( conn );
        }
    }

    private List<String> buildColumnList( Map<Property, String> properties ) {
        List<String> columns = new LinkedList<String>( properties.values() );
        if ( procColumn != null ) {
            columns.add( procColumn );
        }
        columns.add( timeColumn );
        return columns;
    }

    private void generateMeasurements( Date date, Procedure p, List<ByteArrayResult> results,
                                       MeasurementBase measurementBase, Observation measurements,
                                       TimestampFilter timestampFilter ) {
        ArrayList<int[]> intValues = new ArrayList<int[]>( results.size() );
        ArrayList<Property> propValues = new ArrayList<Property>( results.size() );
        for ( ByteArrayResult result : results ) {
            intValues.add( result.getAsInts() );
            propValues.add( result.getProperty() );
        }

        long currentRecordDate = date.getTime();

        int numResults = intValues.get( 0 ).length;
        int numProp = propValues.size();
        for ( int i = 0; i < numResults; i++ ) {
            if ( timestampFilter.keep( currentRecordDate ) ) {
                List<Result> simpleResults = new ArrayList<Result>( numProp );
                for ( int propIdx = 0; propIdx < numProp; propIdx++ ) {
                    Result result;
                    if ( intValues.get( propIdx ).length > 0 ) {
                        result = new SimpleIntegerResult( intValues.get( propIdx )[i], propValues.get( propIdx ) );
                    } else {
                        result = new SimpleNullResult( propValues.get( propIdx ) );
                    }
                    simpleResults.add( result );
                }
                SimpleMeasurement measurement = new SimpleMeasurement( measurementBase, new Date( currentRecordDate ),
                                                                       p, simpleResults );
                measurements.add( measurement );
            }
            currentRecordDate += samplingPeriodMS;
        }

    }

    @Override
    protected PreparedStatement getStatement( FilterCollection filter, Collection<String> collection, Connection conn )
                            throws SQLException, FilterException {
        FilterCollection extendedFilter = new FilterCollection();

        extendedFilter.add( filter.getProcedureFilter() );
        extendedFilter.add( filter.getPropertyFilter() );
        extendedFilter.add( filter.getResultFilter() );
        extendedFilter.add( filter.getSpatialFilter() );

        // we need to alter the time filter, since a filter time may not exactly match the timestamp of the database
        // record.

        // cut off the milliseconds and set filter inclusive, otherwise the DB will not return all records.
        // e.g. a begin of 12:30:00.500 will not return the 12:30.00.000 data block

        for ( TimeFilter timeFilter : filter.getTimeFilter() ) {
            if ( timeFilter instanceof BeginFilter ) {
                BeginFilter beginFilter = (BeginFilter) timeFilter;
                extendedFilter.add( new BeginFilter( roundDownDate( beginFilter.getBegin() ), true ) );
            } else if ( timeFilter instanceof DurationFilter ) {
                DurationFilter durationFilter = (DurationFilter) timeFilter;
                extendedFilter.add( new DurationFilter( roundDownDate( durationFilter.getBegin() ), true,
                                                        durationFilter.getEnd(), durationFilter.isInclusiveEnd() ) );
            } else if ( timeFilter instanceof TimeInstantFilter ) {
                TimeInstantFilter timeinstantFilter = (TimeInstantFilter) timeFilter;
                extendedFilter.add( new TimeInstantFilter( roundDownDate( timeinstantFilter.getInstant() ) ) );
            } else {
                extendedFilter.add( timeFilter );
            }
        }
        return super.getStatement( extendedFilter, collection, conn );
    }

    private Date roundDownDate( Date date ) {
        Calendar tmp = Calendar.getInstance();
        tmp.setTime( date );
        tmp.set( Calendar.MILLISECOND, 0 );
        if ( samplingPeriodMS * numOfMeasurements > T_SECOND ) {
            tmp.set( Calendar.SECOND, 0 );
            if ( samplingPeriodMS * numOfMeasurements > T_MINUTE ) {
                tmp.set( Calendar.MINUTE, 0 );
                if ( samplingPeriodMS * numOfMeasurements > T_HOUR ) {
                    tmp.set( Calendar.HOUR, 0 );
                }
            }
        }
        return tmp.getTime();
    }

    private static class ByteArrayResult {
        private Property prop;

        private byte[] values;

        public ByteArrayResult( Property prop, byte[] values ) {
            this.prop = prop;
            this.values = values;
        }

        public Property getProperty() {
            return prop;
        }

        public int[] getAsInts() {
            int[] result = new int[values.length / 4];
            ByteBuffer buf = ByteBuffer.wrap( values );
            buf.position( 0 );
            IntBuffer intBuf = buf.asIntBuffer();
            intBuf.get( result );

            return result;
        }
    }

    /**
     * This class can make filter descisions based on a timestamp and multiple {@link TimeFilter}.
     */
    private class TimestampFilter {
        private List<SimpleTimestampFilter> filter;

        public TimestampFilter( List<TimeFilter> timeFilters ) {
            this.filter = new LinkedList<SimpleTimestampFilter>();
            for ( TimeFilter filter : timeFilters ) {
                this.filter.add( convertFilter( filter ) );
            }
        }

        @SuppressWarnings("synthetic-access")
        private SimpleTimestampFilter convertFilter( TimeFilter filter ) {
            if ( filter instanceof DurationFilter ) {
                final DurationFilter durationFilter = (DurationFilter) filter;

                return new SimpleTimestampFilter() {
                    @Override
                    public boolean filterOut( long timestamp ) {
                        long begin = durationFilter.getBegin().getTime();
                        long end = durationFilter.getEnd().getTime();

                        boolean isBefore, isAfter;

                        if ( durationFilter.isInclusiveBegin() ) {
                            isBefore = timestamp < begin;
                        } else {
                            isBefore = timestamp <= begin;
                        }

                        if ( durationFilter.isInclusiveEnd() ) {
                            isAfter = timestamp > end;
                        } else {
                            isAfter = timestamp >= end;
                        }

                        return isBefore || isAfter;
                    }
                };
            } else if ( filter instanceof BeginFilter ) {
                final BeginFilter beginFilter = (BeginFilter) filter;

                return new SimpleTimestampFilter() {
                    @Override
                    public boolean filterOut( long timestamp ) {
                        long begin = beginFilter.getBegin().getTime();

                        boolean isBefore;

                        if ( beginFilter.isInclusiveBegin() ) {
                            isBefore = timestamp < begin;
                        } else {
                            isBefore = timestamp <= begin;
                        }

                        return isBefore;
                    }
                };
            } else if ( filter instanceof EndFilter ) {
                final EndFilter endFilter = (EndFilter) filter;
                return new SimpleTimestampFilter() {
                    @Override
                    public boolean filterOut( long timestamp ) {
                        long end = endFilter.getEnd().getTime();

                        boolean isAfter;

                        if ( endFilter.isInclusiveEnd() ) {
                            isAfter = timestamp > end;
                        } else {
                            isAfter = timestamp >= end;
                        }

                        return isAfter;
                    }
                };
            } else if ( filter instanceof TimeInstantFilter ) {
                final TimeInstantFilter timeInstantFilter = (TimeInstantFilter) filter;
                return new SimpleTimestampFilter() {
                    @Override
                    public boolean filterOut( long timestamp ) {
                        long time = timeInstantFilter.getInstant().getTime();

                        return timestamp != time;
                    }
                };
            }
            LOG.warn( "Filter {} is not implemented ", filter );
            return null;
        }

        /**
         * @param timestamp
         * @return true if one filter matches
         */
        public boolean keep( long timestamp ) {
            if ( this.filter.size() == 0 ) {
                return true;
            }
            // filters are chained with OR
            for ( SimpleTimestampFilter filter : this.filter ) {
                if ( !filter.filterOut( timestamp ) ) {
                    return true;
                }
            }
            return false;
        }
    }

    private static interface SimpleTimestampFilter {
        boolean filterOut( long timestamp );
    }
}
