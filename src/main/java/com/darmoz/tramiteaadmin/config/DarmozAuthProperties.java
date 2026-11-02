package com.darmoz.tramiteaadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * darmoz-auth: el login se delega en su API y los access tokens (RS256) se
 * verifican acá con su llave pública. Solo pasan los de la Application de
 * Tramitea que tienen el rol {@link #adminRole}.
 */
@ConfigurationProperties(prefix = "darmoz-auth")
public class DarmozAuthProperties {

    /** Base de la API de darmoz-auth, con su context path (http://darmoz-auth:8080/auth). */
    private String url;

    /** PEM X.509 con la llave pública RSA con la que firma darmoz-auth. */
    private String publicKeyPath;

    private String issuer = "darmoz-auth";

    /** UUID de la Application "Tramitea" en darmoz-auth (header API_ID y claim applicationId). */
    private String applicationId;

    /** Rol de darmoz-auth que habilita el acceso al admin. */
    private String adminRole = "ADMIN";

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPublicKeyPath() { return publicKeyPath; }
    public void setPublicKeyPath(String publicKeyPath) { this.publicKeyPath = publicKeyPath; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getAdminRole() { return adminRole; }
    public void setAdminRole(String adminRole) { this.adminRole = adminRole; }
}
