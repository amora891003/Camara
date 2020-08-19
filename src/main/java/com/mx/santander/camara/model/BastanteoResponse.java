package com.mx.santander.camara.model;

import java.util.List;

public class BastanteoResponse {
	
	private String reglaBastanteo;
	private String resultado;
	private List<String> regla;
	
	public String getReglaBastanteo() {
		return reglaBastanteo;
	}
	public void setReglaBastanteo(String reglaBastanteo) {
		this.reglaBastanteo = reglaBastanteo;
	}
	public String getResultado() {
		return resultado;
	}
	public void setResultado(String resultado) {
		this.resultado = resultado;
	}
	public List<String> getRegla() {
		return regla;
	}
	public void setRegla(List<String> regla) {
		this.regla = regla;
	}

}
