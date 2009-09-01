package com.solidosystems.ravenous.client;

public class DevelopmentHost{
    private long id;
    private String hostname,ip,path;
    private int port;
    
    public DevelopmentHost(long id,String hostname,String ip,int port,String path){
        this.id=id;
        this.hostname=hostname;
        this.ip=ip;
        this.port=port;
        this.path=path;
    }
    
    public String getHostName(){
        return hostname;
    }
    
    public String getPath(){
        return path;
    }
    
    public int getPort(){
        return port;
    }
    
    public long getId(){
        return id;
    }
    public String getIP(){
        return ip;
    }
}