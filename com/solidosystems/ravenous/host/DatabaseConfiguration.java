package com.solidosystems.ravenous.host;

import java.util.*;

public class DatabaseConfiguration{
    private String dbDriver;
    private String dbDatabase;
    private String dbUsername;
    private String dbPassword;
    private List<EntityConfiguration> entities;
    
    public DatabaseConfiguration(String driver,String database,String username,String password){
        dbDriver=driver;
        dbDatabase=database;
        dbUsername=username;
        dbPassword=password;
        entities=new LinkedList<EntityConfiguration>();
    }
    
    public void addEntity(EntityConfiguration ent){
        entities.add(ent);
    }
    
    public List<EntityConfiguration> getEntities(){
        return entities;
    }
    
    public String getDriver(){
        return dbDriver;
    }
    
    public String getDatabase(){
        return dbDatabase;
    }
    
    public String getUsername(){
        return dbUsername;
    }
    
    public String getPassword(){
        return dbPassword;
    }
}