package com.mx.santander.camara.model;

public class BastanteoRequest {
	
    private String cuenta;
    private String monto;
    private String nombre;
    private boolean dosFirmas;
    private String fileName;
	private byte[] file;
    
	public String getCuenta() {
		return cuenta;
	}
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}
	public String getMonto() {
		return monto;
	}
	public void setMonto(String monto) {
		this.monto = monto;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public boolean isDosFirmas() {
		return dosFirmas;
	}
	public void setDosFirmas(boolean dosFirmas) {
		this.dosFirmas = dosFirmas;
	}
	public byte[] getFile() {
		return file;
	}
	public void setFile(byte[] file) {
		this.file = file;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
