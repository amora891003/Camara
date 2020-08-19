package com.mx.santander.camara.model;

public class Cuenta {
	
	private String caso;
    private String cta;
    private String tipo;
    private String titular1;
    private String titular2;
    private String reglaBastanteo;
    private String idRegla;
    
	public String getCaso() {
		return caso;
	}
	public void setCaso(String caso) {
		this.caso = caso;
	}
	public String getCta() {
		return cta;
	}
	public void setCta(String cta) {
		this.cta = cta;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getTitular1() {
		return titular1;
	}
	public void setTitular1(String titular1) {
		this.titular1 = titular1;
	}
	public String getTitular2() {
		return titular2;
	}
	public void setTitular2(String titular2) {
		this.titular2 = titular2;
	}
	public String getReglaBastanteo() {
		return reglaBastanteo;
	}
	public void setReglaBastanteo(String reglaBastanteo) {
		this.reglaBastanteo = reglaBastanteo;
	}
	public String getIdRegla() {
		return idRegla;
	}
	public void setIdRegla(String idRegla) {
		this.idRegla = idRegla;
	}

}
