package com.solidosystems.ravenous.host;

import org.yaml.snakeyaml.*;
import java.io.*;
import java.util.*;

public class ConfigHandler{
    private String filename;
    private List<Section> sections;
    private DatabaseConfiguration dbConfiguration;
    
    public ConfigHandler(String filename) throws Exception{
        this.filename=filename;
        reload();
    }
    
    public void reload() throws Exception{
        DatabaseConfiguration dbconf=null;
        List<Section> tsections=new ArrayList<Section>();
        Yaml yaml = new Yaml();
        Object data =null;
        File ftest=new File(filename);
        if(!ftest.exists()){
            data=yaml.load("");
        }else{
            InputStream input = new FileInputStream(new File(filename));
            data=yaml.load(input);
        }
        if(data!=null){
            LinkedHashMap root=(LinkedHashMap)data;
            if(root.containsKey("sections")){
                for(Object obj:(LinkedList)root.get("sections")){
                    LinkedHashMap section=(LinkedHashMap)obj;
                    // System.out.println("Found new section "+((String)section.get("url")).trim()+" "+((String)section.get("class")).trim());
                    Section sec=new Section(((String)section.get("url")).trim(),((String)section.get("class")).trim());
                    tsections.add(sec);
                }
            }else{
                // setup default sections
            }
            if(root.containsKey("database")){
                LinkedHashMap db=(LinkedHashMap)root.get("database");
                String driver=(String)db.get("driver");
                String database=(String)db.get("database");
                String username=(String)db.get("username");
                String password=(String)db.get("password");
                dbconf=new DatabaseConfiguration(driver,database,username,password);
                if(db.containsKey("entities")){
                    for(Object obj:(LinkedList)db.get("entities")){
                        EntityConfiguration ent=new EntityConfiguration((String)((LinkedHashMap)obj).get("class"));
                        dbconf.addEntity(ent);
                    }
                }
            }
        }else{
            // handle empty configuration
        }
        sections=tsections;
        dbConfiguration=dbconf;
    }
    
    public DatabaseConfiguration getDatabaseConfiguration(){
        return dbConfiguration;
    }
    
    public Section getLongestMatch(String path){
        Section result=null;
        int best=-1;
        for(Section section:sections){
            if(path.startsWith(section.getPath())){
                if(best==-1){
                    result=section;
                    best=section.getPath().length();
                }else if(section.getPath().length()>best){
                    result=section;
                    best=section.getPath().length();
                }
            }
        }
        // System.out.println("Looking for "+path);
        // System.out.println("Found "+result.getClassName());
        return result;
    }
}