package com.syonet;

import java.util.ArrayList;
import java.util.List;

import com.syonet.api.APIDosGuri;
import com.syonet.dao.PaisDAOImpl;
import com.syonet.domain.Cidade;
import com.syonet.domain.Pais;

public class Main {
    public static void main( String[] args ) {   
        APIDosGuri paisApi = new APIDosGuri();  
        PaisDAOImpl pdao = new PaisDAOImpl();
        List< Cidade > cidades = new ArrayList<>(); 
        Pais pais = new Pais("Brasil", cidades);
        Cidade cidade = new Cidade("Montenegro", "Rio Grande do Sul", "Sul", pais);
        cidades.add(cidade);
        pdao.create(pais);
        
        paisApi.start();
    }
}
