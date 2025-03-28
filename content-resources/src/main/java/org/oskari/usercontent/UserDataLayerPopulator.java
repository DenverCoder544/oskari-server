package org.oskari.usercontent;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.OskariRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.geotools.api.geometry.Bounds;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.util.Set;

/**
 * Created by SMAKINEN on 1.9.2015.
 */

public class UserDataLayerPopulator {

    public static final String NAMESPACE = "oskari";
    private static final Logger LOG = LogFactory.getLogger(UserDataLayerPopulator.class);

    public static final String KEY_URL = "url";
    public static final String KEY_USER = "user";
    public static final String KEY_PASSWD = "password";

    public static final String ANALYSIS_MODULE_NAME = "analysis";

    public static String getGeoserverProp(final String module, final String part) {
        final String preferProp = "geoserver." + module + "." + part;
        final String fallbackProp = "geoserver." + part;

        final String prop = PropertyUtil.get(preferProp,
                PropertyUtil.getOptional(fallbackProp));

        if (prop == null) {
            throw new OskariRuntimeException("Geoserver properties not configured! Tried: " + preferProp + " and " + fallbackProp);
        }
        return prop;
    }


    public static void setupGeoserverConf(OskariLayer layer, String module) {
        layer.setUrl(getGeoserverProp(module, KEY_URL) + "/" + NAMESPACE + "/ows");
        layer.setUsername(getGeoserverProp(module, KEY_USER));
        layer.setPassword(getGeoserverProp(module, KEY_PASSWD));
    }

    public static int setupMyplacesLayer(final String srs) {
        final String name = NAMESPACE + ":my_places";
        OskariLayer baseLayer = LayerHelper.getLayerWithName(name);
        boolean doInsert = baseLayer == null;
        if (doInsert) {
            baseLayer = new OskariLayer();
            baseLayer.setType(OskariLayer.TYPE_WFS);
            baseLayer.setVersion("1.1.0");
            baseLayer.setName(name);
            baseLayer.setInternal(true);
            baseLayer.setLocale(JSONHelper.createJSONObject("{ fi:{name:\"Omat paikat\"},sv:{name:\"My places\"},en:{name:\"My places\"}}"));
            baseLayer.setOpacity(50);
            baseLayer.setAttributes(addMyplacesAttributes(createUserContentAttributes()));
        }
        // setup data producer/layergroup since original doesn't have one
        baseLayer.addDataprovider(LayerHelper.getDataprovider());
        baseLayer.setSrs_name(srs);
        if (!doInsert) {
            LayerHelper.update(baseLayer);
            return baseLayer.getId();
        }
        // insert
        LayerHelper.insert(baseLayer);

        return baseLayer.getId();
    }

    public static JSONObject createUserContentAttributes() {
        JSONObject attributes = new JSONObject();
        JSONHelper.putValue(attributes, "maxFeatures", 2000);
        JSONHelper.putValue(attributes, "namespaceURL", "http://www.oskari.org");
        return attributes;
    }

