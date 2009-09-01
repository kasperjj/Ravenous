package com.solidosystems.ravenous.template;

import java.util.*;

public class ExpressionToken{
    public String str;
    public int lineno;
    public List<ExpressionToken> children;
    public ExpressionToken left=null;
    public ExpressionToken right=null;
    
    public ExpressionToken(String str){
        this.str=str;
        children=new ArrayList<ExpressionToken>();
    }
    
    public ExpressionToken(String str,int lineno){
        this.str=str;
        this.lineno=lineno;
        children=new ArrayList<ExpressionToken>();
    }
    
    public void print(String pad){
        System.out.println(pad+"Token : '"+str+"'");
        if(left!=null){
            System.out.println(pad+"  Left");
            left.print(pad+"    ");
        }
        if(right!=null){
            System.out.println(pad+"  Right");
            right.print(pad+"    ");
        }
    }
}