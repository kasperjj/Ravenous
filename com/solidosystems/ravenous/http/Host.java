package com.solidosystems.ravenous.http;

import java.util.*;

public class Host{
    private long id;
    
    private HashSet<String> domains;
    private String hostname;
    
    private String ip;
    private int port;
    
    private String hostroot;
    
    @SuppressWarnings("unchecked")
    public Host(long id,String hostroot,String hostname,HashSet<String> aliases,String ip,int port){
        this.id=id;
        this.hostroot=hostroot;
        if(aliases==null){
            domains=new HashSet<String>();
        }else domains=(HashSet<String>)aliases.clone();
        this.hostname=hostname;
        domains.add(hostname);
        this.ip=ip;
        this.port=port;
    }
    
    public long getId(){
        return id;
    }
    
    public String getHostname(){
        return hostname;
    }
    
    public String getIP(){
        return ip;
    }
    
    public int getPort(){
        return port;
    }
    
    public String getHostRoot(){
        return hostroot;
    }
    
    public Set<String> getDomains(){
        return domains;
    }
    
    /*public Host(LinkedHashMap map) throws HostConfigurationException{
        if(!map.containsKey("host-name"))throw new HostConfigurationException("Missing host-name");
        if(!map.containsKey("bind"))throw new HostConfigurationException("Missing bind");
        if(!map.containsKey("host-root"))throw new HostConfigurationException("Missing host-root");
        
        domains=new HashSet<String>();
        
        String hostname=(String)map.get("host-name");
        if(hostname.length()==0)throw new HostConfigurationException("host-name can not be blank");
        domains.add(hostname);
        
        if(map.containsKey("host-alias")){
            Object obj=map.get("host-alias");
            if(obj.getClass().getName().equals("java.lang.String")){
                hostname=(String)obj;
                if(hostname.length()>0){
                    domains.add(hostname);
                }
            }else if(obj.getClass().getName().equals("java.util.LinkedList")){
                List lst=(List)obj;
                 for(obj : lst){
                    if(obj.getClass().getName().equals("java.lang.String")){
                        hostname=(String)obj;
                        if(hostname.length()>0){
                            domains.add(hostname);
                        }
                    }else throw new HostConfigurationException("Malformed host-alias configuration");
                } 
            }else throw new HostConfigurationException("Malformed host-alias configuration");
        }
        
        System.out.println(hostname);
        Object alias=map.get("bind");
        System.out.println(alias.getClass());
        // System.out.println(data);
        
    } */
}