package com.solidosystems.ravenous.template;

import com.solidosystems.ravenous.host.TemplateHandler;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.*;

public class Template{
    private TemplateHandler handler;
    private String filename;
    private List<TemplateError> errors;
    private int lineno;
    private String line;
    private List<TemplateBlock> parseTree;
    private TemplateScope scope;
    private StringBuffer buffer;
    private Map<String,String> renderedBlocks;
    private String parentTemplate=null;
    private RequestData request=null;
    
    public Template(TemplateHandler handler,String filename){
        this.handler=handler;
        this.filename=filename;
        errors=new ArrayList<TemplateError>();
        renderedBlocks=new HashMap<String,String>();
    }
    
    public void setRequest(RequestData req){
        request=req;
    }
    
    private void addError(String reason){
        errors.add(new TemplateError(reason,filename,line,lineno));
    }
    
    private void addError(String reason,String line,int lineno){
        errors.add(new TemplateError(reason,filename,line,lineno));
    }
    
    public List<TemplateError> getErrors(){
        if(errors.size()==0)return null;
        return errors;
    }
        
    @SuppressWarnings("unchecked")
    private String getStringValue(Object obj){
        if(obj instanceof java.lang.String)return (String)obj;
        try{
            Class cl=obj.getClass();
            Method m=cl.getDeclaredMethod("toString",new Class[0]);
            Object returnValue=m.invoke(obj);
            if(returnValue instanceof java.lang.String)return (String)returnValue;
            addError("toString value did not return String object");
            return null;
        }catch(Exception e){
           e.printStackTrace();
        }
        addError("Object missing toString method");
        return null;
    }
    
    private List<ExpressionToken> tokenizeExpression(String str) throws Exception{
        List<ExpressionToken> result=new ArrayList<ExpressionToken>();
        String last="";
        StringBuilder collect=null;
        StringTokenizer strtkn=new StringTokenizer(str,".[]()=+-*/\\\"",true);
        while(strtkn.hasMoreTokens()){
            String tkn=strtkn.nextToken();
            if(collect==null){
                if(tkn.equals("\"")){
                    if(last.equals("\\")){
                        addError("Syntax error, unexpected escaped \"");
                        return null;
                    }
                    collect=new StringBuilder();
                    collect.append("\"");
                }else{
                    result.add(new ExpressionToken(tkn,lineno));
                }
            }else{
                collect.append(tkn);
                if(tkn.equals("\"")){
                    if(!last.equals("\\")){
                        result.add(new ExpressionToken(collect.toString(),lineno));
                        collect=null;
                    }
                }
            }
            last=tkn;
        }
        return result;
    }
    
