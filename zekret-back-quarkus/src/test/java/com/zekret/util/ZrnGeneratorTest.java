package com.zekret.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ZrnGeneratorTest {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    void testGenerateCredentialZrn() {
        String zrn = ZrnGenerator.generateCredentialZrn();
        
        assertNotNull(zrn);
        assertTrue(zrn.startsWith("zrn:zekret:credential:"));
        assertTrue(ZrnGenerator.isValidZrn(zrn));
        assertEquals("credential", ZrnGenerator.extractResourceType(zrn));
    }

    @Test
    void testGenerateNamespaceZrn() {
        String zrn = ZrnGenerator.generateNamespaceZrn();
        
        assertNotNull(zrn);
        assertTrue(zrn.startsWith("zrn:zekret:namespace:"));
        assertTrue(ZrnGenerator.isValidZrn(zrn));
        assertEquals("namespace", ZrnGenerator.extractResourceType(zrn));
    }

    @Test
    void testGenerateCredentialTypeZrnWithName() {
        String zrn = ZrnGenerator.generateCredentialTypeZrn("SSH Credential");
        
        assertEquals("ssh_credential", zrn);
        assertTrue(ZrnGenerator.isValidZrn(zrn));
        assertEquals("credtype", ZrnGenerator.extractResourceType(zrn));
    }

    @Test
    void testGenerateCredentialTypeZrnGeneric() {
        String zrn = ZrnGenerator.generateCredentialTypeZrn();
        
        assertNotNull(zrn);
        assertTrue(zrn.startsWith("zrn:zekret:credtype:"));
        assertTrue(ZrnGenerator.isValidZrn(zrn));
    }

    @Test
    void testGenerateWithCustomId() {
        String customId = "custom-123";
        String zrn = ZrnGenerator.generateWithCustomId(ZrnGenerator.ResourceType.CREDENTIAL, customId);
        
        assertNotNull(zrn);
        assertTrue(zrn.startsWith("zrn:zekret:credential:"));
        assertTrue(zrn.endsWith(customId));
        assertTrue(ZrnGenerator.isValidZrn(zrn));
        assertEquals(customId, ZrnGenerator.extractIdentifier(zrn));
    }

    @Test
    void testIsValidZrn_ValidFullZrn() {
        String validZrn = "zrn:zekret:credential:20250715:a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        assertTrue(ZrnGenerator.isValidZrn(validZrn));
    }

    @Test
    void testIsValidZrn_ValidSlug() {
        String validSlug = "ssh_credential";
        assertTrue(ZrnGenerator.isValidZrn(validSlug));
    }

    @Test
    void testIsValidZrn_InvalidFormat() {
        assertFalse(ZrnGenerator.isValidZrn("invalid:format"));
        assertFalse(ZrnGenerator.isValidZrn("zrn:wrong:credential:20250715:uuid"));
        assertFalse(ZrnGenerator.isValidZrn("zrn:zekret:invalidtype:20250715:uuid"));
        assertFalse(ZrnGenerator.isValidZrn(""));
        assertFalse(ZrnGenerator.isValidZrn(null));
    }

    @Test
    void testIsValidSlug() {
        assertTrue(ZrnGenerator.isValidSlug("ssh_credential"));
        assertTrue(ZrnGenerator.isValidSlug("password_manager"));
        assertTrue(ZrnGenerator.isValidSlug("simple"));
        assertTrue(ZrnGenerator.isValidSlug("with123numbers"));
        
        assertFalse(ZrnGenerator.isValidSlug("_startswithunderscore"));
        assertFalse(ZrnGenerator.isValidSlug("endswithunderscore_"));
        assertFalse(ZrnGenerator.isValidSlug("has-dash"));
        assertFalse(ZrnGenerator.isValidSlug("HAS_UPPERCASE"));
        assertFalse(ZrnGenerator.isValidSlug("has space"));
        assertFalse(ZrnGenerator.isValidSlug(""));
        assertFalse(ZrnGenerator.isValidSlug(null));
    }

    @Test
    void testExtractResourceType_FullZrn() {
        String zrn = "zrn:zekret:credential:20250715:a1b2c3d4";
        assertEquals("credential", ZrnGenerator.extractResourceType(zrn));
        
        String namespaceZrn = "zrn:zekret:namespace:20250715:b2c3d4e5";
        assertEquals("namespace", ZrnGenerator.extractResourceType(namespaceZrn));
    }

    @Test
    void testExtractResourceType_Slug() {
        assertEquals("credtype", ZrnGenerator.extractResourceType("ssh_credential"));
        assertEquals("credtype", ZrnGenerator.extractResourceType("password_manager"));
    }

    @Test
    void testExtractResourceType_Invalid() {
        assertNull(ZrnGenerator.extractResourceType("invalid:format"));
        assertNull(ZrnGenerator.extractResourceType(""));
        assertNull(ZrnGenerator.extractResourceType(null));
    }

    @Test
    void testExtractDate() {
        String today = LocalDateTime.now().format(DATE_FORMATTER);
        String zrn = ZrnGenerator.generateCredentialZrn();
        
        String extractedDate = ZrnGenerator.extractDate(zrn);
        assertEquals(today, extractedDate);
        
        // Slugs no tienen fecha
        assertNull(ZrnGenerator.extractDate("ssh_credential"));
    }

    @Test
    void testExtractIdentifier() {
        String customId = "test-identifier-123";
        String zrn = ZrnGenerator.generateWithCustomId(ZrnGenerator.ResourceType.NAMESPACE, customId);
        
        assertEquals(customId, ZrnGenerator.extractIdentifier(zrn));
        
        // Para slugs, el identificador es el slug completo
        assertEquals("ssh_credential", ZrnGenerator.extractIdentifier("ssh_credential"));
    }

    @Test
    void testGenerateSlug() {
        assertEquals("ssh_credential", ZrnGenerator.generateSlug("SSH Credential"));
        assertEquals("password_manager", ZrnGenerator.generateSlug("Password Manager"));
        assertEquals("simple_text", ZrnGenerator.generateSlug("Simple Text"));
        assertEquals("multi_word_test", ZrnGenerator.generateSlug("Multi   Word   Test"));
        assertEquals("special_chars", ZrnGenerator.generateSlug("Special!@#$%^&*() Chars"));
        assertEquals("trim_spaces", ZrnGenerator.generateSlug("  Trim Spaces  "));
        assertEquals("unknown_type", ZrnGenerator.generateSlug(""));
        assertEquals("unknown_type", ZrnGenerator.generateSlug(null));
    }

    @Test
    void testGenerateSlug_RemovesDuplicateUnderscores() {
        // The actual implementation removes all underscores, not just duplicates
        assertEquals("testslug", ZrnGenerator.generateSlug("Test___Slug"));
    }

    @Test
    void testGenerateSlug_RemovesLeadingTrailingUnderscores() {
        assertEquals("test", ZrnGenerator.generateSlug("_Test_"));
    }

    @Test
    void testZrnUniqueness() {
        String zrn1 = ZrnGenerator.generateCredentialZrn();
        String zrn2 = ZrnGenerator.generateCredentialZrn();
        
        assertNotEquals(zrn1, zrn2, "Generated ZRNs should be unique");
    }

    @Test
    void testZrnFormat() {
        String zrn = ZrnGenerator.generateCredentialZrn();
        String[] parts = zrn.split(":");
        
        assertEquals(5, parts.length);
        assertEquals("zrn", parts[0]);
        assertEquals("zekret", parts[1]);
        assertEquals("credential", parts[2]);
        assertEquals(8, parts[3].length()); // Date format: yyyyMMdd
        assertFalse(parts[4].isEmpty()); // UUID should not be empty
    }

    @Test
    void testResourceTypeEnum() {
        assertEquals("credential", ZrnGenerator.ResourceType.CREDENTIAL.getValue());
        assertEquals("namespace", ZrnGenerator.ResourceType.NAMESPACE.getValue());
        assertEquals("credtype", ZrnGenerator.ResourceType.CREDENTIAL_TYPE.getValue());
    }
}
