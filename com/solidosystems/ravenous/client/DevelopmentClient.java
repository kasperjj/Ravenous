package com.solidosystems.ravenous.client;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.solidosystems.ravenous.http.*;

public class DevelopmentClient implements Runnable{
    private static DevelopmentClient app;
    private HostListFrame gui;
    private String configPath;
    private Connection dbcon;
    private ConnectionHandler server;
    
    public static DevelopmentClient get(){
        return app;
    }
    
    public int getNextPort() throws Exception{
        int port=8080;
        Statement st=dbcon.createStatement();
        ResultSet rs=st.executeQuery("SELECT MAX(port) FROM tbl_host");
        if(rs.next()){
            if(rs.getInt(1)>=port)port=rs.getInt(1)+1;
        }
        return port;
    }
    
    public boolean isUniquePort(String port) throws Exception{
        Statement st=dbcon.createStatement();
        ResultSet rs=st.executeQuery("SELECT id FROM tbl_host WHERE port="+port);
        if(rs.next())return false;
        return true;
    }
    
    public boolean isUniquePort(String port,long id) throws Exception{
        Statement st=dbcon.createStatement();
        ResultSet rs=st.executeQuery("SELECT id FROM tbl_host WHERE port="+port+" AND id != "+id);
        if(rs.next())return false;
        return true;
    }
    
    public boolean isUniqueHost(String host) throws Exception{
        Statement st=dbcon.createStatement();
        ResultSet rs=st.executeQuery("SELECT id FROM tbl_host WHERE hostname='"+host+"'");
        if(rs.next())return false;
        return true;
    }
    
    public boolean isUniqueHost(String host,long id) throws Exception{
        Statement st=dbcon.createStatement();
        ResultSet rs=st.executeQuery("SELECT id FROM tbl_host WHERE hostname='"+host+"' AND id != "+id);
        if(rs.next())return false;
        return true;
    }
    
    public boolean deleteHost(long id) throws Exception{
        Statement st=dbcon.createStatement();
        st.executeUpdate("DELETE FROM tbl_host WHERE id="+id);
        server.removeHost(id);
        return true;
    }
    
    public boolean createNewHost(String hostname,String path,String listen,String port) throws Exception{
        Statement st=dbcon.createStatement();
        st.executeUpdate("INSERT INTO tbl_host (hostname,ip,port,path,tcreated) VALUES('"+hostname+"','"+listen+"',"+port+",'"+path+"',CURRENT_TIMESTAMP())");
        ResultSet rs=st.executeQuery("SELECT id FROM tbl_host WHERE hostname='"+hostname+"'");
        if(rs.next()){
            Host hst=new Host(rs.getLong("id"),path,hostname,null,listen,Integer.parseInt(port));
            server.addHost(hst);
        }
        return true;
    }
    
    public boolean saveHost(long id,String hostname,String path,String listen,String port) throws Exception{
        Statement st=dbcon.createStatement();
        st.executeUpdate("UPDATE tbl_host SET hostname='"+hostname+"',ip='"+listen+"',port="+port+",path='"+path+"' WHERE id="+id);
        Host hst=new Host((int)id,path,hostname,null,listen,Integer.parseInt(port));
        server.updateHost(hst);
        return true;
    }
    
    public List<DevelopmentHost> getHostList() throws Exception{
        Statement st=dbcon.createStatement();
        ResultSet rs=st.executeQuery("SELECT id,hostname,ip,port,path,tcreated FROM tbl_host ORDER by hostname");
        ArrayList<DevelopmentHost> lst=new ArrayList<DevelopmentHost>();
        while(rs.next()){
            lst.add(new DevelopmentHost(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(5)));
        }
        rs.close();
        st.close();
        return lst;
    }
    
    public DevelopmentClient(){
        app=this;
        try{    
            if(System.getProperty("os.name").indexOf("Windows")>-1){
                // we are on a windows type os
                configPath=System.getProperty("user.home")+"\\AppData\\Local\\";
                File ftest=new File(configPath);
                if(!ftest.exists()){
                    System.out.println("Sorry, this version of windows is not currently supported!");
                    System.exit(-1);
                }
                configPath+="Ravenous2\\";
                ftest=new File(configPath);
                if(!ftest.exists())ftest.mkdir();
            }else{
                // we are probably on a unix style os
                configPath=System.getProperty("user.home")+"/.ravenous/";
                File ftest=new File(configPath);
                if(!ftest.exists())ftest.mkdir();
            }
            Class.forName("org.h2.Driver");
            dbcon = DriverManager.getConnection("jdbc:h2:"+configPath+"configdb", "sa", "");
            Statement st=dbcon.createStatement();
            try{
                ResultSet rs=st.executeQuery("SELECT * FROM tbl_host");
                rs.close();
            }catch(Exception ee){
                st.executeUpdate("CREATE TABLE tbl_host (id IDENTITY,hostname VARCHAR, ip VARCHAR, port INT, path VARCHAR,tcreated TIMESTAMP)");
            }
            st.close();            
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        // new Thread(this).start();
        gui=new HostListFrame();
        
        server=new ConnectionHandler(true);
        try{
            Statement st=dbcon.createStatement();
            ResultSet rs=st.executeQuery("SELECT id,hostname,ip,port,path FROM tbl_host");
            while(rs.next()){
                Host hst=new Host(rs.getLong("id"),rs.getString("path"),rs.getString("hostname"),null,rs.getString("ip"),rs.getInt("port"));
                server.addHost(hst);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }
    
    public void run(){
        try{
            dbcon.close();
            server.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String args[]){
        new DevelopmentClient();
    }
}