    private ExpressionToken parseExpression(List<ExpressionToken> tokens){
        Stack<ExpressionToken> stack=new Stack<ExpressionToken>();
        for(ExpressionToken token:tokens){
            lineno=token.lineno;
            line=token.str;
            if(token.str.equals("(")){
                stack.push(token);
            }else if(token.str.equals(")")){
                if(stack.empty()){
                    addError("Expression syntax error, unexpected )");
                    return null;
                }else{
                    ExpressionToken element=stack.pop();
                    if(stack.peek().str.equals("(")){
                        stack.pop();
                        stack.push(element);
                    }else{
                        addError("Expression syntax error, unexpected )");
                        return null;
                    }
                }
            }else if(token.str.equals("[")){
                if(stack.empty()){
                    addError("Expression syntax error, found [ without list");
                    return null;
                }else if(stack.peek().str.equals(".")){
                    token.left=stack.pop();
                    stack.push(token);
                }else{
                    addError("Expression syntax error, found [ on a non list object");
                    return null;
                }
            }else if(token.str.equals("]")){
                if(stack.size()<2){
                    addError("Expression syntax error, found ]");
                    return null;
                }else{
                    ExpressionToken index=stack.pop();
                    if(stack.peek().str.equals("[")){
                        stack.peek().right=index;
                    }else{
                        addError("Expression syntax error, found ]");
                        return null;
                    }
                }
            }else if(token.str.equals(".")){
                if(stack.empty()){
                    stack.push(token);
                }else{
                    token.left=stack.pop();
                    stack.push(token);
                }
            }else if(token.str.equals("+")||token.str.equals("-")||token.str.equals("*")||token.str.equals("/")){
                if(stack.empty()){
                    addError("Expression syntax error, found +");
                    return null;
                }else{
                    token.left=stack.pop();
                    stack.push(token);
                }
            }else if(token.str.startsWith("\"")){
                if(stack.empty()){
                    stack.push(token);
                }else{
                    if(stack.peek().right==null){
                        stack.peek().right=token;
                    }else{
                        addError("Expression syntax error, found string literal");
                        return null;
                    }
                }
            }else if(token.str.equals("=")){
                if(stack.empty()){
                    addError("Expression syntax error, found = without left hand expression");
                }else{
                    if(!stack.peek().str.equals("=")){
                        ExpressionToken left=stack.pop();
                        token.left=left;
                        stack.push(token);
                    }
                }
            }else{
                try{
                    int i=Integer.parseInt(token.str);
                    stack.push(token);
                }catch(Exception e){
                    if(stack.empty()){
                        stack.push(new ExpressionToken("."));
                    }else if(!stack.peek().str.equals(".")){
                        stack.push(new ExpressionToken("."));
                    }
                    ExpressionToken root=stack.peek();
                    if(root.right==null){
                        root.right=token;
                    }else{
                        addError("Expression syntax error");
                        return null;
                    }
                }
            }
        }
        if(stack.size()!=1){
            addError("Expression syntax error, unclosed elements");
            return null;
        }
        return stack.pop();
    }
    
    @SuppressWarnings("unchecked")
    private Object evaluateExpression(ExpressionToken root) throws Exception{
        if(root==null)return null;
        if(root.str.equals(".")){
            Object left=evaluateExpression(root.left);
            Object right=evaluateExpression(root.right);
            if(left==null){
                return scope.find(right);
            }else{
                TemplateScope subscope=new TemplateScope(null);
                subscope.putFields(left);
                return subscope.find(right);
            }
        }else if(root.str.equals("[")){
            Object left=evaluateExpression(root.left);
            Object right=evaluateExpression(root.right);
            String str=getStringValue(right);
            if(left instanceof java.util.List){
                try{
                    int i=Integer.parseInt(str);
                    List<Object> list=(List)left;
                    return list.get(i);
                }catch(Exception e){
                    addError("List index is not an integer");
                    return null;
                }
            }else if(left instanceof java.util.Map){
                
            }
        }else if(root.str.equals("=")){
            Object left=evaluateExpression(root.left);
            Object right=evaluateExpression(root.right);
            if(getStringValue(left).equals(getStringValue(right))){
                return new Boolean(true);
            }else{
                return new Boolean(false);
            }
        }else if(root.str.startsWith("\"")){
            String str=root.str;
            str=str.substring(1,str.length()-1);
            return str;
        }
        return root.str;
    }
    
    private Object evaluateExpression(String str) throws Exception{
        List<ExpressionToken> tokens=tokenizeExpression(str);
        ExpressionToken root=parseExpression(tokens);
        return evaluateExpression(root);
    }
    
    private String renderExpression(String str) throws Exception{
        // System.out.println("Render Expresssion");
        // System.out.println(" - Original expression : '"+str+"'");
        List<ExpressionToken> tokens=tokenizeExpression(str);
        // System.out.print(  " - Tokens : ");
        // for(ExpressionToken token:tokens)System.out.print("'"+token.str+"' ");
        // System.out.println();
        ExpressionToken root=parseExpression(tokens);
        // System.out.println(" - Parse tree : ");
        // root.print("   ");
        Object result=evaluateExpression(root);
        if(result!=null){
            return getStringValue(result);
        }
        return null;
    }
    
