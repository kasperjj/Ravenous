package com.solidosystems.ravenous.host;

import com.solidosystems.ravenous.db.*;
import com.solidosystems.ravenous.template.*;
import com.solidosystems.ravenous.http.*;
import com.solidosystems.ravenous.web.*;

import org.simpleframework.http.*;
import java.io.PrintStream;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class HostHandler{
    private Host host;
    private FileHandler fileHandler;
    private SourceHandler sourceHandler;
    private ConfigHandler configHandler;
    protected TemplateHandler templateHandler;
    protected DatabasePool dbPool;
    private ObjectRelationalMapper ormHandler;
    private boolean devmode=true;
    
    public HostHandler(Host host){
        this.host=host;
        try{
            fileHandler=new FileHandler(host.getHostRoot()+File.separator+"files"+File.separator);
            sourceHandler=new SourceHandler(host.getHostRoot()+File.separator+"source"+File.separator,host.getHostRoot()+File.separator+"build"+File.separator);
            configHandler=new ConfigHandler(host.getHostRoot()+File.separator+"config.yaml");
            templateHandler=new TemplateHandler(host.getHostRoot()+File.separator+"templates"+File.separator);
            dbPool=new DatabasePool(configHandler.getDatabaseConfiguration());
            ormHandler=new ObjectRelationalMapper(this,configHandler.getDatabaseConfiguration());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public Class getClass(String name) throws java.lang.ClassNotFoundException{
        return sourceHandler.getClass(name);
    }
    
    @SuppressWarnings("unchecked")
    public void handle(Request request, Response response){
        if(devmode){
            sourceHandler.quickCompile();
            if(sourceHandler.compileError()){
                ErrorResponse err=new ErrorResponse();
                err.debug=devmode;
                err.title="Compilation Error";
                err.diagnostics=sourceHandler.getDiagnostics();
                err.message="Failed to compile source.";
                err.handle(request,response);
                return;
            }
        }
        Controller ctrl=null;
        try{
            // Try to find a file that matches the path
            if(fileHandler.handle(request,response))return;
            
            // Try to find a section that matches the path
            String path=request.getPath().getPath();
            Section section=configHandler.getLongestMatch(path);
            if(section!=null){
                Class cl=sourceHandler.getClass(section.getClassName());
                Object obj=cl.newInstance();
                
                if(obj instanceof HostController){
                    ctrl=(Controller)obj;
                    ctrl.RVN_setHandler(this);
                    ctrl.pre();
                }
                
                String subpath=path.substring(section.getPath().length());
                if(subpath.length()==0)subpath="index";
                
                String methodName=subpath;
                if(methodName.indexOf("/")>-1)methodName=methodName.substring(0,methodName.indexOf("/"));
                String argString=subpath.substring(methodName.length()).trim();
                if(argString.startsWith("/"))argString=argString.substring(1);
                if(argString.endsWith("/"))argString=argString.substring(0,argString.length()-1);
                String[] args=argString.split("/");

                List<String> args2=new ArrayList<String>();
                for(String a:args){
                    if(a.length()>0){
                        args2.add(a);
                    }
                }

                Class[] argTypes=new Class[args2.size()];
                for(int i=0;i<args2.size();i++){
                    argTypes[i]=Class.forName("java.lang.String");
                }
                try{
                    Method m=cl.getDeclaredMethod(methodName,argTypes);
                    if(m!=null){
                        Class returnType=m.getReturnType();
                        
                        RequestData req=new RequestData();
                        req.section=section.getPath();
                        req.method=methodName;
                        if(ctrl!=null){
                            ctrl.RVN_setRequestData(req);
                        }
                        
                        Object returnValue=null;
                        if(args2.size()==0){
                            returnValue=m.invoke(obj);
                        }else if(args2.size()==1){
                            returnValue=m.invoke(obj,args2.get(0));
                        }else if(args2.size()==2){
                            returnValue=m.invoke(obj,args2.get(0),args2.get(1));
                        }else if(args2.size()==3){
                            returnValue=m.invoke(obj,args2.get(0),args2.get(1),args2.get(2));
                        }
                        if(ctrl!=null){
                            ctrl.RVN_cleanup();
                            ctrl.post();
                            
                            if(ctrl.RVN_templateErrors!=null){
                                ErrorResponse err=new ErrorResponse();
                                err.debug=devmode;
                                err.title="Template Error";
                                err.message="A call to renderTemplate failed.";
                                err.templateErrors=ctrl.RVN_templateErrors;
                                err.handle(request,response);
                                return;
                            }
                        }
                        if(returnValue!=null){
                            if(returnValue.getClass().getName().equals("java.lang.String")){
                                try{
                                    // set response data
                                    response.set("Server", "Ravenous/2.0 (Simple 4.1)");
                                    response.setDate("Date", System.currentTimeMillis());
                                    PrintStream body = response.getPrintStream();
                                    long time = System.currentTimeMillis();
                                    
                                    if(ctrl!=null){
                                        response.set("Content-Type",ctrl.RVN_mime);
                                    }else response.set("Content-Type", "text/html");
                                    response.setDate("Last-Modified", time);
                                    response.setCode(200);

                                    body.print((String)returnValue);
                                    body.close();
                                    return;
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            // Find template
                            Template template=templateHandler.getTemplate(section.getPath()+methodName+".html");
                            // System.out.println("Template "+section.getPath()+methodName+".html");
                            if(template!=null){
                                try{
                                    
                                    template.setRequest(req);
                                    String rendered=template.render(obj);
                                    if(template.getErrors()!=null){
                                        ErrorResponse err=new ErrorResponse();
                                        err.debug=devmode;
                                        err.title="Template Error";
                                        err.message="The template for the method "+methodName+" could not be rendered.";
                                        err.templateErrors=template.getErrors();
                                        err.handle(request,response);
                                        return;
                                    }
                                    // set response data
                                    response.set("Server", "Ravenous/2.0 (Simple 4.1)");
                                    response.setDate("Date", System.currentTimeMillis());
                                    PrintStream body = response.getPrintStream();
                                    long time = System.currentTimeMillis();

                                    if(ctrl!=null){
                                        response.set("Content-Type",ctrl.RVN_mime);
                                    }else response.set("Content-Type", "text/html");
                                    response.setDate("Last-Modified", time);
                                    response.setCode(200);

                                    body.print(rendered);
                                    body.close();
                                    return;
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }else{
                                // Missing template
                                ErrorResponse err=new ErrorResponse();
                                err.debug=devmode;
                                err.title="Template Not Found";
                                err.message="The template for the method "+methodName+" could not be found.";
                                err.handle(request,response);
                                return;
                            }
                        }
                    }
                }catch(NoSuchMethodException nsme){
                    ErrorResponse err=new ErrorResponse();
                    err.debug=devmode;
                    err.title="Method Not Found";
                    err.message="The method "+methodName+" could not be found on this object.";
                    err.handle(request,response);
                    if(ctrl!=null){
                        ctrl.RVN_cleanup();
                        ctrl.post();
                    }
                    return;
                }
                
            }
            ErrorResponse err=new ErrorResponse();
            err.debug=devmode;
            err.title="Page Not Found";
            err.message="The page "+request.getPath().getPath()+" could not be found on this host.";
            err.handle(request,response);
            if(ctrl!=null){
                ctrl.RVN_cleanup();
                ctrl.post();
            }
            return;
        }catch(Exception e){
            ErrorResponse err=new ErrorResponse();
            err.debug=devmode;
            err.title="Uncaught Exception";
            err.e=e;
            err.message="Response halted by uncaught exception.";
            err.handle(request,response);
            if(ctrl!=null){
                ctrl.RVN_cleanup();
            }
            return;
        }
    }
}