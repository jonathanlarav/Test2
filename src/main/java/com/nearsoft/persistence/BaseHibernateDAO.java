package com.nearsoft.persistence;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class BaseHibernateDAO<T, K extends Serializable> {

    private final Class<T> entityClass = getEntityClass();

    protected abstract Class<T> getEntityClass();

    protected abstract SessionFactory getSessionFactory();

    private Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    protected T find(K id) {
        return (T) getCurrentSession().get(getEntityClass(), id);
    }

    protected List<T> find() {
        return findByCriteria(true);
    }

    protected boolean create(T type) {
        return getCurrentSession().save(type) != null;
    }

    protected void update(T type) {
        getCurrentSession().saveOrUpdate(type);
    }

    protected void delete(T type) {
        getCurrentSession().delete(type);
    }

    protected Criteria createCriteria(boolean cacheable, Criterion... criterion) {
        Criteria criteria = getCurrentSession().createCriteria(getEntityClass());
        for (Criterion criterionItem : criterion) {
            criteria.add(criterionItem);
        }
        criteria.setCacheable(cacheable);
        return criteria;
    }

    protected List<T> findByCriteria(boolean cacheable, Criterion... criterion) {
        Criteria criteria = createCriteria(cacheable, criterion);
        return criteria.list();
    }
}
