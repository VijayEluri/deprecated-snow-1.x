/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.snowfk.web.renderer.freemarker;

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;


public class PathInfoMatcherTemplateMethod implements TemplateMethodModelEx {
    
    public enum Mode{
        STARTS_WITH,IS;
    }

    private Mode mode;

    
    public PathInfoMatcherTemplateMethod(Mode mode){
        this.mode = mode;
    }
    
    @Override
    public Object exec(List args) throws TemplateModelException {
        String pathInfo = FreemarkerUtil.getDataModel("r.pathInfo",String.class);
        
        String pathInfoMatch = FreemarkerUtil.getParam(args.get(0),String.class);
        
        /*--------- Get the eventual true/false values ---------*/
        String trueValue = null;
        String falseValue = null;
        if (args.size() > 1){
            trueValue = FreemarkerUtil.getParam(args.get(1),String.class);
            falseValue = ""; //by default the false value will be empty
        }
        
        if (args.size() > 2){
            falseValue = FreemarkerUtil.getParam(args.get(2),String.class);
        }
        /*--------- /Get the eventual true/false values ---------*/
        
        /*--------- Match the string ---------*/
        boolean match = false;
        switch(mode){
            case IS:
                if (pathInfo.equals(pathInfoMatch)){
                    match= true;
                }
                break;
            case STARTS_WITH:
                if (pathInfo.startsWith(pathInfoMatch)){
                    match = true;
                }
                break;
                
        }
        /*--------- /Match the string ---------*/
        
        /*--------- Return the value ---------*/
        if (trueValue != null){
            return match?trueValue:falseValue;
        }else{
            return match;
        }

    }
    /*
    private Object execStartWith(String pathInfo, String pathInfoMatch, String trueValue, String falseValue) throws TemplateModelException{
        
        if (pathInfo.startsWith(pathInfoMatch)){
            return trueValue;
        }else{
            return falseValue;
        }        
    }
    
    private Object execIs(String pathInfo, String pathInfoMatch, String trueValue, String falseValue) throws TemplateModelException{
        
        if (pathInfo.equals(pathInfoMatch)){
            return trueValue;
        }else{
            return falseValue;
        }        
    }*/

}
