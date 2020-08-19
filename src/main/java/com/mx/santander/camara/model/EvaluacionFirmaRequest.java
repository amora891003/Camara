package com.mx.santander.camara.model;

import java.util.List;

public class EvaluacionFirmaRequest {
	
	private ImagenFirmaCheque signature;
	private List<ImagenFirmaCheque> reference;
	
	public ImagenFirmaCheque getSignature() {
		return signature;
	}
	public void setSignature(ImagenFirmaCheque signature) {
		this.signature = signature;
	}
	public List<ImagenFirmaCheque> getReference() {
		return reference;
	}
	public void setReference(List<ImagenFirmaCheque> reference) {
		this.reference = reference;
	}
	

}
