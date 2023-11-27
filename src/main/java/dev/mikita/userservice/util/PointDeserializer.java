package dev.mikita.userservice.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

/**
 * The type Point deserializer.
 */
public class PointDeserializer extends JsonDeserializer<Point> {
    private final GeometryFactory geometryFactory;

    /**
     * Instantiates a new Point deserializer.
     */
    public PointDeserializer() {
        this.geometryFactory = new GeometryFactory();
    }

    @Override
    public Point deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        double latitude = 0.0;
        double longitude = 0.0;

        if (jsonParser.isExpectedStartObjectToken()) {
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                if ("latitude".equals(fieldName)) {
                    latitude = jsonParser.getDoubleValue();
                } else if ("longitude".equals(fieldName)) {
                    longitude = jsonParser.getDoubleValue();
                }
            }
        }

        Coordinate coordinate = new Coordinate(longitude, latitude);
        return geometryFactory.createPoint(coordinate);
    }
}