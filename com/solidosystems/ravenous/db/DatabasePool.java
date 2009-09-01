package com.solidosystems.ravenous.db;

import com.solidosystems.ravenous.host.DatabaseConfiguration;
import java.util.*;
import java.sql.*;

public class DatabasePool{
    public int MAX_CONNECTIONS=8;
    public long TIMEOUT=1000;
    public long RETIRE=5*60*1000l;
    private int connections;
    private List<DatabaseConnection> pool;
    private DatabaseConfiguration config;
    
    public DatabasePool(DatabaseConfiguration config){
        connections=0;
        pool=new LinkedList<DatabaseConnection>();
        this.config=config;
    }

    public synchronized DatabaseConnection createConnection(){
        try{
            Class.forName(config.getDriver());
            Connection con=DriverManager.getConnection(config.getDatabase(),config.getUsername(),config.getPassword());
            DatabaseConnection dbcon=new DatabaseConnection(con);
            connections++;
            return dbcon;
        }catch(Exception e){}
        return null;
    }
    
    public synchronized DatabaseConnection getConnection(){
        long twait=System.currentTimeMillis();
        while(true){
            if(pool.size()>0){
                DatabaseConnection dbcon=pool.remove(0);
                dbcon.tlastuse=System.currentTimeMillis();
                return dbcon;
            }
            if(connections<MAX_CONNECTIONS){
                return createConnection();
            }
            if((System.currentTimeMillis()-twait)>TIMEOUT){
                return createConnection();
            }
            try{
                wait(100);
            }catch(Exception e){}
        }
    }
    
    public synchronized void returnConnection(DatabaseConnection dbcon){
        if(System.currentTimeMillis()-dbcon.tlastuse>RETIRE){
            dbcon.close();
            connections--;
            return;
        }
        if(connections<MAX_CONNECTIONS){
            pool.add(dbcon);
            notify();
            return;
        }
        connections--;
        dbcon.close();
    }
}