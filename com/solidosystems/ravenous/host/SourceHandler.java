package com.solidosystems.ravenous.host;

import java.net.URLClassLoader;
import java.net.URL;
import javax.tools.*;
import java.util.*;
import java.io.*;
import java.nio.charset.Charset;

public class SourceHandler{
    private String sourcePath;
    private String buildPath;
    private boolean validRoot=false;
    private List<String> pathList;
    
    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;
    private DiagnosticCollector<JavaFileObject> diagnostics;
    private URLClassLoader loader;
    private boolean compileError;
    
    public SourceHandler(String sourcePath,String buildPath){
        this.sourcePath=sourcePath;
        this.buildPath=buildPath;
        compiler=ToolProvider.getSystemJavaCompiler();
        diagnostics = new DiagnosticCollector<JavaFileObject>();
        fileManager=compiler.getStandardFileManager(diagnostics, null, null);
        try{
            
            File ftest=new File(sourcePath);
            if(ftest.exists()){
                validRoot=true;
                ftest=new File(buildPath);
                if(!ftest.exists()){
                    ftest.mkdir();
                }
                quickCompile();
                resetClassLoader();
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public boolean compileError(){
        return compileError;
    }
    
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics(){
        return diagnostics.getDiagnostics();
    }
    
    public void resetClassLoader() throws java.net.MalformedURLException{
        File location = new File(buildPath);
        URL[] urls = new URL[]{location.toURI().toURL()};
        loader = new URLClassLoader(urls);
    }
    
    public Class getClass(String name) throws java.lang.ClassNotFoundException{
        return loader.loadClass(name);
    }
    
    private void compile(String filename) throws Exception{
        File file = new File(sourcePath+filename);
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(file);
        String[] options = new String[]{"-d", buildPath,"-cp",buildPath+":"+this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()};
        if(!compiler.getTask(null, fileManager, diagnostics, Arrays.asList(options), null, fileObjects).call()){
            compileError=true;
        }
    }
    
    public synchronized void quickCompile(){
        try{
            compileError=false;
            diagnostics = new DiagnosticCollector<JavaFileObject>();
            if(fileManager!=null)fileManager.close();
            fileManager=compiler.getStandardFileManager(diagnostics, null, null);
            boolean dirty=false;
            pathList=new ArrayList<String>();
            pathList.add("");
            while(pathList.size()>0){
                String path=pathList.remove(0);
                File ftest=new File(sourcePath+path);
                for(String file:ftest.list()){
                    ftest=new File(sourcePath+path+File.separator+file);
                    if(ftest.isDirectory()){
                        pathList.add(path+File.separator+file);
                    }else if(file.endsWith(".java")){
                        File dtest=new File(buildPath+path+File.separator+file.substring(0,file.length()-5)+".class");
                        if(dtest.exists()){
                            if(dtest.lastModified()<ftest.lastModified()){
                                compile(path+File.separator+file);
                                dirty=true;
                            }
                        }else{
                            compile(path+File.separator+file);
                            dirty=true;
                        }
                    }
                }
            }
            if(dirty){
                resetClassLoader();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public synchronized void reCompile(){
        try{
            compileError=false;
            diagnostics = new DiagnosticCollector<JavaFileObject>();
            if(fileManager!=null)fileManager.close();
            fileManager=compiler.getStandardFileManager(diagnostics, null, null);
            pathList=new ArrayList<String>();
            pathList.add("");
            while(pathList.size()>0){
                String path=pathList.remove(0);
                File ftest=new File(sourcePath+path);
                for(String file:ftest.list()){
                    ftest=new File(sourcePath+path+File.separator+file);
                    if(ftest.isDirectory()){
                        pathList.add(path+File.separator+file);
                    }else if(file.endsWith(".java")){
                        compile(path+File.separator+file);
                    }
                }
            }
            resetClassLoader();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}