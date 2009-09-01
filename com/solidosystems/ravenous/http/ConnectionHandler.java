package com.solidosystems.ravenous.http;

import com.solidosystems.ravenous.host.HostHandler;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.http.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.io.PrintStream;
import java.util.*;

public class ConnectionHandler implements Container{
    private HashMap<Long,Host> hostMap;
    private HashMap<String,Host> domainMap;
    private HashMap<String,Connection> connectionMap;
    private HashMap<Long,HostHandler> hostHandlerMap;
    
    private boolean PORT_MODE=false;
        
    public ConnectionHandler(boolean port_mode){
        PORT_MODE=port_mode;
        hostHandlerMap=new HashMap<Long,HostHandler>();
        hostMap=new HashMap<Long,Host>();
        domainMap=new HashMap<String,Host>();
        connectionMap=new HashMap<String,Connection>();
    }
    
    public void addHost(Host hst){
        hostMap.put(hst.getId(),hst);
        reloadHosts();
    }
    
    public void updateHost(Host hst){
        hostMap.put(hst.getId(),hst);
        reloadHosts();
    }
    
    public void removeHost(long id){
        hostMap.remove(id);
        reloadHosts();
    }
    
    private synchronized void reloadHosts(){
        HashMap<String,Host> tmp_domainMap=new HashMap<String,Host>();
        HashMap<String,Connection> tmp_connectionMap=new HashMap<String,Connection>();
        for(Host hst:hostMap.values()){
            for(String domain:hst.getDomains()){
                tmp_domainMap.put(domain,hst);
            }
            String bind=hst.getIP()+":"+hst.getPort();
            if(connectionMap.containsKey(bind)){
                tmp_connectionMap.put(bind,connectionMap.remove(bind));
            }else{
                // New binding
                try{
                    Connection connection = new SocketConnection(this);
                    SocketAddress address;
                    if(hst.getIP().equals("*")){
                        address=new InetSocketAddress(hst.getPort());
                    }else{
                        address=new InetSocketAddress(hst.getIP(),hst.getPort());
                    }
                    connection.connect(address);
                    tmp_connectionMap.put(bind,connection);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            if(!hostHandlerMap.containsKey(hst.getId())){
                hostHandlerMap.put(hst.getId(),new HostHandler(hst));
            }
        }
        domainMap=tmp_domainMap;
        for(Connection con:connectionMap.values()){
            try{
                con.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        connectionMap=tmp_connectionMap;
        
    }
    
    public void close(){
        for(Connection con:connectionMap.values()){
            try{
                con.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private void handleError(String msg,Request request,Response response){
        try{
            PrintStream body = response.getPrintStream();
            long time = System.currentTimeMillis();

            response.set("Content-Type", "text/plain");
            response.set("Server", "Ravenous/2.0 (Simple 4.1)");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            response.setCode(500);

            body.println("Server error");
            body.println(msg);
            body.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void handle(Host hst,Request request, Response response){
        try{
            if(hostHandlerMap.containsKey(hst.getId())){
                HostHandler hndl=hostHandlerMap.get(hst.getId());
                hndl.handle(request,response);
            }else handleError("Internal server error... unknown host id",request,response);
        }catch(Exception e){
            e.printStackTrace();
            handleError("Internal server error",request,response);
        }
    }
    
    public void handle(Request request, Response response) {
        Address adr=request.getAddress();
        // System.out.println("adr: "+adr.toString());
        String domain=adr.getDomain();
        int port=adr.getPort();
        // currently does not give us the right port
        if(port==-1)port=80;
        if(PORT_MODE){
            for(Host hst:hostMap.values()){
                if(hst.getPort()==port){
                    handle(hst,request,response);
                    return;
                }
            }
        }else{
            // Implement correct host resolving
            if(domainMap.containsKey("domain")){
            
            }
        
            else{
            
            }
        }
        // current fuckup mode
        for(Host hst:hostMap.values()){
            handle(hst,request,response);
            return;
        }
    }
}