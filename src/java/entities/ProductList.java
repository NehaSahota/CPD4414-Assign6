/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entities;

import credentials.Credentials;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import servlet.ProductServlet;

/**
 *
 * @author c0646567
 */
@ApplicationScoped
public class ProductList {
    private List<Product> productList;

    public ProductList() {
        
        
         try (Connection conn = getConnection()) {
             String query = "Select * from product";
            PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {

                    Product prod = new Product (
                            rs.getInt("productID"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getInt("quantity"));
                    productList.add(prod);
             }
       }
         catch(SQLException ex){
               Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
    
    public JsonArray toJson(){
        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Product p : productList)
                json.add(p.toJSON());
        return json.build();
        
    }
    
         
    
        private Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://localhost/neha";
            String user = "root";
            String pass = "";

            conn = (Connection) DriverManager.getConnection(jdbc, user, pass);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
        
    }
        
        private String getResults(String query, String... params) {
        JsonArrayBuilder productArray = Json.createArrayBuilder();
        String myString = new String();
        try (Connection conn = Credentials.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                JsonObjectBuilder jsonob = Json.createObjectBuilder()
                        .add("productID", rs.getInt("productID"))
                        .add("name", rs.getString("name"))
                        .add("description", rs.getString("description"))
                        .add("quantity", rs.getInt("quantity"));

                myString = jsonob.build().toString();
                productArray.add(jsonob);
            }

        } catch (SQLException ex) {
            Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (params.length == 0) {
            myString = productArray.build().toString();
        }
        return myString;
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
            Logger.getLogger(ProductList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numChanges;
    }
    
    
}
