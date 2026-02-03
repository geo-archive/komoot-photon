package de.komoot.photon.nominatim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * Utility functions to parse data from and create SQL for PostgreSQL/PostGIS.
 */
@NullMarked
public class PostgisDataAdapter implements DBDataAdapter {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Map<String, String> getMap(ResultSet rs, String columnName) throws SQLException {
        Map<String, String> map = (Map<String, String>) rs.getObject(columnName);
        if (map == null) {
            return new HashMap<>();
        }

        return map;
    }

    @Override
    @Nullable
    public Geometry extractGeometry(ResultSet rs, String columnName) throws SQLException {
        var ewkbHex = rs.getString(columnName);
        if  (ewkbHex == null) {
            return null;
        }

        try {
            var ewkb = HexFormat.of().parseHex(ewkbHex);
            return new WKBReader(GEOMETRY_FACTORY).read(ewkb);
        } catch (IllegalArgumentException | ParseException e) {
            LOGGER.error("Cannot parse database geometry: {}", ewkbHex, e);
            return null;
        }
    }

    @Override
    public String deleteReturning(String deleteSQL, String columns) {
        return deleteSQL + " RETURNING " + columns;
    }

    @Override
    public String jsonArrayFromSelect(String valueSQL, String fromSQL) {
        return "(SELECT json_agg(val) FROM (SELECT " + valueSQL + " as val " + fromSQL + ") xxx)";
    }
}
