package com.syonet.domain;

import java.util.List;

import javax.persistence.*;

@Entity
@Table( name = "pais" )
public class Pais {
  @Id
  @GeneratedValue( strategy = GenerationType.IDENTITY )
  private Integer id;

  @Column
  private String nome;

  @OneToMany( mappedBy = "pais", cascade = CascadeType.ALL )
  private List< Cidade > cidades;

  public Pais() {
  }

  public Pais( String nome, List< Cidade > cidades ) {
    this.nome = nome;
    this.cidades = cidades;
  }
  public Integer getId() {
    return id;
  }
  public void setId( Integer id ) {
    this.id = id;
  }
  public String getNome() {
    return nome;
  }
  public void setNome( String nome ) {
    this.nome = nome;
  }
  public List<Cidade> getCidades() {
    return cidades;
  }
  
  public void setCidades( List<Cidade> cidades ) {
    this.cidades = cidades;
  }

}
