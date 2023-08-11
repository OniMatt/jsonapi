package com.syonet.dao;

import java.util.List;

import com.syonet.domain.Cidade;
import com.syonet.generic.GenericDAO;

public class CidadeDAOImpl implements GenericDAO< Cidade > {

    @Override
    public void create( Cidade cidade ) {
        transaction.begin();
        try {
            em.persist( em.contains( cidade ) ? cidade : em.merge( cidade ) );
            transaction.commit();
        } catch ( Exception e ) {
            System.out.println( e );
            transaction.rollback();
        }
    }

    @Override
    public Cidade getById( Integer id ) {
        return em.createQuery( "SELECT c FROM Cidade c WHERE id = " + id, Cidade.class )
            .getSingleResult();
    }

    public List< Cidade > getCidadesByEstado( String estado ) {
        return em.createQuery( "SELECT c FROM Cidade c WHERE estado = '" + estado + "'", Cidade.class )
            .getResultList();
    }

    public List< Cidade > getCidadesByRegiao( String regiao ) {
        return em.createQuery( "SELECT c FROM Cidade c WHERE regiao = '" + regiao + "'", Cidade.class )
            .getResultList();
    }

    @Override
    public List< Cidade > getAll() {
        return em.createQuery( "SELECT c FROM Cidade c", Cidade.class )
            .getResultList();
    }

    @Override
    public void update( Cidade cidade ) {
        transaction.begin();
        try {
            em.merge( cidade );
            transaction.commit();
        } catch ( Exception e ) {
            System.out.println( e );
            transaction.rollback();
        }
    }

    @Override
    public void delete( Cidade cidade ) {
        transaction.begin();
        try {
            em.remove( em.contains( cidade ) ? cidade : em.merge( cidade ) );
            transaction.commit();
        } catch ( Exception e ) {
            System.out.println( e );
            transaction.rollback();
        }
    }
    
}