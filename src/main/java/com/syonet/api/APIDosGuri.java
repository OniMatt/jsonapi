package com.syonet.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syonet.dao.CidadeDAOImpl;
import com.syonet.dao.PaisDAOImpl;
import com.syonet.domain.Cidade;
import com.syonet.domain.Pais;
import com.syonet.generic.GenericDAO;

public class APIDosGuri {
  private final int PORT = 8084;
  private final String OK_RESPONSE = "HTTP/1.1 200 OK\r\n\r\n";
  private final String BAD_REQUEST_RESPONSE = "HTTP/1.1 400 Bad Request\r\n\r\n";

  private PaisDAOImpl pdao = new PaisDAOImpl();
  private CidadeDAOImpl cdao = new CidadeDAOImpl();
  private ObjectMapper objectMapper = new ObjectMapper();

  public void start() {
		try( ServerSocket socket = new ServerSocket( PORT ) ) {
			System.out.println( "App rodando em http://localhost:" + PORT );
			while( true ) {
				Socket client = socket.accept();
				handleRequest( client );
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

  private void handleRequest( Socket client ) {
    try {
      InputStream inputStream = client.getInputStream();
      OutputStream outputStream = client.getOutputStream();

      String request = readRequest(inputStream);

      if( request != null && !request.isEmpty() ) {
        String response = processRequest(request);
        
        outputStream.write( response.getBytes() );
        outputStream.flush();
      }

      client.close();  
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  private String processRequest( String request ) {
    String response = null;

    if ( request.contains( "GET" ) ) {
      response = handleGetRequest( request );
    } 
    
    if ( request.contains( "POST" ) || request.contains( "PUT" ) ) {
      response = handlePostRequest( request );
    } 
    
    if ( request.contains( "DELETE" ) ) {
      response = handleDeleteRequest( request );
    } 

    return response;
  }

  private String handleGetRequest( String request ) {
    if ( request.contains( "/paises?id=" ) ) {
      List< Pais > pais = new ArrayList<>();
      pais.add( pdao.getById( extractPaisId( request ) ) );
      return OK_RESPONSE + serializeGenericList( pais );
    }
    if( request.contains( "/paises?estado=" ) || request.contains( "/paises?regiao=" ) ) {
      return OK_RESPONSE + serializeGenericList( pdao.getAll() );
    }
    if( request.contains( "/paises" ) ) {
      return OK_RESPONSE + serializeGenericList( pdao.getAll() );
    }    
    if( request.contains( "/cidades?estado=" ) || request.contains( "/cidades?regiao=" ) ) {
      return OK_RESPONSE + serializeGenericList( filterCidades(request) );
    }   
    if( request.contains( "/cidades" ) ) {
      return OK_RESPONSE + serializeGenericList( cdao.getAll() );
    }
    return BAD_REQUEST_RESPONSE;
  }

  private String handlePostRequest( String request ) {
    if ( request.contains( "/paises" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      persistGenericJson( json, new TypeReference<List<Pais>>() {}, pdao );
      return OK_RESPONSE + "Sucesso.";
    }
    if ( request.contains( "/cidades" ) || request.contains( "/cidades" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      persistGenericJson( json, new TypeReference<List<Cidade>>() {}, cdao );
      return OK_RESPONSE + "Sucesso.";
    }
    return BAD_REQUEST_RESPONSE;
  }

  private String handleDeleteRequest( String request ) {
    if ( request.contains( "/paises" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      deleteGenericJson( json, new TypeReference<List<Pais>>() {}, pdao );
      return OK_RESPONSE + "Sucesso.";
    }
    if ( request.contains( "/cidades" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      deleteGenericJson( json, new TypeReference<List<Cidade>>() {}, cdao );
      return OK_RESPONSE + "Sucesso.";
    } 
    return BAD_REQUEST_RESPONSE;
  }

  private List< Cidade > filterCidades( String request ) {
    if( request.contains( "estado=" ) && request.contains( "regiao=" ) ) {
      int inicioEstado = request.indexOf("estado=") + "estado=".length();
      int fimEstado = request.indexOf("&", inicioEstado);
      String estado = request.substring( inicioEstado, fimEstado );

      int inicioRegiao = request.indexOf("regiao=") + "regiao=".length();
      int fimRegiao = request.indexOf(" ", inicioRegiao);
      String regiao = request.substring( inicioRegiao, fimRegiao );

      return cdao.getAll().stream()
        .filter(cidade -> cidade.getEstado().equalsIgnoreCase(estado) && cidade.getRegiao().equalsIgnoreCase(regiao))
        .collect(Collectors.toList());
    }

    if( request.contains( "GET /cidades?estado=" ) ) {
      int inicio = request.indexOf("/cidades?estado=") + "/cidades?estado=".length();
      int fim = request.indexOf(" ", inicio);
      String estado = request.substring( inicio, fim );

      return cdao.getCidadesByEstado( estado );
    }

    if( request.contains( "GET /cidades?regiao=" ) ) {
      int inicio = request.indexOf("/cidades?regiao=") + "/cidades?regiao=".length();
      int fim = request.indexOf(" ", inicio);
      String regiao = request.substring( inicio, fim );

      return cdao.getCidadesByRegiao( regiao );
    }

    return null;
  }

  private Integer extractPaisId( String request ) {
      int inicio = request.indexOf("/paises?id=") + "/paises?id=".length();
      int fim = request.indexOf(" ", inicio);
      return Integer.parseInt( request.substring( inicio, fim ));
  }

  private String readRequest( InputStream inputStream ) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    byte[] buffer = new byte[4096];
    int bytesRead;
    while( ( bytesRead = inputStream.read( buffer ) ) != -1 ) {
      stringBuilder.append( new String( buffer, 0, bytesRead ) );
      if( stringBuilder.toString().contains( "\r\n\r\n" ) ) {
        break;
      }
    }
    return stringBuilder.toString();
  }

  private < T > String serializeGenericList( List< T > objects ) {
    try {
        return objectMapper.writeValueAsString( objects );
    } catch ( Exception e ) {
        e.printStackTrace();
        return "{}";
    }
  }

  private < T > void persistGenericJson( String json, TypeReference<List<T>> typeReference, GenericDAO< T > dao  ){
    List< T > objects = new ArrayList<>();
    try {
        objects = objectMapper.readValue( json, typeReference );

        objects.forEach( object -> {
              if ( object instanceof Pais ) ( ( Pais ) object ).getCidades().forEach( cidade -> cidade.setPais( ( Pais ) object ) );
              dao.create( object );
          });

    } catch ( Exception e ) {
        System.out.println( "Não foi possível retornar objetos a partir do JSON.");
        e.printStackTrace();
        return;
    }
  }

  private < T > void deleteGenericJson( String json, TypeReference<List<T>> typeReference, GenericDAO< T > dao ){
    List< T > objects = new ArrayList<>();
    try {
        objects = objectMapper.readValue( json, typeReference );

        objects.forEach( object -> {
              dao.delete( object );
          });
    } catch ( Exception e ) {
        System.out.println( "Não foi possível retornar objetos a partir do JSON." );
        e.printStackTrace();
    }
  }
}