    public static JSONObject addMyplacesAttributes(JSONObject attributes) {
        JSONObject data = new JSONObject();
        JSONHelper.putValue(attributes, "data", data);

        JSONObject filter = new JSONObject();
        JSONHelper.putValue(data, "filter", filter);
        Set<String> fields = ConversionHelper.asSet("name", "place_desc", "image_url", "link");
        JSONHelper.putValue(filter, "default", new JSONArray(fields));
        JSONHelper.putValue(filter, "fi", new JSONArray(fields));

        JSONObject locale = new JSONObject();
        JSONHelper.putValue(data, "locale", locale);
        JSONObject en = new JSONObject();
        JSONHelper.putValue(locale, "en", en);
        JSONHelper.putValue(en, "name", "Name");
        JSONHelper.putValue(en, "place_desc", "Description");
        JSONHelper.putValue(en, "link", "URL");
        JSONHelper.putValue(en, "image_url", "Image URL");
        JSONHelper.putValue(en, "attention_text", "Text on map");

        JSONObject fi = new JSONObject();
        JSONHelper.putValue(locale, "fi", fi);
        JSONHelper.putValue(fi, "name", "Nimi");
        JSONHelper.putValue(fi, "place_desc", "Kuvaus");
        JSONHelper.putValue(fi, "link", "Linkki");
        JSONHelper.putValue(fi, "image_url", "Kuvalinkki");
        JSONHelper.putValue(fi, "attention_text", "Teksti kartalla");

        JSONObject sv = new JSONObject();
        JSONHelper.putValue(locale, "sv", sv);
        JSONHelper.putValue(sv, "name", "Namn");
        JSONHelper.putValue(sv, "place_desc", "Beskrivelse");
        JSONHelper.putValue(sv, "link", "Webbaddress");
        JSONHelper.putValue(sv, "image_url", "Bild-URL");
        JSONHelper.putValue(sv, "attention_text", "Placera text på kartan");

        /*
        Format is:
        "name": {
            "type": "h3",
            "noLabel": true
        },
        "place_desc": {
            "type": "p",
            "noLabel": true,
            "skipEmpty": true
        },
        "attention_text": {
            "type": "hidden"
        },
        "image_url": {
            "type": "image",
            "noLabel": true,
            "params": {
                "link": true
            },
            "skipEmpty": true
        },
        "link": {
            "type": "link",
            "skipEmpty": true
        }
         */
        JSONObject format = new JSONObject();
        JSONHelper.putValue(data, "format", format);

        JSONObject name = new JSONObject();
        JSONHelper.putValue(format, "name", name);
        JSONHelper.putValue(name, "type", "h3");
        JSONHelper.putValue(name, "noLabel", true);

        JSONObject place_desc = new JSONObject();
        JSONHelper.putValue(format, "place_desc", place_desc);
        JSONHelper.putValue(place_desc, "type", "p");
        JSONHelper.putValue(place_desc, "noLabel", true);
        JSONHelper.putValue(place_desc, "skipEmpty", true);

        JSONObject attention_text = new JSONObject();
        JSONHelper.putValue(format, "attention_text", attention_text);
        JSONHelper.putValue(attention_text, "type", "hidden");

        JSONObject image_url = new JSONObject();
        JSONHelper.putValue(format, "image_url", image_url);
        JSONHelper.putValue(image_url, "type", "image");
        JSONHelper.putValue(image_url, "noLabel", true);
        JSONHelper.putValue(image_url, "skipEmpty", true);
        JSONObject image_params = new JSONObject();
        JSONHelper.putValue(image_params, "link", true);
        JSONHelper.putValue(image_url, "params", image_params);

        JSONObject link = new JSONObject();
        JSONHelper.putValue(format, "link", link);
        JSONHelper.putValue(link, "type", "link");
        JSONHelper.putValue(link, "skipEmpty", true);

        return attributes;
    }

    public static int setupAnalysisLayer(final String srs) {
        final String name = NAMESPACE + ":analysis_data";
        OskariLayer baseLayer = LayerHelper.getLayerWithName(name);
        boolean doInsert = baseLayer == null;
        if (doInsert) {
            baseLayer = new OskariLayer();
            baseLayer.setType(OskariLayer.TYPE_WFS);
            baseLayer.setVersion("1.1.0");
            baseLayer.setName(name);
            baseLayer.setInternal(true);
            baseLayer.setLocale(JSONHelper.createJSONObject("{ fi:{name:\"Analyysitaso\"},sv:{name:\"Analys\"},en:{name:\"Analyse\"}}"));
            baseLayer.setOpacity(50);
            baseLayer.setAttributes(createUserContentAttributes());
        }
        // setup data producer/layergroup since original doesn't have one
        baseLayer.addDataprovider(LayerHelper.getDataprovider());
        setupGeoserverConf(baseLayer, ANALYSIS_MODULE_NAME);
        baseLayer.setSrs_name(srs);
        if (!doInsert) {
            LayerHelper.update(baseLayer);
            return baseLayer.getId();
        }
        // insert
        LayerHelper.insert(baseLayer);

        return baseLayer.getId();
    }

    public static int setupUserLayer(final String srs) {
        final String name = NAMESPACE + ":vuser_layer_data";
        OskariLayer baseLayer = LayerHelper.getLayerWithName(name);
        boolean doInsert = baseLayer == null;
        if (doInsert) {
            baseLayer = new OskariLayer();
            baseLayer.setType(OskariLayer.TYPE_WFS);
            baseLayer.setVersion("1.1.0");
            baseLayer.setName(name);
            baseLayer.setInternal(true);
            baseLayer.setLocale(JSONHelper.createJSONObject("{ fi:{name:\"Omat aineistot\"},sv:{name:\"User layers\"},en:{name:\"User layers\"}}"));
            baseLayer.setOpacity(80);
            baseLayer.setAttributes(createUserContentAttributes());
        }
        // setup data producer/layergroup since original doesn't have one
        baseLayer.addDataprovider(LayerHelper.getDataprovider());
        baseLayer.setSrs_name(srs);
        if (!doInsert) {
            LayerHelper.update(baseLayer);
            return baseLayer.getId();
        }
        // insert
        LayerHelper.insert(baseLayer);

        return baseLayer.getId();
    }
}