    /*
    @SuppressWarnings("unchecked")
    private Object locateValue(Object root,Object obj,String expr){
        try{
            System.out.println("Looking for "+expr+" on "+obj.getClass().getName());
            if(expr.indexOf(".")>-1){
                if(expr.indexOf("[")<expr.indexOf(".")){
                    String pre=expr.substring(0,expr.indexOf("."));
                    String post=expr.substring(expr.indexOf(".")+1);
                    Object field=getField(obj,pre);
                    if(field!=null){
                        return locateValue(root,field,post);
                    }else{
                        addError(pre+" is not a valid field");
                        return null;
                    }
                }
            }
            
            if(expr.indexOf("[")>-1){
                if(expr.indexOf("]")>-1){
                    String pre=expr.substring(0,expr.indexOf("["));
                    String post=expr.substring(expr.indexOf("]")+1);
                    String index=expr.substring(expr.indexOf("[")+1);
                    index=index.substring(0,index.indexOf("]"));
                    Object field=getField(obj,pre);
                    if(field!=null){
                        if(field instanceof java.util.List){
                            int i=-1;
                            try{
                                i=Integer.parseInt(index);
                            }catch(Exception e){}
                            if(i==-1){
                                String str=getStringValue(locateValue(root,root,index));

                                try{
                                    i=Integer.parseInt(str);
                                }catch(Exception e){
                                    addError("Could not resolve list index "+index);
                                    return null;
                                }
                            }
                            List<Object> list=(List<Object>)field;
                            if(i<list.size()){
                                return list.get(i);
                            }else{
                                addError("Index out of bounds");
                                return null;
                            }
                        }else if(field instanceof java.util.Map){
                        
                        }
                    }else{
                        addError(pre+" is not a valid field");
                    }
                }else{
                    addError("Syntax error, missing ]");
                    return null;
                }
            }
            
            Object field=getField(obj,expr);
            if(field!=null){
                return field;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        addError(expr+" is not a valid field");
        return null;
    }
    */
    
