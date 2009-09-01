package com.solidosystems.ravenous.server;

import com.solidosystems.ravenous.http.Host;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class RavenousServer implements Runnable{
    public RavenousServer(){
        try{
            status("Parsing host configuration");
            InputStream input = new FileInputStream(new File("config/hostlist.conf"));
                Yaml yaml = new Yaml();
                for (Object data : yaml.loadAll(input)) {
                    LinkedHashMap map=(LinkedHashMap)data;
                    // Host hst=new Host(map);                    
                }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void status(String str){
        System.out.println(str);
    }
    
    public void run(){
        
    }
    
    public static void main(String[] args){
        new RavenousServer();
    }
}