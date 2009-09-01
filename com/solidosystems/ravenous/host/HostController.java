package com.solidosystems.ravenous.host;

import com.solidosystems.ravenous.template.Template;
import com.solidosystems.ravenous.template.TemplateError;
import com.solidosystems.ravenous.template.RequestData;
import com.solidosystems.ravenous.db.*;
import java.sql.Connection;
import java.util.*;

public class HostController{
    protected List<DatabaseConnection> RVN_connections=new LinkedList<DatabaseConnection>();
    protected HostHandler RVN_handler;
    protected RequestData RVN_req;
    protected String RVN_mime="text/html";
    protected List<TemplateError> RVN_templateErrors=null;
    
    protected void RVN_setRequestData(RequestData req){
        this.RVN_req=req;
    }
    
    protected void RVN_setHandler(HostHandler handler){
        this.RVN_handler=handler;
    }
    
    protected void RVN_cleanup(){
        for(DatabaseConnection con:RVN_connections){
            RVN_handler.dbPool.returnConnection(con);
        }
    }

    public Connection getJDBCConnection(){
        DatabaseConnection con=RVN_handler.dbPool.getConnection();
        RVN_connections.add(con);
        return con.connection;
    }
    
    public void setMimeType(String mime){
        this.RVN_mime=mime;
    }
    
    public String renderTemplate(String filename) throws Exception{
        return renderTemplate(filename,this);
    }
    
    public String renderTemplate(String filename,Object object) throws Exception{
        Template template=RVN_handler.templateHandler.getTemplate(filename);
        if(template!=null){
           template.setRequest(RVN_req);
           String rendered=template.render(object);
           if(template.getErrors()!=null){
               RVN_templateErrors=template.getErrors();
               return null;
           }else{
               return rendered;
           }
        }
        return null;
    }
}