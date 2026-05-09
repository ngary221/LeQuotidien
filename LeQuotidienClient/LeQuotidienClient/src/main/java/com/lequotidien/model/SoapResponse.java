package com.lequotidien.model;

/**
 * Encapsule la réponse d'un appel au service SOAP.
 */
public class SoapResponse {

    private boolean succes;
    private String message;
    private String data; // JSON brut retourné par le service

    public SoapResponse() {}

    public SoapResponse(boolean succes, String message, String data) {
        this.succes = succes;
        this.message = message;
        this.data = data;
    }

    public boolean isSucces()     { return succes; }
    public String getMessage()    { return message; }
    public String getData()       { return data; }

    public void setSucces(boolean succes)   { this.succes = succes; }
    public void setMessage(String message)  { this.message = message; }
    public void setData(String data)        { this.data = data; }
}
