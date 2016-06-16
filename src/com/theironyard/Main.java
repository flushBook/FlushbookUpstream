package com.theironyard;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS toilets (id IDENTITY, facility VARCHAR, latitude DOUBLE, longitude DOUBLE, access INT, capacity INT, cleanliness INT)");
    }

    public static ArrayList<Toilet> selectToilets(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM toilets");
        ResultSet results = stmt.executeQuery();
        ArrayList<Toilet> toilets = new ArrayList<>();
        while (results.next()) {
            Integer id = results.getInt("id");
            String facility = results.getString("facility");
            double lat = results.getDouble("latitude");
            double lon = results.getDouble("longitude");
            int easeOfAccess = results.getInt("access");
            int capacity = results.getInt("capacity");
            int cleanliness = results.getInt("cleanliness");
            Toilet toilet = new Toilet(id, facility, lat, lon, easeOfAccess, capacity, cleanliness);
            toilets.add(toilet);
        }
        return toilets;
    }

    public static void insertToilet(Connection conn, Toilet toilet) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO toilets VALUES(NULL, ?, ?, ?, ?, ?, ?)");
        stmt.setString(1, toilet.facility);
        stmt.setDouble(2, toilet.lat);
        stmt.setDouble(3, toilet.lon);
        stmt.setInt(4, toilet.easeOfAccess);
        stmt.setInt(5, toilet.capacity);
        stmt.setInt(6, toilet.capacity);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.externalStaticFileLocation("public");
        Spark.init();

        Spark.get(
                "/flush",
                (request, response) -> {
                    ArrayList<Toilet> toilets = selectToilets(conn);
                    JsonSerializer s = new JsonSerializer();
                    return s.serialize(toilets);
                }
        );
        Spark.post(
                "/flush",
                (request, response) -> {
                    String body = request.body();
                    JsonParser p = new JsonParser();
                    Toilet toilet = p.parse(body, Toilet.class);
                    insertToilet(conn, toilet);
                    return "";

                }
        );
//        Spark.put(
//                "/flush",
//                (request, response) -> {
//
//                }
//        );
//        Spark.delete(
//                "/flush",
//                (request, response) -> {
//
//                }
//        );






    }
}
