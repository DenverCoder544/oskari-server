package org.oskari.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;

public class GeoJSONTest {

    @Test
    public void testPoint() throws JSONException, IOException {
        JSONObject json = ResourceHelper.readJSONResource("point.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        assertEquals(2, f.getProperties().size());

        Point geom = (Point) f.getDefaultGeometry();
        Coordinate c = geom.getCoordinate();
        assertEquals(125.6, c.x, 1e6);
        assertEquals(10.1, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }

    @Test
    public void testLineString() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("lineString.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        assertEquals(2, f.getProperties().size());

        LineString geom = (LineString) f.getDefaultGeometry();
        CoordinateSequence cs = geom.getCoordinateSequence();
        assertEquals(2, cs.size());
        Coordinate c = cs.getCoordinate(0);
        assertEquals(125.6, c.x, 1e6);
        assertEquals(10.1, c.y, 1e6);
        c = cs.getCoordinate(1);
        assertEquals(10.1, c.x, 1e6);
        assertEquals(125.6, c.y, 1e6);

        String name = (String) f.getAttribute("name");
        assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }

    @Test
    public void testPolygon() throws IOException, JSONException {
        JSONObject json = ResourceHelper.readJSONResource("polygon.json", this);
        SimpleFeature f = GeoJSONReader.toFeature(json);
        assertEquals(2, f.getProperties().size());

        Polygon geom = (Polygon) f.getDefaultGeometry();
        CoordinateSequence cs = geom.getExteriorRing().getCoordinateSequence();
        assertEquals(5, cs.size());
        Coordinate c = cs.getCoordinate(0);
        assertEquals(125.6, c.x, 1e6);
        assertEquals(10.1, c.y, 1e6);
        c = cs.getCoordinate(4);
        assertEquals(10.1, c.x, 1e6);
        assertEquals(125.6, c.y, 1e6);
        assertEquals(1, geom.getNumInteriorRing());

        String name = (String) f.getAttribute("name");
        assertEquals("Dinagat Islands", name);

        GeoJSONWriter w = new GeoJSONWriter();
        assertTrue(JSONHelper.isEqual(json, w.writeFeature(f)));
    }

}
