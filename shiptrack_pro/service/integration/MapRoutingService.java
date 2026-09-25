package com.shiptrack.shiptrack_pro.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MapRoutingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org";

    private static final String OSRM_URL =
            "https://router.project-osrm.org";

    private final WebClient webClient;

    private final ObjectMapper objectMapper;


    public MapRoutingService() {

        this.objectMapper = new ObjectMapper();

        this.webClient = WebClient.builder()
                .defaultHeader(
                        "User-Agent",
                        "ShipTrack/1.0 (college-project)"
                )
                .build();

        log.info(
                "OpenStreetMap Nominatim + OSRM service initialized"
        );
    }


    // ============================================================
    // GEOCODE ADDRESS
    // ============================================================

    public Map<String, Double> geocodeAddress(
            String address
    ) {

        try {

            log.info(
                    "Geocoding address: {}",
                    address
            );

            String encodedAddress =
                    URLEncoder.encode(
                            address,
                            StandardCharsets.UTF_8
                    );

            String url =
                    NOMINATIM_URL
                            + "/search?format=json"
                            + "&limit=1"
                            + "&q="
                            + encodedAddress;

            String response =
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            JsonNode results =
                    objectMapper.readTree(response);

            if (results == null ||
                    !results.isArray() ||
                    results.isEmpty()) {

                log.warn(
                        "No geocoding results found for: {}",
                        address
                );

                return null;
            }

            JsonNode location =
                    results.get(0);

            double latitude =
                    location
                            .get("lat")
                            .asDouble();

            double longitude =
                    location
                            .get("lon")
                            .asDouble();

            Map<String, Double> coordinates =
                    new HashMap<>();

            coordinates.put(
                    "latitude",
                    latitude
            );

            coordinates.put(
                    "longitude",
                    longitude
            );

            log.info(
                    "Geocoding successful: {} -> {}, {}",
                    address,
                    latitude,
                    longitude
            );

            return coordinates;

        } catch (Exception e) {

            log.error(
                    "Error geocoding address: {}",
                    address,
                    e
            );

            return null;
        }
    }


    // ============================================================
    // GET ROUTE
    // ============================================================

    public RouteResult getRoute(
            String origin,
            String destination
    ) {

        try {

            log.info(
                    "Calculating route: {} -> {}",
                    origin,
                    destination
            );


            // ----------------------------------------------------
            // Geocode origin
            // ----------------------------------------------------

            Map<String, Double> originCoords =
                    geocodeAddress(origin);


            if (originCoords == null) {

                throw new RuntimeException(
                        "Unable to geocode origin: "
                                + origin
                );
            }


            // ----------------------------------------------------
            // Geocode destination
            // ----------------------------------------------------

            Map<String, Double> destinationCoords =
                    geocodeAddress(destination);


            if (destinationCoords == null) {

                throw new RuntimeException(
                        "Unable to geocode destination: "
                                + destination
                );
            }


            double originLatitude =
                    originCoords.get("latitude");

            double originLongitude =
                    originCoords.get("longitude");

            double destinationLatitude =
                    destinationCoords.get("latitude");

            double destinationLongitude =
                    destinationCoords.get("longitude");


            // ----------------------------------------------------
            // IMPORTANT:
            //
            // OSRM expects:
            //
            // longitude,latitude
            //
            // ----------------------------------------------------

            String coordinates =
                    originLongitude
                            + ","
                            + originLatitude
                            + ";"
                            + destinationLongitude
                            + ","
                            + destinationLatitude;


            String url =
                    OSRM_URL
                            + "/route/v1/driving/"
                            + coordinates
                            + "?overview=full"
                            + "&geometries=geojson";


            log.info(
                    "Calling OSRM: {}",
                    url
            );


            String response =
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();


            JsonNode root =
                    objectMapper.readTree(response);


            if (root == null) {

                throw new RuntimeException(
                        "Empty response from OSRM"
                );
            }


            String code =
                    root.path("code")
                            .asText();


            if (!"Ok".equals(code)) {

                throw new RuntimeException(
                        "OSRM routing failed: "
                                + code
                );
            }


            JsonNode routes =
                    root.path("routes");


            if (!routes.isArray() ||
                    routes.isEmpty()) {

                throw new RuntimeException(
                        "No route found"
                );
            }


            JsonNode route =
                    routes.get(0);


            double distanceMeters =
                    route.path("distance")
                            .asDouble();


            double durationSeconds =
                    route.path("duration")
                            .asDouble();


            JsonNode geometry =
                    route.path("geometry");


            log.info(
                    "Route calculated successfully"
            );

            log.info(
                    "Distance: {} km",
                    distanceMeters / 1000.0
            );

            log.info(
                    "Estimated time: {} minutes",
                    Math.ceil(
                            durationSeconds / 60.0
                    )
            );


            return new RouteResult(
                    distanceMeters,
                    durationSeconds,
                    geometry
            );


        } catch (Exception e) {

            log.error(
                    "Error calculating route: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Unable to calculate route: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // ============================================================
    // GET DISTANCE
    // ============================================================

    public Double getDistance(
            String origin,
            String destination
    ) {

        try {

            RouteResult route =
                    getRoute(
                            origin,
                            destination
                    );

            return route.distanceMeters()
                    / 1000.0;

        } catch (Exception e) {

            log.error(
                    "Error calculating distance",
                    e
            );

            return null;
        }
    }


    // ============================================================
    // GET ESTIMATED TIME
    // ============================================================

    public Integer getEstimatedTime(
            String origin,
            String destination
    ) {

        try {

            RouteResult route =
                    getRoute(
                            origin,
                            destination
                    );

            return (int) Math.ceil(
                    route.durationSeconds()
                            / 60.0
            );

        } catch (Exception e) {

            log.error(
                    "Error calculating estimated time",
                    e
            );

            return null;
        }
    }


    // ============================================================
    // GET ROUTE INFO
    // ============================================================

    public Map<String, Object> getRouteInfo(
            String origin,
            String destination
    ) {

        try {

            RouteResult route =
                    getRoute(
                            origin,
                            destination
                    );

            Map<String, Double> originCoords =
                    geocodeAddress(origin);

            Map<String, Double> destinationCoords =
                    geocodeAddress(destination);


            Map<String, Object> routeInfo =
                    new HashMap<>();


            routeInfo.put(
                    "distance_km",
                    route.distanceMeters()
                            / 1000.0
            );

            routeInfo.put(
                    "estimated_time_minutes",
                    (int) Math.ceil(
                            route.durationSeconds()
                                    / 60.0
                    )
            );

            routeInfo.put(
                    "origin_coordinates",
                    originCoords
            );

            routeInfo.put(
                    "destination_coordinates",
                    destinationCoords
            );

            routeInfo.put(
                    "geometry",
                    route.geometry()
            );


            return routeInfo;

        } catch (Exception e) {

            log.error(
                    "Error getting route information",
                    e
            );

            return null;
        }
    }


    // ============================================================
    // VALIDATE ADDRESS
    // ============================================================

    public boolean isValidAddress(
            String address
    ) {

        return geocodeAddress(address) != null;
    }


    // ============================================================
    // REVERSE GEOCODING
    // ============================================================

    public String getAddressFromCoordinates(
            double latitude,
            double longitude
    ) {

        try {

            String url =
                    NOMINATIM_URL
                            + "/reverse?format=json"
                            + "&lat="
                            + latitude
                            + "&lon="
                            + longitude;


            String response =
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();


            JsonNode result =
                    objectMapper.readTree(response);


            if (result == null ||
                    result.path("display_name")
                            .isMissingNode()) {

                return null;
            }


            return result
                    .path("display_name")
                    .asText();


        } catch (Exception e) {

            log.error(
                    "Error reverse geocoding",
                    e
            );

            return null;
        }
    }


    // ============================================================
    // ROUTE RESULT
    // ============================================================

    public record RouteResult(
            double distanceMeters,
            double durationSeconds,
            JsonNode geometry
    ) {
    }
}