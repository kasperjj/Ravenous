package com.solidosystems.ravenous.db;

import java.lang.annotation.*;
import com.solidosystems.ravenous.host.DatabaseConfiguration;
import com.solidosystems.ravenous.host.EntityConfiguration;
import com.solidosystems.ravenous.host.HostHandler;

import com.solidosystems.ravenous.db.persistence.*;

public class ObjectRelationalMapper{
    private DatabaseConfiguration config;
    private HostHandler handler;
    
    @SuppressWarnings("unchecked")
    public ObjectRelationalMapper(HostHandler handler,DatabaseConfiguration config){
        this.config=config;
        this.handler=handler;
        for(EntityConfiguration entity:config.getEntities()){
            try{
                System.out.println("Loading "+entity.getClassName());
                Class cl=handler.getClass(entity.getClassName());
                Entity en=(Entity)cl.getAnnotation(Entity.class);
                if(en!=null){
                    System.out.println("Yes, an entity");
                }else{
                    System.out.println("No, not an entity");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}