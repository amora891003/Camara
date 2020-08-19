package com.mx.santander.camara.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.mx.santander.camara.model.*;

@RestController
public class RestServices {
    String base_path = "C:\\santander\\camara\\referencias";
    String base_path_cheque = "C:\\santander\\camara\\cheques";
	private static final String SIGN_APP = "com.mx.bizpro.santander.camara.parascript.app";
	private static final String TEST_INI = "com.mx.bizpro.santander.camara.parascript.testini";	
	@Autowired
	RestTemplate restTemplate;	
	@Autowired
	private Environment env;

    @RequestMapping("/obtenerCuentas")
    public String obtenerCuentas() {
    	StringBuilder stringBuilder = new StringBuilder();
    	String line = null;    
		File fileDir = new File(base_path + File.separator +"cuentas.json");   	
    	try (BufferedReader bufferedReader  = new BufferedReader(
				new InputStreamReader(
                        new FileInputStream(fileDir), "UTF8"))) {			
    		while ((line = bufferedReader.readLine()) != null) {
    			stringBuilder.append(line);
    		}
	        return stringBuilder.toString();
		
    	} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }
    
	@PutMapping(value="/validarCheckstock")
	public EvaluacionFirmaResponse validarCheckstock(@RequestBody FirmaChequeReferencia cheque) {
		EvaluacionFirmaResponse evaluacionFirmaResponse = new EvaluacionFirmaResponse();
    	evaluacionFirmaResponse.setPorcentaje("ERROR");    	

		try {
			//mover archivo de entrada a ruta de cheques como p2.tif
			FileUtils.writeByteArrayToFile(new File(base_path_cheque +  File.separator + "p2.tif"), 
					cheque.getFirmaReferenciaBytes());			
			
			//mover referencia a ruta de cheques
			File directorioCuenta = new File(base_path_cheque + File.separator + cheque.getCuenta());
			ArrayList<File> directoriosTitulares = new ArrayList<File>(Arrays.asList(directorioCuenta.listFiles()));				
			for (File dir:directoriosTitulares) {
				if (dir.getName().startsWith(cheque.getTitular().replaceAll("[^a-zA-Z0-9]+",""))) {
					File directorioTitular = new File(directorioCuenta + File.separator + dir.getName());
					ArrayList<File> referenciasTitular = new ArrayList<File>(Arrays.asList(directorioTitular.listFiles()));
					for (File ref:referenciasTitular) {		
						if (!ref.getName().contains(".db")) {
							System.out.println("ref " + directorioTitular + File.separator + ref.getName());
							File f = new File(directorioTitular + File.separator + ref.getName());		
							byte[] data = FileUtils.readFileToByteArray(f);
							FileUtils.writeByteArrayToFile(new File(base_path_cheque +  File.separator + "p1.tif"), data);
							break;
						}
					}
					break;
				}
			}
			
		    ProcessBuilder processBuilder = new ProcessBuilder();
		    processBuilder.command(
		    		env.getProperty(SIGN_APP), //Aplicación a ejecutar
		    		env.getProperty(TEST_INI)); //Archivo de inicialización
		    Process process;
			process = processBuilder.start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				if (line.contains("1.cas")) {
					System.out.println(line);
					String[] salida = new String[4];
					salida = line.split(",");
					evaluacionFirmaResponse.setPorcentaje(salida[2]);
					evaluacionFirmaResponse.setEvaluacionCheque(salida[1]);
				}
			}			
		    int exitVal = process.waitFor();
		    if (exitVal != 0) {
		        System.out.println("ERROR");
		    }	
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return evaluacionFirmaResponse;
    }
    
    @PutMapping(value = "/agregarRefrenciasATitulares")
    public @ResponseBody BastanteoResponse agregarRefrenciasATitulares(@RequestBody List<FirmaChequeReferencia> nuevasReferencias) {
    	BastanteoResponse bastanteoResponse = new BastanteoResponse();
    	bastanteoResponse.setResultado("NOK");
    	for (FirmaChequeReferencia firmaChequeReferencia: nuevasReferencias) {
            String docPath = base_path + File.separator + firmaChequeReferencia.getCuenta() + File.separator + 
            		firmaChequeReferencia.getTitular().replaceAll("[^a-zA-Z0-9]+","");
            try {
                FileUtils.writeByteArrayToFile(new File(docPath + File.separator + firmaChequeReferencia.getFirmaReferenciaNombre()), 
                		firmaChequeReferencia.getFirmaReferenciaBytes());
                bastanteoResponse.setResultado("OK");
            } catch (IOException e) {
                e.printStackTrace();
            }    		
    	}
        return bastanteoResponse;
    }
    
