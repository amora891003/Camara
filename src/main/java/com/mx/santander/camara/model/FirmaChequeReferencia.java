package com.mx.santander.camara.model;

public class FirmaChequeReferencia {
	private String cuenta;
	private String titular;
	private String titular2;
	private String firmaReferenciaNombre;
	private byte[] firmaReferenciaBytes;
	private boolean esCheque;


	public byte[] getFirmaReferenciaBytes() {
		return firmaReferenciaBytes;
	}

	public void setFirmaReferenciaBytes(byte[] firmaReferencia) {
		this.firmaReferenciaBytes = firmaReferencia;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public String getTitular() {
		return titular;
	}

	public void setTitular(String titular) {
		this.titular = titular;
	}

	public String getTitular2() {
		return titular2;
	}

	public void setTitular2(String titular2) {
		this.titular2 = titular2;
	}

	public String getFirmaReferenciaNombre() {
		return firmaReferenciaNombre;
	}

	public void setFirmaReferenciaNombre(String firmaReferenciaNombre) {
		this.firmaReferenciaNombre = firmaReferenciaNombre;
	}

	public boolean isEsCheque() {
		return esCheque;
	}

	public void setEsCheque(boolean esCheque) {
		this.esCheque = esCheque;
	}
}