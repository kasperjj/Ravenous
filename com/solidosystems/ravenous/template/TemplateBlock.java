package com.solidosystems.ravenous.template;

import java.util.*;

public class TemplateBlock{
    public static final int DATA=0;
    public static final int EXPRESSION=1;
    public static final int CODE=2;
    public static final int BLOCK=3;
    public static final int INCLUDE=4;
    public static final int RENDER=5;
    public static final int CYCLE=6;
    public static final int EXTENDS=7;
    public static final int FOR=8;
    public static final int IF=9;
    public static final int ELSE=10;
    public static final int END=11;
    
    public String str;
    public int lineno;
    public int type;

    public List<TemplateBlock> children;
    public List<TemplateBlock> elseChildren;
    
    public TemplateBlock(int type,String str,int lineno){
        this.type=type;
        this.str=str;
        this.lineno=lineno;
        children=new ArrayList<TemplateBlock>();
        elseChildren=new ArrayList<TemplateBlock>();
    }
}