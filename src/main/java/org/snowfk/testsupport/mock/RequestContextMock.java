package org.snowfk.testsupport.mock;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.snowfk.web.RequestContext;

public class RequestContextMock extends RequestContext {

    HttpServletRequestMock req;
    HttpServletResponseMock res; 
                            
    @Inject
    public RequestContextMock(HttpServletRequestMock req,HttpServletResponseMock res, ServletContext servletContext){
        super(req,res,servletContext,null);
        this.req = req;
        this.res = res;
    }
    
    
    public void setRequestMethod(String method){
        req.setMethod(method);
    }
    
    
    public void setParamMap(Map params){
        super.setParamMap(params);
    }
    
    public void setPathInfo(String pathInfo){
        req.setPathInfo(pathInfo);
    }
    
    public String getResponseAsString(){
        return res.getResponseAsString();
    }
    
    public byte[] getResponseAsByArray(){
        return res.getResponseAsByteArray();
    }
    
    public void init(){
        super.init();
    }
}
