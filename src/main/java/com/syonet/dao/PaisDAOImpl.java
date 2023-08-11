package com.syonet.dao;

import java.util.List;

import javax.persistence.NoResultException;

import com.syonet.domain.Pais;
import com.syonet.generic.GenericDAO;

public class PaisDAOImpl implements GenericDAO< Pais > {

    @Override
    public void create( Pais pais ) {
        transaction.begin();
        try {
            em.persist(em.contains(pais) ? pais : em.merge(pais));
            transaction.commit();
        } catch ( Exception e ) {
            System.out.println( e );
            transaction.rollback();
        }
    }

    @Override
    public Pais getById( Integer id ) {
      try {
        return em.createQuery( "SELECT p FROM Pais p WHERE id = " + id, Pais.class )
            .getSingleResult();
      } catch (NoResultException e) {
        e.printStackTrace();
        return null;
      }
    }

    @Override
    public List< Pais > getAll() {
        return em.createQuery( "SELECT p FROM Pais p", Pais.class )
            .getResultList();
    }

    @Override
    public void update( Pais pais ) {
        transaction.begin();
        try {
            em.merge( pais );
            transaction.commit();
        } catch ( Exception e ) {
            System.out.println( e );
            transaction.rollback();
        }
    }

    @Override
    public void delete( Pais pais ) {
        transaction.begin();
        try {
            em.remove(em.contains(pais) ? pais : em.merge(pais));
            transaction.commit();
        } catch ( Exception e ) {
            System.out.println( e );
            transaction.rollback();
        }
    }
    
}