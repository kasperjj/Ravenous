package com.solidosystems.ravenous.host;

import com.solidosystems.ravenous.template.*;
import java.io.*;

public class TemplateHandler{
    private String base;
    
    public TemplateHandler(String path) throws Exception{
        this.base=path;
    }
    
    public Template getTemplate(String subPath){
        try{
            File ftest=new File(base+subPath);
            if(ftest.exists()){
                return new Template(this,base+subPath);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}