package com.solidosystems.ravenous.host;

public class Section{
    private String path;
    private String className;

    public Section(String path,String className){
        this.path=path;
        this.className=className;
    }
    
    public String getPath(){
        return path;
    }
    
    public String getClassName(){
        return className;
    }
}