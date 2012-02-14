package org.snowfk.web.db.hibernate;

import org.hibernate.SessionFactory;

public interface HibernateSessionFactoryBuilder{
    
    public SessionFactory getSessionFactory();
}