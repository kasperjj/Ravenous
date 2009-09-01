package com.solidosystems.ravenous.host;

public class EntityConfiguration{
    private String className;
    
    public EntityConfiguration(String className){
        this.className=className;
    }
    
    public String getClassName(){
        return className;
    }
}