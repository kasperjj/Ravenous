package com.solidosystems.ravenous.http;

import com.solidosystems.ravenous.template.*;
import org.simpleframework.http.*;
import java.io.PrintStream;
import java.util.*;
import java.io.*;
import javax.tools.*;

public class ErrorResponse{
    public int code=500;
    public String title="Server Error";
    public String message="Sorry, an error occured.";
    public Exception e=null;
    public boolean debug=false;
    public List<Diagnostic<? extends JavaFileObject>> diagnostics=null;
    public List<TemplateError> templateErrors=null;
    
    
    public void handle(Request request, Response response){
        try{
            PrintStream body = response.getPrintStream();
            long time = System.currentTimeMillis();

            response.set("Content-Type", "text/html");
            response.set("Server", "Ravenous/2.0 (Simple 4.1)");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            response.setCode(code);

            body.println("<html>");
            body.println("  <head>");
            body.println("    <title>"+title+"</title>");
            body.println("  </head>");
            body.println("  <body>");
            body.println("    <h1>"+code+" "+title+"</h1>");
            body.println("    <p>"+message+"</p>");
            
            if(e!=null){
                body.println("<h2>"+e.toString()+"</h2>");
                body.println("<table>");
                Throwable c=e.getCause();
                while(c!=null){
                    if(c instanceof Error==false){
                        body.println("<tr><td colspan=\"2\">"+c.toString()+"</td></tr>");
                        StackTraceElement[] cstack=c.getStackTrace();
                        for(int i=0;i<cstack.length;i++){
                            body.println("<tr><td>&nbsp;</td><td>"+cstack[i].toString()+"</td></tr>");
                        }
                    }
                    c=c.getCause();
                }
                body.println("<tr><td colspan=\"2\">&nbsp;</td></tr>");
                StackTraceElement[] stack=e.getStackTrace();
                for(int i=0;i<stack.length;i++){
                    body.println("<tr><td>&nbsp;</td><td>"+stack[i].toString()+"</td></tr>");
                }
                body.println("</table>");
                
            }
            if(diagnostics!=null){
                body.println("<h2>Compilation Errors</h2>");
                body.println("<table>");
                for(Diagnostic diagnostic : diagnostics){
                    String msg=diagnostic.getMessage(null);
                    if(msg.startsWith("/")){
                        if(msg.indexOf("source")>-1)msg=msg.substring(msg.indexOf("source")+6);
                    }
                    body.println("<tr><td>"+msg+"</td></tr>");
                }                    
                body.println("</table>");
            }
            
            if(templateErrors!=null){
                body.println("<h2>Template Errors</h2>");
                body.println("<table>");
                for(TemplateError error : templateErrors){                    
                    String msg=error.getFile()+":"+error.getLineNo()+":"+error.getError();
                    if(msg.indexOf("templates")>-1)msg=msg.substring(msg.indexOf("templates")+10);

                    body.println("<tr><td>"+msg+"</td></tr>");
                }                    
                body.println("</table>");
            }
            
            if(debug){
                body.println("<h2>Request</h2>");
                body.println("<table>");
                body.println("<tr><td>HTTP Method</td><td>"+request.getMethod()+"</td></tr>");
                body.println("<tr><td>HTTP Version</td><td>"+request.getMajor()+"."+request.getMinor()+"</td></tr>");
                body.println("<tr><td>Path</td><td>"+request.getPath()+"</td></tr>");
                body.println("<tr><td>Query</td><td>"+request.getQuery().toString()+"</td></tr>");
                body.println("</table>");
            }
            
            body.println("  </body>");
            body.println("</html>");
            body.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}