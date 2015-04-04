/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package entities;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;


/**
 *
 * @author c0646567
 */
@ApplicationScoped
public class ProductList {
    private List<Product> productList;

    public ProductList() {
        
        productList = new ArrayList<>();
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
    
    public Product get(int productID){
        Product result = null;
        for(Product p : productList)
            if(p.getProductID() ==  productID)
                result = p;
                return result;
    }
    
    
    public void set(int productID, Product product) throws Exception{
        
        int result = doUpdate("UPDATE product  SET  name = ? , description = ?, quantity = ? WHERE productID = ?  ",
                   
                    product.getName(),
                    product.getDescription(),
                    String.valueOf(product.getQuantity()));
                    String.valueOf(product.getProductID());
        
                    if(result > 0){
                    Product initial = get(productID);
                    initial.setName(product.getName());
                    initial.setDescription(product.getDescription());
                    initial.setQuantity(product.getQuantity());
               }
                     else throw new Exception("Update Unsuccessful");

    }
    public void add(Product p ) throws Exception{
        int result = doUpdate("INSERT INTO product (productID , name , description, quantity) VALUES  (?,?,?,?) ",
                    String.valueOf(p.getProductID()),
                    p.getName(),
                    p.getDescription(),
                    String.valueOf(p.getQuantity()));
        if (result > 0){
            productList.add(p);
        }
        else throw new Exception("Insertion Unsuccessful");
    
    
    }
    
    public void remove (Product p) throws Exception{
        remove(p.getProductID());
    }
    
    
    public void remove(int productID) throws Exception{
        int result = doUpdate("DELETE FROM product WHERE productID = ?",
                    String.valueOf(productID));
        
        if (result > 0){
            Product initial = get(productID);
            productList.remove(initial);
        
        }
        else
            throw new Exception("Deletion Unsuccessful");
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
        try (Connection conn = getConnection()) {
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
        try (Connection conn = getConnection()) {
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