    public void preParse(String filename) throws Exception{
        List<TemplateBlock> blocks=new ArrayList<TemplateBlock>();
        
        // First split out all tags
        Pattern codePattern=Pattern.compile("\\<\\?([^\\?]+)\\?\\>");
        BufferedReader in=new BufferedReader(new FileReader(filename));
        String str=in.readLine();
        lineno=0;
        while(str!=null){
            line=str;
            lineno++;
            int offset=0;
            Matcher m=codePattern.matcher(str);
            while(m.find()){
                String match=m.group(1).trim();
                String pre=null;
                if(offset<m.start())pre=str.substring(offset,m.start());
                offset=m.end();
                if(pre!=null)blocks.add(new TemplateBlock(TemplateBlock.DATA,pre,lineno));
                blocks.add(new TemplateBlock(TemplateBlock.CODE,match,lineno));
            }
            if(offset<str.length()-1)blocks.add(new TemplateBlock(TemplateBlock.DATA,str.substring(offset),lineno));
            blocks.add(new TemplateBlock(TemplateBlock.DATA,"\n",lineno));
            str=in.readLine();
        }
        in.close();

        // next assign type and split out all expressions
        List<TemplateBlock> blocks2=new ArrayList<TemplateBlock>();
        Pattern valuePattern=Pattern.compile("\\$\\{([^\\}]+)\\}");
        
        for(TemplateBlock block:blocks){
            if(block.type==TemplateBlock.DATA){
                Matcher m=valuePattern.matcher(block.str);
                int offset=0;
                lineno=block.lineno;
                line=block.str;
                while(m.find()){
                    String match=m.group(1).trim();
                    String pre=null;
                    if(offset<m.start())pre=block.str.substring(offset,m.start());
                    offset=m.end();
                    if(pre!=null)blocks2.add(new TemplateBlock(TemplateBlock.DATA,pre,lineno));
                    blocks2.add(new TemplateBlock(TemplateBlock.EXPRESSION,match,lineno));
                }
                if(offset<block.str.length())blocks2.add(new TemplateBlock(TemplateBlock.DATA,block.str.substring(offset),lineno));
            }else{
                if(block.str.startsWith("for")||block.str.startsWith("empty")){
                    block.type=TemplateBlock.FOR;
                    blocks2.add(block);
                }else if(block.str.startsWith("include")){
                    block.type=TemplateBlock.INCLUDE;
                    blocks2.add(block);
                }else if(block.str.startsWith("render")){
                    block.type=TemplateBlock.RENDER;
                    blocks2.add(block);
                }else if(block.str.startsWith("end")){
                    block.type=TemplateBlock.END;
                    blocks2.add(block);
                }else if(block.str.startsWith("cycle")){
                    block.type=TemplateBlock.CYCLE;
                    blocks2.add(block);
                }else if(block.str.startsWith("block")){
                    block.type=TemplateBlock.BLOCK;
                    blocks2.add(block);
                }else if(block.str.startsWith("extends")){
                    block.type=TemplateBlock.EXTENDS;
                    blocks2.add(block);
                }else if(block.str.startsWith("if")){
                    block.type=TemplateBlock.IF;
                    blocks2.add(block);
                }else if(block.str.startsWith("else")){
                    block.type=TemplateBlock.ELSE;
                    blocks2.add(block);
                }else addError("Unknown command "+str,str,block.lineno);
            }
        }
        
        // Now collect and create a tree
        blocks=new ArrayList<TemplateBlock>();
        Stack<TemplateBlock> stack=new Stack<TemplateBlock>();
        for(TemplateBlock block:blocks2){
            if(block.type==TemplateBlock.FOR||block.type==TemplateBlock.BLOCK||block.type==TemplateBlock.IF){
                if(stack.empty())blocks.add(block);
                stack.push(block);
            }else if(block.type==TemplateBlock.END){
                if(stack.empty()){
                    addError("end without beginning for or block",block.str,block.lineno);
                }else{
                    if(stack.peek().type==TemplateBlock.ELSE){
                        TemplateBlock elseBlock=stack.pop();
                        stack.peek().elseChildren=elseBlock.children;
                    }
                    stack.pop();
                }
            }else if(block.type==TemplateBlock.ELSE){
                if(stack.empty()){
                    addError("Else without beginning if or for.",block.str,block.lineno);
                }
                stack.push(block);
            }else{
                if(stack.empty()){
                    blocks.add(block);
                }else{
                    stack.peek().children.add(block);
                }
            }
        }
        if(!stack.empty()){
            TemplateBlock block=stack.pop();
            if(block.type==TemplateBlock.FOR){
                addError("Missing end for for statement",block.str,block.lineno);
            }if(block.type==TemplateBlock.IF){
                addError("Missing end for if statement",block.str,block.lineno);
            }if(block.type==TemplateBlock.BLOCK){
                addError("Missing end for block statement",block.str,block.lineno);
            }
        }
        parseTree=blocks;
    }
    
