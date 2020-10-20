/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rmunene
 */
public class DBFunctions {

    public Map<String, String> customerRegistration(int nationalID, String firstName, String lastName,
            String phonenumber, String uuid) {
        Map<String, String> responseMap = new HashMap<>();
        // check if customer exists
        boolean custExist = checkIfExist(phonenumber);
        if (custExist) {
            responseMap.put("status", "0");
            responseMap.put("message", "Customer Already Exist");
        } else {
            // insert to customer and accounts
            int pin = 12345;
            insertToCustomer(nationalID, firstName, lastName, phonenumber, uuid, pin);
            insertToAccount(nationalID, phonenumber);
            responseMap.put("status", "1");
            responseMap.put("message", "Registration Successful");
        }
        return responseMap;
    }

    private boolean checkIfExist(String phonenumber) {
        boolean exist = false;
        String sql = "SELECT ID FROM tbcustomers  WHERE PhoneNumber=?";
        try (Connection connection = DBconnection.Connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phonenumber);
            ps.executeQuery();
            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {
                    exist = true;
                }
            }
        } catch (Exception ex) {

            // log
        }
        return exist;
    }

    private void insertToAccount(int nationalID, String phonenumber) {
        String sql = "Insert into tbaccounts(`NationalID`, `AvailableBal`, `ActualBal`,`PhoneNumber`)values(?,?,?,?)";
        try (Connection connection = DBconnection.Connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, nationalID);
            ps.setInt(2, 0);
            ps.setInt(3, 0);
            ps.setString(4, phonenumber);
            ps.execute();
        } catch (Exception ex) {
            // log
        }
    }

    private void insertToCustomer(int nationalID, String firstName, String lastName,
            String phonenumber, String uuid, int pin) {
        String sql = "Insert into tbcustomers(`NationalID`, `FirstName`, `LastName`,`PhoneNumber`,`Uuid`,`PIN`)values (?,?,?,?,?,?)";
        try (Connection connection = DBconnection.Connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, nationalID);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, phonenumber);
            ps.setString(5, uuid);
            ps.setInt(6, pin);
            ps.execute();
        } catch (Exception ex) {
            // log
        }
    }

    public Map<String, String> Login(String phoneNumber, String pin, String uuid) {
        Map<String, String> respMap = new HashMap<>();
        String sql = "SELECT Uuid,PIN FROM tbcustomers  WHERE PhoneNumber=?";
        try (Connection connection = DBconnection.Connect();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phoneNumber);
            ps.executeQuery();
            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {
                    String dbUUID = rs.getString("Uuid");
                    String dbPIN = rs.getString("PIN");
                    if (!dbUUID.equalsIgnoreCase(uuid)) {
                        respMap.put("status", "0");
                        respMap.put("message", "Kindly the device does not match that was used previously");
                    } else if (!dbPIN.equals(pin)) {
                        respMap.put("status", "0");
                        respMap.put("message", "Wrong Credentials");
                    } else {
                        respMap.put("status", "1");
                        respMap.put("message", "login succesful");
                    }
                } else {
                    respMap.put("status", "0");
                    respMap.put("message", "Customer Does Not  Exist.Kindly Register");
                }
            }
        } catch (Exception ex) {
        }
        return respMap;
    }

}
