package com.zekret.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_credential")
public class Credential {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false, unique = true)
	private String zrn;
	
	@Column(nullable = true)
	private String username;
	
	@Column(nullable = true)
	private String password;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String sshPrivateKey;
	
	@Column(nullable = true)
	private String secretText;
	
	@Column(nullable = true)
	private String fileName;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String fileContent;
	
	@Column(nullable = true, columnDefinition = "TEXT")
	private String notes;
	
	@ManyToOne
	@JoinColumn(name = "id_credential_type", nullable = true)
	private CredentialType credentialType;
	
	@ManyToOne
	@JoinColumn(name = "id_namespace", nullable = true)
	private Namespace namespace;
	
	@ManyToOne
	@JoinColumn(name = "id_user", nullable = true)
	@JsonIgnore
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getZrn() {
		return zrn;
	}

	public void setZrn(String zrn) {
		this.zrn = zrn;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSshPrivateKey() {
		return sshPrivateKey;
	}

	public void setSshPrivateKey(String sshPrivateKey) {
		this.sshPrivateKey = sshPrivateKey;
	}

	public String getSecretText() {
		return secretText;
	}

	public void setSecretText(String secretText) {
		this.secretText = secretText;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public CredentialType getCredentialType() {
		return credentialType;
	}

	public void setCredentialType(CredentialType credentialType) {
		this.credentialType = credentialType;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
