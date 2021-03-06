/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.snowfk.web.renderer.freemarker;



import static org.snowfk.web.renderer.freemarker.FreemarkerUtil.getDataModel;
import static org.snowfk.web.renderer.freemarker.FreemarkerUtil.getParam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.snowfk.SnowRuntimeException;
import org.snowfk.util.ObjectUtil;
import org.snowfk.web.PathFileResolver;
import org.snowfk.web.RequestContext;
import org.snowfk.web.renderer.WebBundleManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;



import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

@Singleton
public class WebBundleDirective implements TemplateDirectiveModel {
    public enum Alert{
        NOT_VALID_WEBBUNDLE_PATH, NOT_VALID_WEBBUNDLE_TYPE;
    }
    static final private String DEBUG_LINK_STRING = "_debug_links";
    private enum LinkItemType {
        css, js
    };

    @Inject
    private WebBundleManager webBundleManager;
    @Inject
    private PathFileResolver pathFileResolver;
    
    
    @Override
    public void execute(Environment env, Map args, TemplateModel[] tms, TemplateDirectiveBody body)
                            throws TemplateException, IOException {

        RequestContext rc = getDataModel("r.rc", RequestContext.class);
        
        Boolean debug_links = rc.getParam(DEBUG_LINK_STRING, Boolean.class, null);
        //if not null, make sure we set the cookie with the value
        if (debug_links != null){
            rc.setCookie(DEBUG_LINK_STRING, debug_links);
        }
        //if there is not debug_link param in the URL, check the cookie (set false if not found)
        else{
            debug_links = rc.getCookie(DEBUG_LINK_STRING, Boolean.class, false);
        }
        
        String contextPath = rc.getContextPath();
        
        
        // [@webBundle folder="/css/" type="js" /]
        //get the param
        String path = getParam(args,"path",String.class); 
        if (!path.endsWith("/")){
            path = path + "/";
        }
        String webPath = contextPath + path;
        String typeStr = getParam(args,"type",String.class);
        String key = getParam(args,"key",String.class);
        
        LinkItemType type = ObjectUtil.getValue(typeStr, LinkItemType.class, null);
        if (type == null){
            throw new SnowRuntimeException(Alert.NOT_VALID_WEBBUNDLE_TYPE,"type",typeStr);
        }
        String fileExt = "." + type.name();

        BufferedWriter bw = new BufferedWriter(env.getOut());
        
        //Part part = webApplication.getPart(path);
        //File folder = part.getResourceFile();
        File folder = pathFileResolver.resolve(path);
        
        if (!folder.exists()){
            throw new SnowRuntimeException(Alert.NOT_VALID_WEBBUNDLE_PATH,"path",folder.getAbsolutePath());
        }
        
        StringBuilder sb = new StringBuilder();
        
        //if debug mode, include all the files
        
        if (debug_links){
            List<File> files = webBundleManager.getWebBundleFiles(folder, fileExt);
            for (File file : files){
                sb.append(buildHtmlTag(webPath + file.getName(), type));
            }
        }
        //if not debug mode, then, include the "_web_bundle_all..."
        else{
            //if there is no key, then the key is the latest type
            if (key == null){
                List<File> files = webBundleManager.getWebBundleFiles(folder, fileExt);
                Long lasttime = 0L;
                for (File file : files){
                    Long t = file.lastModified();
                    if (t > lasttime){
                        lasttime = t;
                    }
                }
                key = "" + lasttime;
            }
            
            StringBuilder sbHref = new StringBuilder(webPath).append("_web_bundle_all");
            // if we have a key, then add it.
            if (key.length() > 0){
               sbHref.append("__").append(key).append("__");  
            }
            sbHref.append(fileExt);
            
            sb.append(buildHtmlTag(sbHref.toString(),type));
            
            
        }
        
        bw.write(sb.toString());
        
        //System.out.println("WebBundleTest... " + path + " " + type + " " + part.getResourceFile().getAbsolutePath());
        
        bw.flush();
    }

    private String buildHtmlTag(String href,LinkItemType type){
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case css:
                sb.append("<link type='text/css' href='");
                sb.append(href);
                sb.append("'  rel='stylesheet'  />\n");
                break;
            case js:
                sb.append("<script type='text/javascript' src='");
                sb.append(href);
                sb.append("'></script>\n");
                break;
        }        
        return sb.toString();
    }

}
