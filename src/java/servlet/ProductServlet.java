/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import credentials.Credentials;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.json.simple.JSONValue;

/**
 *
 * @author c0646567
 */
//@WebServlet("/product")
//public class ProductServlet  {
/**
 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 * methods.
 *
 * @param request servlet request
 * @param response servlet response
 * @throws ServletException if a servlet-specific error occurs
 * @throws IOException if an I/O error occurs
 */
@Path("/product")
public class ProductServlet {

    @GET
    @Produces("application/json")
    public String doGet() {
        String str = getResults("SELECT * FROM product");
        return str;
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public String doGet(@PathParam("id") String id) {
        String str = getResults("SELECT * FROM product where productID = ?", id);
        return str;
    }


    /**
     * json format taken from
     * https://code.google.com/p/json-simple/wiki/EncodingExamples
     *
     * @param query
     * @param params
     * @return
     */
    private String getResults(String query, String... params) {
        StringBuilder sb = new StringBuilder();
        String myString = "";
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }

            ResultSet rs = pstmt.executeQuery();
            // sb.append("[");
            List list = new LinkedList();
            while (rs.next()) {
                //sb.append(String.format("{ \"productId\" : %s , \"name\" : \"%s\", \"description\" : \"%s\", \"quantity\" : %s }" + ",\n", rs.getInt("productID"), rs.getString("name"), rs.getString("description"), rs.getInt("quantity")));
                //sb.append(", ");

                Map map = new LinkedHashMap();
                map.put("productID", rs.getInt("productID"));
                map.put("name", rs.getString("name"));
                map.put("description", rs.getString("description"));
                map.put("quantity", rs.getInt("quantity"));

                list.add(map);

            }
            myString = JSONValue.toJSONString(list);
            //sb.delete(sb.length() - 2, sb.length() - 1);
            //sb.append("]");
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return myString.replace("},", "},\n");
    }

    @POST
    @Consumes("application/json")
    public void doPost(String str) {
        JsonParser parser = Json.createParser(new StringReader(str));
        Map<String, String> map = new HashMap<>();
        String name = "", value;

        while (parser.hasNext()) {
            JsonParser.Event evt = parser.next();
            switch (evt) {
                case KEY_NAME:
                    name = parser.getString();
                    break;
                case VALUE_STRING:

                    value = parser.getString();
                    map.put(name, value);
                    break;
                case VALUE_NUMBER:
                    value = Integer.toString(parser.getInt());
                    map.put(name, value);
                    break;
            }
        }
        System.out.println(map);
        String str1 = map.get("name");
        String description = map.get("description");
        String quantity = map.get("quantity");
        doUpdate("insert into product ( name, description, quantity) values ( ?, ?, ?)", str1, description, quantity);
    }

    private int doUpdate(String query, String... params) {
        int numChanges = 0;
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            numChanges = pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numChanges;
    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public void doPut(@PathParam("id") String id, String str) {
        JsonParser parser = Json.createParser(new StringReader(str));
        Map<String, String> map = new HashMap<>();
        String name = "", value;
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            switch (event) {
                case KEY_NAME:
                    name = parser.getString();
                    break;
                case VALUE_STRING:
                    value = parser.getString();
                    map.put(name, value);
                    break;
                case VALUE_NUMBER:
                    value = Integer.toString(parser.getInt());
                    map.put(name, value);
                    break;
            }
        }
        System.out.println(map);

        String str1 = map.get("name");
        String description = map.get("description");
        String quantity = map.get("quantity");
        doUpdate("update product set productId = ?, name = ?, description = ?, quantity = ? where productID = ?", id, str1, description, quantity, id);

    }


    @DELETE
    @Path("{id}")
    public void doDelete(@PathParam("id") String id, String str) {
        doUpdate("delete from product where productId = ?", id);
    }

}
