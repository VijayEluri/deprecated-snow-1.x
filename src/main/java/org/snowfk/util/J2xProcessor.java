/* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.snowfk.util;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import static org.snowfk.util.Jx.x;

import org.snowfk.SnowRuntimeException;

import org.w3c.dom.Element;

public class J2xProcessor {

    public enum Alert {
        CANNOT_INIT_JAXBCONTEXT, CANNOT_CREATE_OBJECT_FROM_XML_ELEMENT, CANNOT_CREATE_XML_FROM_OBJECT;
    }

    JAXBContext jaxbContext = null;
    Class[]     jaxbClasses = null;

    public J2xProcessor() {

    }

    public J2xProcessor(List<Class> jaxbClassesList) {
        Class[] jaxbClasses = new Class[jaxbClassesList.size()];
        jaxbClassesList.toArray(jaxbClasses);
        init(jaxbClasses);
    }

    public J2xProcessor(Class[] jaxbClasses) {
        init(jaxbClasses);
    }

    private void init(Class[] jaxbClasses) {
        this.jaxbClasses = jaxbClasses;
        try {
            jaxbContext = JAXBContext.newInstance(jaxbClasses);
        } catch (JAXBException e) {
            throw new SnowRuntimeException(Alert.CANNOT_INIT_JAXBCONTEXT, e, "jaxbClasses", jaxbClasses);
        }
    }

    /*--------- J2x Methods ---------*/
    @SuppressWarnings("unchecked")
    public <T> T getObjectFromXml(Element element, Class<T> cls) throws SnowRuntimeException {
        T obj = null;
        if (element != null) {
            Unmarshaller m;
            try {
                m = jaxbContext.createUnmarshaller();
                obj = (T) m.unmarshal(element);
            } catch (JAXBException e) {
                throw new SnowRuntimeException(Alert.CANNOT_CREATE_OBJECT_FROM_XML_ELEMENT, e);
            }
        }

        return obj;
    }

    /**
     * Serialize an Java object to XML Element. if the rootElement is null,
     * then, an empty <result/> element will be create which will contain the
     * result of the serialization.
     * 
     * @param obj
     * @param rootElement
     * @return
     * @throws SnowRuntimeException
     */
    public Element getElementFromObject(Object obj, Element rootElement) throws SnowRuntimeException {
        Element value = null;
        Marshaller m;
        try {
            m = jaxbContext.createMarshaller();
            if (rootElement == null) {
                rootElement = x("<result/>").e();
            }
            m.marshal(obj, rootElement);
            value = (Element) rootElement.getLastChild();

        } catch (JAXBException e) {
            throw new SnowRuntimeException(Alert.CANNOT_CREATE_XML_FROM_OBJECT, e, "object", obj);
        }
        return value;
    }
    /*--------- /J2x Methods ---------*/

}
