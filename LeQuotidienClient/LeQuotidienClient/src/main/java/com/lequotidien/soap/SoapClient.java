package com.lequotidien.soap;

import com.lequotidien.model.SoapResponse;
import com.lequotidien.model.Utilisateur;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client SOAP pour le service Le•Quotidien.
 * Construit et envoie les enveloppes SOAP manuellement (sans génération de stub),
 * ce qui évite toute dépendance externe au-delà du JDK.
 */
public class SoapClient {

    private static final Logger LOGGER = Logger.getLogger(SoapClient.class.getName());

    private static final String SOAP_NS    = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String SOAP_ENC   = "http://schemas.xmlsoap.org/soap/encoding/";
    private static final String TARGET_NS  = "http://lequotidien.local/soap/users";  // namespace exact du WSDL Laravel
    private static final String CONTENT_TYPE = "text/xml; charset=utf-8";

    private final String endpointUrl;

    public SoapClient(String serverBase) {
        // Normalise l'URL : supprime le slash final s'il existe
        String base = serverBase.endsWith("/") ? serverBase.substring(0, serverBase.length() - 1) : serverBase;
        this.endpointUrl = base + "/soap";
    }

    // -------------------------------------------------------------------------
    // Méthodes publiques
    // -------------------------------------------------------------------------

    /**
     * Authentifie un utilisateur.
     * Ne requiert pas de token.
     */
    public SoapResponse authentifierUtilisateur(String email, String password) {
        String body = buildMethodBody("authentifierUtilisateur",
                param("email", escape(email)),
                param("password", escape(password)));
        return call("authentifierUtilisateur", body);
    }

    /**
     * Liste tous les utilisateurs.
     */
    public List<Utilisateur> listerUtilisateurs(String token) throws Exception {
        String body = buildMethodBody("listerUtilisateurs", param("token", escape(token)));
        SoapResponse response = call("listerUtilisateurs", body);
        if (!response.isSucces()) {
            throw new Exception(response.getMessage());
        }
        return parseUtilisateurs(response.getData());
    }

    /**
     * Ajoute un nouvel utilisateur.
     */
    public SoapResponse ajouterUtilisateur(String token, String nom, String email,
                                           String password, String role) {
        String body = buildMethodBody("ajouterUtilisateur",
                param("token",    escape(token)),
                param("nom",      escape(nom)),
                param("email",    escape(email)),
                param("password", escape(password)),
                param("role",     escape(role)));
        return call("ajouterUtilisateur", body);
    }

    /**
     * Modifie un utilisateur existant.
     */
    public SoapResponse modifierUtilisateur(String token, int id, String nom,
                                            String email, String role) {
        String body = buildMethodBody("modifierUtilisateur",
                param("token", escape(token)),
                param("id",    String.valueOf(id)),
                param("nom",   escape(nom)),
                param("email", escape(email)),
                param("role",  escape(role)));
        return call("modifierUtilisateur", body);
    }

    /**
     * Supprime un utilisateur.
     */
    public SoapResponse supprimerUtilisateur(String token, int id) {
        String body = buildMethodBody("supprimerUtilisateur",
                param("token", escape(token)),
                param("id",    String.valueOf(id)));
        return call("supprimerUtilisateur", body);
    }

    // -------------------------------------------------------------------------
    // Construction des enveloppes SOAP
    // -------------------------------------------------------------------------

    private String buildEnvelope(String bodyContent) {
        // Style rpc/encoded : namespace + encodingStyle obligatoires sur le body
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<soapenv:Envelope" +
                " xmlns:soapenv=\"" + SOAP_NS + "\"" +
                " xmlns:tns=\""     + TARGET_NS + "\"" +
                " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                bodyContent +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";
    }

    private String buildMethodBody(String method, String... params) {
        StringBuilder sb = new StringBuilder();
        // rpc/encoded : l'élément racine porte le namespace et l'encodingStyle
        sb.append("<tns:").append(method)
          .append(" xmlns:tns=\"").append(TARGET_NS).append("\"")
          .append(" soapenv:encodingStyle=\"").append(SOAP_ENC).append("\"")
          .append(">");
        for (String p : params) sb.append(p);
        sb.append("</tns:").append(method).append(">");
        return buildEnvelope(sb.toString());
    }

