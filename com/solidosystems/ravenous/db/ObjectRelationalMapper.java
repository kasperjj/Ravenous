package com.solidosystems.ravenous.db;

import java.lang.annotation.*;
import com.solidosystems.ravenous.host.DatabaseConfiguration;
import com.solidosystems.ravenous.host.EntityConfiguration;
import com.solidosystems.ravenous.host.HostHandler;

import com.solidosystems.ravenous.db.persistence.*;

public class ObjectRelationalMapper{
    private DatabaseConfiguration config;
    private HostHandler handler;
    
    public ObjectRelationalMapper(HostHandler handler,DatabaseConfiguration config){
        this.config=config;
        this.handler=handler;
        for(EntityConfiguration entity:config.getEntities()){
            try{
                System.out.println("Loading "+entity.getClassName());
                Class cl=handler.getClass(entity.getClassName());
                // Annotation en=cl.getAnnotation(Entity);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}