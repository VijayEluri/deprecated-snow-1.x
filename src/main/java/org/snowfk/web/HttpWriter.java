package org.snowfk.web;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowfk.util.FileUtil;
import org.snowfk.util.MapUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HttpWriter {
    static private Logger  logger      = LoggerFactory.getLogger(HttpWriter.class);

    public static int      BUFFER_SIZE = 2048 * 2;

    @Inject(optional=true)
    private ServletContext servletContext;


    public String getContentType(String resourcePath) {
        String contentType = null;
        if (servletContext != null){
            servletContext.getMimeType(resourcePath);
        }
        // if the servletContext (server) could not fine the mimeType, then, give a little help
        if (contentType == null) {
            contentType = FileUtil.getExtraMimeType(resourcePath);
        }
        return contentType;
    }

    public void writeFile(RequestContext rc,File file, boolean cache, Map options) throws Throwable{
        String contentType = FileUtil.getExtraMimeType(file.getAbsolutePath());
        
        if (contentType != null && (contentType.startsWith("text") || contentType.indexOf("javascript") != -1)) {
           FileReader reader = new FileReader(file);
           writeStringContent(rc, file.getName(), reader, cache, options);
        }else{
            InputStream fileIs = new FileInputStream(file);
            writeBinaryContent(rc,file.getName(),fileIs,cache,options);
        }
    }
    
    
    /**
     * Still experimental
     * 
     * @param rc
     * @param fileName
     * @param content
     * @param cache
     *            (for now, not supported, only no-cache)
     * @param options
     * @throws Exception
     */
    public void writeStringContent(RequestContext rc, String fileName, Reader contentReader, boolean cache, Map options)
                            throws Exception {
        
        HttpServletRequest req = rc.getReq();
        String characterEncoding = MapUtil.getNestedValue(options, "characterEncoding");
        characterEncoding = (characterEncoding != null) ? characterEncoding : "UTF-8";
        req.setCharacterEncoding(characterEncoding);

        setHeaders(rc, fileName, cache, options);

        // --------- Stream File --------- //
        Writer ow = null;
        try {

            // create the reader/writer
            ow = rc.getRes().getWriter();

            char[] buffer = new char[BUFFER_SIZE];
            int readLength = contentReader.read(buffer);

            while (readLength != -1) {
                ow.write(buffer, 0, readLength);
                readLength = contentReader.read(buffer);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            contentReader.close();
            if (ow != null) {
                ow.close();
            }
        }
    }

    public void writeBinaryContent(RequestContext rc, String fileName, InputStream contentIS, boolean cache, Map options)
                            throws Exception {

        setHeaders(rc, fileName, cache, options);

        OutputStream os = rc.getRes().getOutputStream();

        BufferedInputStream bis = new BufferedInputStream(contentIS);

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = buffer.length;
            while (true) {
                len = bis.read(buffer);
                if (len == -1)
                    break;
                os.write(buffer, 0, len);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            os.close();
            contentIS.close();
            bis.close();
        }
    }

    private void setHeaders(RequestContext rc, String fileName, Boolean cache, Map options) throws Exception {
        HttpServletResponse res = rc.getRes();

        String contentType = MapUtil.getNestedValue(options, "contentType");
        contentType = (contentType != null) ? contentType : getContentType(fileName);
        res.setContentType(contentType);

        // TODO: needs to support "cache=true"

        // --------- Set Cache --------- //

        if (cache) {
            /*
             * NOTE: for now we remove this, in the case of a CSS, we do not know the length, since it is a template
             * contentLength = resourceFile.length();
             * 
             * if (contentLength < Integer.MAX_VALUE) { res.setContentLength((int) contentLength); } else {
             * res.setHeader("content-length", "" + contentLength); }
             */
            // This content will expire in 1 hours.
            final int CACHE_DURATION_IN_SECOND = 60 * 60 * 1; // 1 hours
            final long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND * 1000;
            long now = System.currentTimeMillis();

            res.addHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
            res.addHeader("Cache-Control", "must-revalidate");// optional
            res.setDateHeader("Last-Modified", now);
            res.setDateHeader("Expires", now + CACHE_DURATION_IN_MS);
        } else {
            res.setHeader("Pragma", "No-cache");
            res.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
            res.setDateHeader("Expires", 1);
        }

        // --------- Set Cache --------- //
    }

}