    private String param(String name, String value) {
        return "<" + name + ">" + value + "</" + name + ">";
    }

    /** Échappe les caractères XML spéciaux dans les valeurs. */
    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }

    // -------------------------------------------------------------------------
    // Appel HTTP
    // -------------------------------------------------------------------------

    private SoapResponse call(String action, String envelope) {
        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE);
            conn.setRequestProperty("SOAPAction", "\"" + action + "\"");

            byte[] bytes = envelope.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            int status = conn.getResponseCode();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    status >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                    StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            String xml = sb.toString();
            // Log toujours visible dans le terminal pour faciliter le débogage
            System.out.println("[SOAP " + action + "] status=" + status);
            System.out.println("[SOAP " + action + "] response=" + xml);
            LOGGER.fine("SOAP response [" + action + "]: " + xml);
            return parseResponse(xml);

        } catch (Exception e) {
            LOGGER.severe("Erreur SOAP [" + action + "]: " + e.getMessage());
            return new SoapResponse(false, "Erreur de connexion : " + e.getMessage(), null);
        }
    }

    // -------------------------------------------------------------------------
    // Parsing des réponses XML
    // -------------------------------------------------------------------------

    /**
     * Parse la réponse SOAP et en extrait succes, message et data.
     * Utilise des regex légères plutôt qu'un DOM pour éviter les dépendances.
     */
    private SoapResponse parseResponse(String xml) {
        boolean succes  = extractBool(xml, "succes");
        String message  = extractTag(xml, "message");
        String data     = extractTag(xml, "data");

        // Certains serveurs encodent le JSON en entités HTML dans la réponse XML
        if (data != null) {
            data = data.replace("&quot;", "\"")
                       .replace("&amp;",  "&")
                       .replace("&lt;",   "<")
                       .replace("&gt;",   ">")
                       .replace("&#039;", "'");
        }

        return new SoapResponse(succes, message, data);
    }

    private boolean extractBool(String xml, String tag) {
        String val = extractTag(xml, tag);
        return "true".equalsIgnoreCase(val) || "1".equals(val);
    }

    private String extractTag(String xml, String tag) {
        // Accepte <tag>, <ns:tag>, <tns:tag>, etc.
        Pattern p = Pattern.compile("<(?:[a-zA-Z0-9_]+:)?" + tag + ">(.*?)</(?:[a-zA-Z0-9_]+:)?" + tag + ">",
                Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        return m.find() ? m.group(1).trim() : null;
    }

    /**
     * Parse le JSON array d'utilisateurs retourné dans le champ <data>.
     * Parser JSON minimal sans bibliothèque externe.
     */
    private List<Utilisateur> parseUtilisateurs(String json) {
        List<Utilisateur> list = new ArrayList<>();
        if (json == null || json.isBlank()) return list;

        // Découpe chaque objet JSON { ... }
        Pattern objPattern = Pattern.compile("\\{([^}]+)}", Pattern.DOTALL);
        Matcher objMatcher = objPattern.matcher(json);

        while (objMatcher.find()) {
            String obj = objMatcher.group(1);
            Utilisateur u = new Utilisateur();
            u.setId(    Integer.parseInt(extractJsonValue(obj, "id",    "0")));
            u.setNom(   extractJsonValue(obj, "nom",   ""));
            u.setEmail( extractJsonValue(obj, "email", ""));
            u.setRole(  extractJsonValue(obj, "role",  ""));
            u.setActif( "1".equals(extractJsonValue(obj, "actif", "0"))
                     || "true".equalsIgnoreCase(extractJsonValue(obj, "actif", "false")));
            list.add(u);
        }
        return list;
    }

    /** Extrait une valeur depuis un fragment JSON plat (sans imbrication). */
    private String extractJsonValue(String obj, String key, String defaultVal) {
        // Correspondance : "key":"value" ou "key":123 ou "key":true
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|([\\w.@+-]+))");
        Matcher m = p.matcher(obj);
        if (m.find()) {
            return m.group(1) != null ? m.group(1) : m.group(2);
        }
        return defaultVal;
    }
}
