package com.solidosystems.ravenous.host;

import com.solidosystems.ravenous.http.*;
import org.simpleframework.http.*;
import java.io.PrintStream;
import java.util.*;
import java.io.*;

public class FileHandler{
    private String path;
    private boolean validRoot=false;
    
    public FileHandler(String path){
        this.path=path;
        try{
            File ftest=new File(path);
            if(ftest.exists()){
                validRoot=true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public boolean handle(Request request, Response response){
        if(!validRoot)return false;
        try{
            String relativePath=request.getPath().getPath().substring(1);
            File ftest=new File(path+relativePath);
            if(ftest.exists()&&ftest.isFile()){
                OutputStream out=response.getOutputStream();
                InputStream in=new FileInputStream(path+relativePath);
                long size=ftest.length();
                byte[] buf=new byte[32768];
                while(size>0){
                    int read=in.read(buf,0,32768);
                    if(read>0)out.write(buf,0,read);
                    size=size-read;
                }
                out.flush();
                out.close();
                in.close();
                
                response.setDate("Last-Modified", ftest.lastModified());
                response.set("Content-Type", getContentType(relativePath));
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    private String getContentType(String path){
        if(path.indexOf(".")>-1){
            String suffix=path.substring(path.lastIndexOf(".")).toLowerCase();
            
            if(suffix.equals(".htm"))return "text/html";
            if(suffix.equals(".html"))return "text/html";
            if(suffix.equals(".txt"))return "text/plain";
            if(suffix.equals(".rtf"))return "text/richtext";
            if(suffix.equals(".rtx"))return "text/richtext";
            if(suffix.equals(".sgml"))return "text/sgml";
            if(suffix.equals(".css"))return "text/css";
            if(suffix.equals(".js"))return "text/javascript";
            if(suffix.equals(".mpg"))return "video/mpeg";
            if(suffix.equals(".mpe"))return "video/mpeg";
            if(suffix.equals(".mpeg"))return "video/mpeg";
            if(suffix.equals(".mov"))return "video/quicktime";
            if(suffix.equals(".qt"))return "video/quicktime";
            if(suffix.equals(".avi"))return "video/x-msvideo";
            if(suffix.equals(".movie"))return "video/x-sgi-movie";
            if(suffix.equals(".wmv"))return "video/x-ms-wmv";
            if(suffix.equals(".asf"))return "video/x-ms-asf";
            if(suffix.equals(".wvx"))return "video/x-ms-wvx";
            if(suffix.equals(".asx"))return "video/x-ms-asf";
            if(suffix.equals(".rv"))return "video/vnd.rn-realvideo";
            if(suffix.equals(".doc"))return "application/msword";
            if(suffix.equals(".pdf"))return "application/pdf";
            if(suffix.equals(".ai"))return "application/postscript";
            if(suffix.equals(".eps"))return "application/postscript";
            if(suffix.equals(".ps"))return "application/postscript";
            if(suffix.equals(".gz"))return "application/x-gtar";
            if(suffix.equals(".tar"))return "application/x-tar";
            if(suffix.equals(".class"))return "application/x-java-vm";
            if(suffix.equals(".jar"))return "application/x-java-archive";
            if(suffix.equals(".zip"))return "application/zip";
            if(suffix.equals(".tex"))return "application/x-tex";
            if(suffix.equals(".hqx"))return "application/mac-binhex40";
            if(suffix.equals(".sit"))return "application/x-stuffit";
            if(suffix.equals(".sitx"))return "application/x-stuffitx";
            if(suffix.equals(".dmg"))return "application/octet-stream";
            if(suffix.equals(".rss"))return "application/xml";
            if(suffix.equals(".xml"))return "application/xml";
            if(suffix.equals(".spi"))return "application/futuresplash";
            if(suffix.equals(".dcr"))return "application/x-director";
            if(suffix.equals(".dir"))return "application/x-director";
            if(suffix.equals(".dxr"))return "application/x-director";
            if(suffix.equals(".swf"))return "application/x-shockwave-flash";
            if(suffix.equals(".au"))return "audio/basic";
            if(suffix.equals(".snd"))return "audio/basic";
            if(suffix.equals(".ra"))return "audio/vnd.rn-realaudio";
            if(suffix.equals(".ram"))return "audio/vnd.rn-realaudio";
            if(suffix.equals(".wax"))return "audio/x-ms-wax";
            if(suffix.equals(".wav"))return "audio/x-wav";
            if(suffix.equals(".aiff"))return "audio/x-aiff";
            if(suffix.equals(".aif"))return "audio/x-aiff";
            if(suffix.equals(".mid"))return "audio/x-midi";
            if(suffix.equals(".wma"))return "audio/x-ms-wma";
            if(suffix.equals(".gif"))return "image/gif";
            if(suffix.equals(".jpg"))return "image/jpeg";
            if(suffix.equals(".jpeg"))return "image/jpeg";
            if(suffix.equals(".jpe"))return "image/jpeg";
            if(suffix.equals(".png"))return "image/png";
            if(suffix.equals(".tif"))return "image/tiff";
            if(suffix.equals(".tiff"))return "image/tiff";
            if(suffix.equals(".bmp"))return "image/bmp";
        }
        return "application/octet-stream";
    }
}