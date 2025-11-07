package com.aidredaline.backend.domain.runningsession.geo;

import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Component;

@Component
public class GeoFactory {

    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

    // ⚠️ JTS는 (x=lng, y=lat) 순서
    public Point point(double lat, double lng) {
        return gf.createPoint(new Coordinate(lng, lat));
    }
}
