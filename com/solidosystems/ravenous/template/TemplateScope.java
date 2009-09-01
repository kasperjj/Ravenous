package com.solidosystems.ravenous.template;

import java.util.*;
import java.lang.reflect.*;

public class TemplateScope{
    public TemplateScope parent;
    public Map<String,Object> map;
    
    public TemplateScope(TemplateScope parent){
        this.parent=parent;
        map=new HashMap<String,Object>();
    }
    
    public void putFields(Object obj) throws Exception{
        if(obj==null)return;
        for(Field field:obj.getClass().getDeclaredFields()){
            map.put(field.getName(),field.get(obj));
        }
    }
    
    public void putObject(String name,Object obj){
        if(obj==null)return;
        map.put(name,obj);
    }


    public Object find(Object obj) throws Exception{
        String name=(String)obj;
        if(map.containsKey(name))return map.get(name);
        if(parent!=null)return parent.find(name);
        return null;
    }
}