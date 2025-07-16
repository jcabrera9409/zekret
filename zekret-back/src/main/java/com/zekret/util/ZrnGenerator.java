package com.zekret.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utilidad para generar ZRN (Zekret Resource Names)
 * 
 * Los ZRN siguen el formato: zrn:zekret:recurso:region:account:tipo/identificador
 * Para simplicidad, usamos: zrn:zekret:tipo:timestamp:uuid
 * 
 * Excepción: CredentialType usa formato slug para mejor legibilidad
 * 
 * Ejemplos:
 * - zrn:zekret:credential:20250715:a1b2c3d4-e5f6-7890-abcd-ef1234567890
 * - zrn:zekret:namespace:20250715:b2c3d4e5-f6g7-8901-bcde-f23456789012
 * - ssh_credential (para CredentialType "SSH Credential")
 * - password_manager (para CredentialType "Password Manager")
 */
public class ZrnGenerator {

    private static final String ZRN_PREFIX = "zrn:zekret";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // Tipos de recursos
    public enum ResourceType {
        CREDENTIAL("credential"),
        NAMESPACE("namespace"),
        CREDENTIAL_TYPE("credtype");
        
        private final String value;
        
        ResourceType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * Genera un ZRN único para un tipo de recurso específico
     * 
     * @param resourceType El tipo de recurso
     * @return ZRN único generado
     */
    public static String generate(ResourceType resourceType) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString();
        
        return String.format("%s:%s:%s:%s", 
                ZRN_PREFIX, 
                resourceType.getValue(), 
                timestamp, 
                uuid);
    }
    
    /**
     * Genera un ZRN para una credencial
     * 
     * @return ZRN único para credencial
     */
    public static String generateCredentialZrn() {
        return generate(ResourceType.CREDENTIAL);
    }
    
    /**
     * Genera un ZRN para un namespace
     * 
     * @return ZRN único para namespace
     */
    public static String generateNamespaceZrn() {
        return generate(ResourceType.NAMESPACE);
    }
    
    /**
     * Genera un ZRN para un tipo de credencial usando formato slug
     * 
     * @param name El nombre del tipo de credencial (ej: "SSH Credential")
     * @return ZRN en formato slug (ej: "ssh_credential")
     */
    public static String generateCredentialTypeZrn(String name) {
        return generateSlug(name);
    }
    
    /**
     * Sobrecarga del método anterior para mantener compatibilidad
     * Genera un ZRN genérico para tipo de credencial
     * 
     * @return ZRN único para tipo de credencial
     */
    public static String generateCredentialTypeZrn() {
        return generate(ResourceType.CREDENTIAL_TYPE);
    }
    
    /**
     * Genera un ZRN personalizado con un identificador específico
     * 
     * @param resourceType El tipo de recurso
     * @param customId Identificador personalizado
     * @return ZRN con identificador personalizado
     */
    public static String generateWithCustomId(ResourceType resourceType, String customId) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        
        return String.format("%s:%s:%s:%s", 
                ZRN_PREFIX, 
                resourceType.getValue(), 
                timestamp, 
                customId);
    }
    
    /**
     * Valida si un string tiene el formato ZRN válido o es un slug válido para CredentialType
     * 
     * @param zrn El string a validar
     * @return true si es un ZRN válido o un slug válido, false en caso contrario
     */
    public static boolean isValidZrn(String zrn) {
        if (zrn == null || zrn.isEmpty()) {
            return false;
        }
        
        // Si es un slug (no contiene ":"), considerarlo válido para CredentialType
        if (!zrn.contains(":")) {
            return isValidSlug(zrn);
        }
        
        String[] parts = zrn.split(":");
        if (parts.length != 5) {
            return false;
        }
        
        // Verificar que comience con zrn:zekret
        if (!parts[0].equals("zrn") || !parts[1].equals("zekret")) {
            return false;
        }
        
        // Verificar que el tipo de recurso sea válido
        String resourceType = parts[2];
        for (ResourceType type : ResourceType.values()) {
            if (type.getValue().equals(resourceType)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Valida si un string es un slug válido
     * 
     * @param slug El string a validar como slug
     * @return true si es un slug válido, false en caso contrario
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        
        // Un slug válido solo contiene letras minúsculas, números y guiones bajos
        return slug.matches("^[a-z0-9_]+$") && !slug.startsWith("_") && !slug.endsWith("_");
    }
    
    /**
     * Extrae el tipo de recurso de un ZRN o determina si es un slug
     * 
     * @param zrn El ZRN del cual extraer el tipo
     * @return El tipo de recurso, "credtype" para slugs, o null si es inválido
     */
    public static String extractResourceType(String zrn) {
        if (!isValidZrn(zrn)) {
            return null;
        }
        
        // Si es un slug, es un CredentialType
        if (!zrn.contains(":")) {
            return "credtype";
        }
        
        String[] parts = zrn.split(":");
        return parts[2];
    }
    
    /**
     * Extrae la fecha de un ZRN
     * 
     * @param zrn El ZRN del cual extraer la fecha
     * @return La fecha como string (formato yyyyMMdd) o null si el ZRN es inválido o es un slug
     */
    public static String extractDate(String zrn) {
        if (!isValidZrn(zrn)) {
            return null;
        }
        
        // Los slugs no tienen fecha
        if (!zrn.contains(":")) {
            return null;
        }
        
        String[] parts = zrn.split(":");
        return parts[3];
    }
    
    /**
     * Extrae el identificador único de un ZRN
     * 
     * @param zrn El ZRN del cual extraer el identificador
     * @return El identificador único, el slug completo para CredentialType, o null si es inválido
     */
    public static String extractIdentifier(String zrn) {
        if (!isValidZrn(zrn)) {
            return null;
        }
        
        // Si es un slug, el identificador es el slug completo
        if (!zrn.contains(":")) {
            return zrn;
        }
        
        String[] parts = zrn.split(":");
        return parts[4];
    }
    
    /**
     * Convierte un nombre a formato slug (minúsculas con guiones bajos)
     * 
     * @param name El nombre a convertir
     * @return El nombre en formato slug
     */
    public static String generateSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "unknown_type";
        }
        
        return name.toLowerCase()
                   .trim()
                   .replaceAll("[^a-z0-9\\s]", "") // Remover caracteres especiales
                   .replaceAll("\\s+", "_")        // Reemplazar espacios con guiones bajos
                   .replaceAll("_+", "_")          // Eliminar guiones bajos duplicados
                   .replaceAll("^_|_$", "");       // Eliminar guiones bajos al inicio/final
    }
}
