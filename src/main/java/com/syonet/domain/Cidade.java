package com.syonet.domain;

import javax.persistence.*;

@Entity
@Table( name = "cidade" )
public class Cidade {
  @Id
  @GeneratedValue( strategy = GenerationType.IDENTITY )
  private Integer id;

  @Column
  private String nome;

  @Column
  private String estado;

  @Column
  private String regiao;

  @ManyToOne( cascade = CascadeType.ALL )
  @JoinColumn( name = "id_pais" )
  private Pais pais;

  public Cidade() {
  }

  public Cidade( String nome, String estado, String regiao, Pais pais ) {
    this.nome = nome;
    this.estado = estado;
    this.regiao = regiao;
    this.pais = pais;
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

  public String getEstado() {
    return estado;
  }

  public void setEstado( String estado ) {
    this.estado = estado;
  }

  public String getRegiao() {
    return regiao;
  }

  public void setRegiao( String regiao ) {
    this.regiao = regiao;
  }

  public void setPais(Pais pais) {
    this.pais = pais;
  }
  
}
