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
      String response = processRequest(request);

      outputStream.write( response.getBytes() );
			outputStream.flush();
			client.close();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  private String processRequest( String request ) {
    String response;

    if ( request.contains( "GET /paises?id=" ) ) {
      List< Pais > pais = new ArrayList<>();
      pais.add( pdao.getById( extractPaisId( request ) ) );
      response = OK_RESPONSE + serializeGenericList( pais );

    } else if( request.contains( "GET /paises?estado=" ) || request.contains( "GET /paises?regiao=" ) ) {
      response = OK_RESPONSE + serializeGenericList( pdao.getAll() );

    } else if( request.contains( "GET /paises" ) ) {
      response = OK_RESPONSE + serializeGenericList( pdao.getAll() );

    } else if ( request.contains( "POST /paises" ) || request.contains( "PUT /paises" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      persistPaisJson( json );
      response = OK_RESPONSE + "Sucesso.";

    } else if ( request.contains( "DELETE /paises" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      deleteGenericJson( json, new TypeReference<List<Pais>>() {}, pdao );
      response = OK_RESPONSE + "Sucesso.";

    } else if( request.contains( "GET /cidades?estado=" ) || request.contains( "GET /cidades?regiao=" ) ) {
      response = OK_RESPONSE + serializeGenericList( filterCidades(request) );

    } else if( request.contains( "GET /cidades" ) ) {
      response = OK_RESPONSE + serializeGenericList( cdao.getAll() );

    } else if ( request.contains( "POST /cidades" ) || request.contains( "PUT /cidades" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      persistCidadesJson( json );
      response = OK_RESPONSE + "Sucesso.";

    } else if ( request.contains( "DELETE /cidades" ) ) {
      String json = request.substring( request.indexOf( "[" ) );
      deleteGenericJson( json, new TypeReference<List<Cidade>>() {}, cdao );
      response = OK_RESPONSE + "Sucesso.";

    } else {
      response = BAD_REQUEST_RESPONSE;
    }
    return response;

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

  private void persistPaisJson( String json ){
    List< Pais > paises = new ArrayList<>();
    try {
        paises = objectMapper.readValue( json, new TypeReference<List<Pais>>() {} );

        paises.forEach( pais -> {
              pais.getCidades().forEach( cidade -> cidade.setPais( pais )  );
              pdao.create( pais );
          });
    } catch ( Exception e ) {
        System.out.println( "Não foi possível retornar objetos a partir do JSON.");
        e.printStackTrace();
        return;
    }
  }

  private void persistCidadesJson( String json ){
    List< Cidade > cidades = new ArrayList<>();
    try {
        cidades = objectMapper.readValue( json, new TypeReference<List<Cidade>>() {} );

        cidades.forEach( cidade -> {
              cdao.create( cidade );
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