    @PutMapping(value = "/validarFirmaCheque")
    public @ResponseBody EvaluacionFirmaResponse validarFirmaCheque(@RequestBody FirmaChequeReferencia firma) {  
    	EvaluacionFirmaResponse evaluacionFirmaResponse = new EvaluacionFirmaResponse();
    	evaluacionFirmaResponse.setPorcentaje("ERROR");
		Cuenta cuenta = new Cuenta();
    	StringBuilder stringBuilder = new StringBuilder();
    	String line = null; 
    	String otroTitular = null;
		try {
			File fileDir = new File(base_path + File.separator +"cuentas.json");  
    		try (BufferedReader bufferedReader  = new BufferedReader(
				new InputStreamReader(
                        new FileInputStream(fileDir), "UTF8"))) {	
	    		while ((line = bufferedReader.readLine()) != null) {
	    			stringBuilder.append(line);
	    		}
    		}          
            JSONObject obj = new JSONObject(stringBuilder.toString());
			JSONArray cuentas = obj.getJSONArray("cuentas");
			for (int i = 0; i < cuentas.length(); i++) {
				if (cuentas.getJSONObject(i).getString("cta").equals(firma.getCuenta())) {
					cuenta.setCta(cuentas.getJSONObject(i).getString("cta"));
					cuenta.setTitular1(cuentas.getJSONObject(i).getString("titular1"));
					cuenta.setTitular2(cuentas.getJSONObject(i).getString("titular2"));
					cuenta.setReglaBastanteo(cuentas.getJSONObject(i).getString("reglaBastanteo"));
					cuenta.setIdRegla(cuentas.getJSONObject(i).getString("idRegla"));
					break;
				}
		    }
			
			if (cuenta.getCta() != null) {
				EvaluacionFirmaRequest evaluacionFirmaRequest = new EvaluacionFirmaRequest();
				List<ImagenFirmaCheque> references = new ArrayList<>();				
				File directorioCuenta = new File(base_path + File.separator + cuenta.getCta());
				ArrayList<File> directoriosTitulares = new ArrayList<File>(Arrays.asList(directorioCuenta.listFiles()));				
				for (File dir:directoriosTitulares) {
					if (dir.getName().startsWith(firma.getTitular().replaceAll("[^a-zA-Z0-9]+",""))) {
						File directorioTitular = new File(directorioCuenta + File.separator + dir.getName());
						ArrayList<File> referenciasTitular = new ArrayList<File>(Arrays.asList(directorioTitular.listFiles()));
						for (File ref:referenciasTitular) {		
							if (!ref.getName().contains(".db")) {
								System.out.println("ref " + directorioTitular + File.separator + ref.getName());
								ImagenFirmaCheque reference = new ImagenFirmaCheque();
								reference.setName(ref.getName());
								File f = new File(directorioTitular + File.separator + ref.getName());		
								reference.setData(FileUtils.readFileToByteArray(f));
								reference.setIscheck(false);
								references.add(reference);
							}
						}
					} else {
						otroTitular = dir.getName();
					}
				}
				evaluacionFirmaRequest.setReference(references);

				ImagenFirmaCheque signature = new ImagenFirmaCheque();
				signature.setName(firma.getFirmaReferenciaNombre());
				signature.setData(firma.getFirmaReferenciaBytes());
				signature.setIscheck(firma.isEsCheque());
				evaluacionFirmaRequest.setSignature(signature);
				
				String response = restTemplate.postForObject("http://172.16.0.77/TestRest_deploy/api/rest/", 
						evaluacionFirmaRequest, String.class);				      
	            obj = new JSONObject(response);
	            System.out.println("NumberOfFoundSignatures "+obj.getString("NumberOfFoundSignatures"));
	            if (!obj.getString("NumberOfFoundSignatures").equals("0")) {
	            	if (obj.getString("NumberOfFoundSignatures").equals("1")) {
	            		JSONArray porcentaje = obj.getJSONArray("Answer");
						evaluacionFirmaResponse.setPorcentaje(porcentaje.getString(1).split(",")[1]);
						evaluacionFirmaResponse.setNumFirmas(obj.getString("NumberOfFoundSignatures"));
	            	} else {
	            		System.out.println("otro titular dir " + directorioCuenta + File.separator + otroTitular);
						File directorioTitular = new File(directorioCuenta + File.separator + otroTitular);
						ArrayList<File> referenciasTitular = new ArrayList<File>(Arrays.asList(directorioTitular.listFiles()));
						for (File ref:referenciasTitular) {		
							if (!ref.getName().contains(".db")) {
								System.out.println("ref " + directorioTitular + File.separator + ref.getName());
								ImagenFirmaCheque reference = new ImagenFirmaCheque();
								reference.setName(ref.getName());
								File f = new File(directorioTitular + File.separator + ref.getName());		
								reference.setData(FileUtils.readFileToByteArray(f));
								reference.setIscheck(false);
								references.add(reference);
							}
						}
						evaluacionFirmaRequest.setReference(references);
						String response2 = restTemplate.postForObject("http://172.16.0.77/TestRest_deploy/api/rest/", 
								evaluacionFirmaRequest, String.class);				      
			            obj = new JSONObject(response2);
			            System.out.println("NumberOfFoundSignatures2 "+obj.getString("NumberOfFoundSignatures"));
			            if (!obj.getString("NumberOfFoundSignatures").equals("0")) {
			            	JSONArray porcentaje = obj.getJSONArray("Answer");
							evaluacionFirmaResponse.setPorcentaje(porcentaje.getString(1).split(",")[1]);
							evaluacionFirmaResponse.setNumFirmas(obj.getString("NumberOfFoundSignatures"));
			            }
	            	}
				}
			}			
        } catch (JSONException | IOException e) {
			e.printStackTrace();
		}
        return evaluacionFirmaResponse;
    }
    
