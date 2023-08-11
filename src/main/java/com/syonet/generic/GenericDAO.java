package com.syonet.generic;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public interface GenericDAO< T > {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory( "apijpa" );
    EntityManager em = emf.createEntityManager();
    EntityTransaction transaction = em.getTransaction();
        
    void create( T objeto );
    T getById( Integer id );
    List< T > getAll();
    void update( T objeto );
    void delete( T objeto );

    default void fechaTransacao() {
        emf.close();
        em.close();
    }
}