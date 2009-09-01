package com.solidosystems.ravenous.db;

import java.sql.Connection;

public class DatabaseConnection{
    public long tlastuse;
    public Connection connection;
    
    public DatabaseConnection(Connection connection){
        this.connection=connection;
        tlastuse=System.currentTimeMillis();
    }
    
    public void close(){
        try{
            connection.close();
        }catch(Exception e){}
    }
}