    @PutMapping(value = "/aplicarReglasDeBastanteo")
    public @ResponseBody BastanteoResponse aplicarReglasDeBastanteo(@RequestBody BastanteoRequest bastanteo) {
    	BastanteoResponse bastanteoResponse = new BastanteoResponse();
		bastanteoResponse.setResultado("ERROR");
		Cuenta cuenta = new Cuenta();    	
    	StringBuilder stringBuilder = new StringBuilder();
    	String line = null;
    	
        try {      	
    		File fileDir = new File(base_path + File.separator +"cuentas.json");   	
        	try (BufferedReader bufferedReader  = new BufferedReader(
    				new InputStreamReader(
                            new FileInputStream(fileDir), "UTF8"))) {	
	    		while ((line = bufferedReader.readLine()) != null) {
	    			stringBuilder.append(line);
	    		}
    		}
    		JSONObject obj = new JSONObject(stringBuilder.toString());
			JSONArray cuentas = obj.getJSONArray("cuentas");
			for (int i = 0; i < cuentas.length(); i++) {
				if (cuentas.getJSONObject(i).getString("cta").equals(bastanteo.getCuenta())) {
					cuenta.setCta(cuentas.getJSONObject(i).getString("cta"));
					cuenta.setTitular1(cuentas.getJSONObject(i).getString("titular1"));
					cuenta.setTitular2(cuentas.getJSONObject(i).getString("titular2"));
					cuenta.setReglaBastanteo(cuentas.getJSONObject(i).getString("reglaBastanteo"));
					cuenta.setIdRegla(cuentas.getJSONObject(i).getString("idRegla"));
					break;
				}
		    }
			
			if (cuenta.getCta() != null) {		
				if (cuenta.getIdRegla().equals("N/A")) {
					bastanteoResponse.setReglaBastanteo("Esta cuenta no tiene una regla de bastanteo asociada.");
					bastanteoResponse.setResultado("OK");
				} else {
					List<BastanteoReglas> reglasDeBastanteo = new ArrayList<>();
					HttpHeaders headers = new HttpHeaders();
					headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJFeHBlcnRTeXN0ZW1zIiwidXNlcklkIjoyLCJ1c2VyTmFtZSI6ImJpenBybyIsInNlc3Npb25JZCI6ImVjNjU4M2M5LTM2ODYtNDhkMS1hZTNlLTljNDE3OWVmOTZiZiIsImlhdCI6MTU2ODcwNDU0OX0.awlWNwOelPF1gV82-f6mati9JkhErw4xDWFI3zvHRl0");
					HttpEntity<String> entity = new HttpEntity<>("body", headers);
					String response = restTemplate.exchange("http://answers.expertsystemlab.com/answers-ws-bizpro/v1/management/entities/documents/" 
			    			+ cuenta.getIdRegla() + "/annotations", HttpMethod.GET, entity, String.class).getBody();			
					if (response != null && !response.equals("null")) {
						JSONArray types = new JSONArray(response);
						for (int i=0; i < types.length(); i++){
							if (types.getJSONObject(i).getString("type").equals("/Template/BASTANTEO")) {
								JSONArray fields = types.getJSONObject(i).getJSONArray("fields");
								BastanteoReglas regla = new BastanteoReglas();
								for (int j=0; j < fields.length(); j++){	
									if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@TITULAR_1")) {
					   	    			regla.setTitular1(fields.getJSONObject(j).getString("value"));
					   	    	   }
						   	    	if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@TITULAR_2")) {
						   	    		regla.setTitular2(fields.getJSONObject(j).getString("value"));
					   	    	   }
						   	    	if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@IMPORTE_LIMITE")) {
						   	    		regla.setMonto(fields.getJSONObject(j).getString("value").replaceAll(",",""));
					   	    	   }
						   	    	if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@TIPO_LIMITE")) {
						   	    		regla.setOperacion(fields.getJSONObject(j).getString("value"));
					   	    	   }
								}
								reglasDeBastanteo.add(regla);
							}
						}
						bastanteoResponse.setReglaBastanteo(cuenta.getReglaBastanteo());
						bastanteoResponse.setResultado("NOK");
						bastanteoResponse.setRegla(new ArrayList<>());
						for (BastanteoReglas reg:reglasDeBastanteo) {
							if (reg.getTitular2() == null) {
								bastanteoResponse.getRegla().add(reg.getTitular1() + " " + reg.getOperacion() + " " + reg.getMonto());
							} else if (reg.getTitular1() == null) {
								bastanteoResponse.getRegla().add(reg.getTitular2() + " " + reg.getOperacion() + " " + reg.getMonto());
							} else {
								bastanteoResponse.getRegla().add(reg.getTitular1() + " y " + reg.getTitular2() + " " +reg.getOperacion() + " " + reg.getMonto());						
							}
							if ((!bastanteo.isDosFirmas() && reg.getTitular2() == null && reg.getTitular1().replaceAll("[^a-zA-Z0-9]+","").startsWith(bastanteo.getNombre().replaceAll("[^a-zA-Z0-9]+",""))) ||
								(!bastanteo.isDosFirmas() && reg.getTitular1() == null && reg.getTitular2().replaceAll("[^a-zA-Z0-9]+","").startsWith(bastanteo.getNombre().replaceAll("[^a-zA-Z0-9]+",""))) ||
								(!bastanteo.isDosFirmas() && reg.getTitular1() != null && !reg.getTitular1().replaceAll("[^a-zA-Z0-9]+","").startsWith(cuenta.getTitular1().replaceAll("[^a-zA-Z0-9]+","")) && 
									!reg.getTitular1().replaceAll("[^a-zA-Z0-9]+","").contains("DOS")) ||
								(bastanteo.isDosFirmas())) {
								if (reg.getOperacion().equals("<=") && (Double.parseDouble(bastanteo.getMonto()) <= Double.parseDouble(reg.getMonto())) ||
										reg.getOperacion().equals(">=") && (Double.parseDouble(bastanteo.getMonto()) >= Double.parseDouble(reg.getMonto())) ||
										reg.getOperacion().equals("<") && (Double.parseDouble(bastanteo.getMonto()) < Double.parseDouble(reg.getMonto())) ||
										reg.getOperacion().equals(">") && (Double.parseDouble(bastanteo.getMonto()) > Double.parseDouble(reg.getMonto()))) {
									bastanteoResponse.setResultado("OK");
								}
							}
						}
					}
				}
			}
        } catch (JSONException | IOException e) {
			e.printStackTrace();
		}
        return bastanteoResponse;
    }
    
    @PutMapping(value = "/actualizarReglaDeBastanteo")
    public @ResponseBody BastanteoResponse actualizarReglaDeBastanteo(@RequestBody BastanteoRequest bastanteo) {
    	BastanteoResponse bastanteoResponse = new BastanteoResponse();
		bastanteoResponse.setResultado("NOK");
    	StringBuilder stringBuilder = new StringBuilder();
    	StringBuilder stringBuilder2 = new StringBuilder();
    	String line = null;
    	String nuevaReglaTexto = null;
    	String response = "N/A";
    	
        try {  
        	FileUtils.writeByteArrayToFile(new File(base_path + File.separator + bastanteo.getFileName()), 
            		bastanteo.getFile());
            File archivo = new File(base_path + File.separator + bastanteo.getFileName());     
            
        	File fileDir1 = new File(base_path + File.separator + bastanteo.getFileName());  
    		try (BufferedReader bufferedReader1  = new BufferedReader(
				new InputStreamReader(
                        new FileInputStream(fileDir1), "UTF8"))) {	
	    		while ((line = bufferedReader1.readLine()) != null) {
	    			stringBuilder.append(line);
	    		}
	    		nuevaReglaTexto = stringBuilder.toString();
    		}
    		
    		if (!nuevaReglaTexto.equals("N/A")) {    			
                HttpHeaders headers = new HttpHeaders();
        		headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJFeHBlcnRTeXN0ZW1zIiwidXNlcklkIjoyLCJ1c2VyTmFtZSI6ImJpenBybyIsInNlc3Npb25JZCI6ImVjNjU4M2M5LTM2ODYtNDhkMS1hZTNlLTljNDE3OWVmOTZiZiIsImlhdCI6MTU2ODcwNDU0OX0.awlWNwOelPF1gV82-f6mati9JkhErw4xDWFI3zvHRl0");	
            	headers.setContentType(MediaType.MULTIPART_FORM_DATA);    	
            	MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            	body.add("file", new FileSystemResource(archivo));
            	body.add("name", bastanteo.getFileName());
            	
            	HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            	response = restTemplate.postForEntity("http://answers.expertsystemlab.com/answers-ws-bizpro/v1/management/entities/sources/1/documents/upload", 
            			requestEntity, String.class).getBody();		
    		}
        	archivo.delete();      
    		
			File fileDir2 = new File(base_path + File.separator +"cuentas.json");  
    		try (BufferedReader bufferedReader2  = new BufferedReader(
				new InputStreamReader(
                        new FileInputStream(fileDir2), "UTF8"))) {	
	    		while ((line = bufferedReader2.readLine()) != null) {
	    			stringBuilder2.append(line);
	    		}
    		}
    		JSONObject obj = new JSONObject(stringBuilder2.toString());
			JSONArray cuentas = obj.getJSONArray("cuentas");
			JSONArray cuentasNuevas = new JSONArray();		
			for (int i = 0; i < cuentas.length(); i++) {
				JSONObject cuenta = new JSONObject();
				cuenta.put("tipo", cuentas.getJSONObject(i).getString("tipo"));
				cuenta.put("cta", cuentas.getJSONObject(i).getString("cta"));
				cuenta.put("titular1", cuentas.getJSONObject(i).getString("titular1"));
				cuenta.put("titular2", cuentas.getJSONObject(i).getString("titular2"));
				if (cuentas.getJSONObject(i).getString("cta").equals(bastanteo.getCuenta())) {
					cuenta.put("idRegla", response);
					cuenta.put("reglaBastanteo", nuevaReglaTexto);
				} else {
					cuenta.put("idRegla", cuentas.getJSONObject(i).getString("idRegla"));
					cuenta.put("reglaBastanteo", cuentas.getJSONObject(i).getString("reglaBastanteo"));
				}
				cuentasNuevas.put(cuenta);
				
		    }	
			JSONObject objetoCuentas = new JSONObject();
			objetoCuentas.put("cuentas", cuentasNuevas);			
		    try (FileWriter fileWriter = new FileWriter(base_path + File.separator +"cuentas.json")){
		    	fileWriter.write(objetoCuentas.toString());	
				bastanteoResponse.setReglaBastanteo(nuevaReglaTexto);	
				bastanteoResponse.setResultado("OK");	
			}
            
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }  
        return bastanteoResponse;
    }
    
    @PutMapping(value="/fileToBytes")
    public @ResponseBody BastanteoRequest fileToBytes(@RequestBody BastanteoRequest bastanteo) {
        File f = new File(bastanteo.getFileName());
        try {
        	bastanteo.setFile(FileUtils.readFileToByteArray(f));
            return bastanteo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /*@PutMapping(value = "/aplicarSoloReglasDeBastanteo")
    public @ResponseBody BastanteoResponse aplicarSoloReglasDeBastanteo(@RequestBody BastanteoRequest bastanteo) {
    	BastanteoResponse bastanteoResponse = new BastanteoResponse();
		bastanteoResponse.setResultado("NOK");
    	
        try {
			List<BastanteoReglas> reglasDeBastanteo = new ArrayList<>();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJFeHBlcnRTeXN0ZW1zIiwidXNlcklkIjoyLCJ1c2VyTmFtZSI6ImJpenBybyIsInNlc3Npb25JZCI6ImVjNjU4M2M5LTM2ODYtNDhkMS1hZTNlLTljNDE3OWVmOTZiZiIsImlhdCI6MTU2ODcwNDU0OX0.awlWNwOelPF1gV82-f6mati9JkhErw4xDWFI3zvHRl0");
			HttpEntity<String> entity = new HttpEntity<>("body", headers);
			String response = restTemplate.exchange("http://answers.expertsystemlab.com/answers-ws-bizpro/v1/management/entities/documents/" 
	    			+ bastanteo.getFileName() + "/annotations", HttpMethod.GET, entity, String.class).getBody();
			if (response != null && !response.equals("null")) {
				JSONArray types = new JSONArray(response);
				for (int i=0; i < types.length(); i++){
					if (types.getJSONObject(i).getString("type").equals("/Template/BASTANTEO")) {
						JSONArray fields = types.getJSONObject(i).getJSONArray("fields");
						BastanteoReglas regla = new BastanteoReglas();
						for (int j=0; j < fields.length(); j++){	
							if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@TITULAR_1")) {
			   	    			regla.setTitular1(fields.getJSONObject(j).getString("value"));
			   	    	   }
				   	    	if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@TITULAR_2")) {
				   	    		regla.setTitular2(fields.getJSONObject(j).getString("value"));
			   	    	   }
				   	    	if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@IMPORTE_LIMITE")) {
				   	    		regla.setMonto(fields.getJSONObject(j).getString("value").replaceAll(",",""));
			   	    	   }
				   	    	if (fields.getJSONObject(j).getString("type").equals("/Field/BASTANTEO@TIPO_LIMITE")) {
				   	    		regla.setOperacion(fields.getJSONObject(j).getString("value"));
			   	    	   }
						}
						reglasDeBastanteo.add(regla);
					}
				}
				bastanteoResponse.setReglaBastanteo(bastanteo.getCuenta());
				bastanteoResponse.setRegla(new ArrayList<>());
				for (BastanteoReglas reg:reglasDeBastanteo) {
					if (reg.getTitular2() == null) {
						bastanteoResponse.getRegla().add(reg.getTitular1() + " " + reg.getOperacion() + " " + reg.getMonto());
					} else {
						bastanteoResponse.getRegla().add(reg.getTitular1() + " y " + reg.getTitular2() + " " +reg.getOperacion() + " " + reg.getMonto());						
					}
				}
				bastanteoResponse.setResultado("OK");
			}
        } catch (JSONException e) {
			e.printStackTrace();
		}
        return bastanteoResponse;
    }*/
    
}