    @SuppressWarnings("unchecked")
    private void renderRender(TemplateBlock block) throws Exception{
        Pattern renderSyntax=Pattern.compile("render ([^\\s]+) ([^\\s]+)");
        Matcher m=renderSyntax.matcher(block.str);
        if(m.matches()){
            String expression=m.group(1);
            String template=m.group(2);
            Object obj=evaluateExpression(expression);
            Template subTemplate=handler.getTemplate(template);
            if(subTemplate!=null){
                String str=subTemplate.render(scope,obj);
                buffer.append(str);
            }else{
                addError("Template not found.");
            }
        }else{
            addError("Syntax error in render statement");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void renderIf(TemplateBlock block) throws Exception{
        Pattern ifSyntax=Pattern.compile("if (.+)");
        Matcher m=ifSyntax.matcher(block.str);
        if(m.matches()){
            String expression=m.group(1);
            Object obj=evaluateExpression(expression);
            if(obj instanceof java.lang.Boolean){
                Boolean bl=(Boolean)obj;
                if(bl.booleanValue()){
                    for(TemplateBlock child:block.children){
                        renderTemplateBlock(child);
                    }
                }else{
                    for(TemplateBlock child:block.elseChildren){
                        renderTemplateBlock(child);
                    }
                }
            }else{
                addError("Boolean expression expected");
            }
        }else{
            addError("Syntax error in if statement");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void renderBlock(TemplateBlock block) throws Exception{
        Pattern blockSyntax=Pattern.compile("block (.+)");
        Matcher m=blockSyntax.matcher(block.str);
        if(m.matches()){
            String identifier=m.group(1);
            if(parentTemplate==null){
                if(renderedBlocks.containsKey(identifier)){
                   buffer.append(renderedBlocks.get(identifier));
                }else{
                    for(TemplateBlock child:block.children){
                        renderTemplateBlock(child);
                    }
                }
            }else{
                buffer=new StringBuffer();
                for(TemplateBlock child:block.children){
                    renderTemplateBlock(child);
                }
                renderedBlocks.put(identifier,buffer.toString());
            }
        }else{
            addError("Syntax error in block statement");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void renderExtends(TemplateBlock block) throws Exception{
        Pattern extendsSyntax=Pattern.compile("extends (.+)");
        Matcher m=extendsSyntax.matcher(block.str);
        if(m.matches()){
            String filename=m.group(1);
            parentTemplate=filename;
        }else{
            addError("Syntax error in extends statement");
        }
    }
    
    @SuppressWarnings("unchecked")
    private void renderFor(TemplateBlock block) throws Exception{
        Pattern forSyntax=Pattern.compile("for ([^\\s]+) in (.+)");
        Matcher m=forSyntax.matcher(block.str);
        if(m.matches()){
            String identifier=m.group(1);
            String expression=m.group(2);
            Object obj=evaluateExpression(expression);
            if(obj instanceof java.util.List){
                List<Object> list=(List<Object>)obj;
                if(list.size()>0){
                    TemplateScope subscope=new TemplateScope(scope);
                    ForData forData=new ForData();
                    forData.size=list.size();
                    forData.index=0;
                    forData.first=true;
                    if(list.size()==0){
                        forData.last=true;
                    }else{
                        forData.last=false;
                    }
                    subscope.putObject("for",forData);
                    scope=subscope;
                    for(Object object:list){
                        subscope.putObject(identifier,object);
                        for(TemplateBlock child:block.children){
                            renderTemplateBlock(child);
                        }
                        forData.index++;
                        forData.first=false;
                        if(forData.index==forData.size-1)forData.last=true;
                    }
                    scope=scope.parent;
                }else{
                    for(TemplateBlock child:block.elseChildren){
                        renderTemplateBlock(child);
                    }
                }
            }else{
                addError("List expected");
            }
        }else{
            addError("Syntax error in for statement");
        }
    }
    
    public void renderTemplateBlock(TemplateBlock block){
        try{
            line=block.str;
            lineno=block.lineno;
            switch(block.type){
                case TemplateBlock.DATA : buffer.append(block.str);
                    break;
                case TemplateBlock.EXPRESSION : buffer.append(renderExpression(block.str));
                    break;
                case TemplateBlock.FOR : renderFor(block);
                    break;
                case TemplateBlock.IF : renderIf(block);
                    break;
                case TemplateBlock.RENDER : renderRender(block);
                    break;
                case TemplateBlock.BLOCK : renderBlock(block);
                    break;
                case TemplateBlock.EXTENDS : renderExtends(block);
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public String render(TemplateScope scope,Object obj){
        try{
            if(parseTree==null){
                preParse(filename);
            }
            
            TemplateScope subscope=new TemplateScope(null);
            subscope.putObject("request",request);
            subscope.putFields(obj);
            subscope.parent=scope;
            this.scope=subscope;
            buffer=new StringBuffer();
            for(TemplateBlock block:parseTree){
                renderTemplateBlock(block);
            }
            if(parentTemplate!=null){
               Template parent=handler.getTemplate(parentTemplate);
               parent.setRequest(request);
               parent.renderedBlocks=renderedBlocks;
               errors.addAll(parent.errors);
               return parent.render(null,null);
            }else{
                return buffer.toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    public String render(Object obj){
        return render(null,obj);
    }
}