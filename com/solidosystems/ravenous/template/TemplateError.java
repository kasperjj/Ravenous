package com.solidosystems.ravenous.template;

public class TemplateError{
    private String error;
    private String file;
    private String line;
    private int lineno;
    
    public TemplateError(String error,String file,String line,int lineno){
        this.error=error;
        this.file=file;
        this.line=line;
        this.lineno=lineno;
    }
    
    public String getError(){
        return error;
    }
    
    public String getFile(){
        return file;
    }
    
    public String getLine(){
        return line;
    }
    
    public int getLineNo(){
        return lineno;
    }
}