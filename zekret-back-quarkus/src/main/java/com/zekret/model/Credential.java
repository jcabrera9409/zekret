package com.zekret.model;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_credential")
@RegisterForReflection
public class Credential extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "zrn", nullable = false, unique = true)
	private String zrn;
	
	@Column(name = "username", nullable = true)
	private String username;
	
	@Column(name = "password", nullable = true)
	private String password;
	
    @Lob
	@Column(name = "ssh_public_key", nullable = true, columnDefinition = "TEXT")
	private String sshPublicKey;

    @Lob
	@Column(name = "ssh_private_key", nullable = true, columnDefinition = "TEXT")
	private String sshPrivateKey;
	
	@Column(name = "secret_text", nullable = true)
	private String secretText;
	
	@Column(name = "file_name", nullable = true)
	private String fileName;
	
    @Lob
	@Column(name = "file_content", nullable = true, columnDefinition = "TEXT")
	private String fileContent;
	
    @Lob
	@Column(name = "notes", nullable = true, columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
	@JoinColumn(name = "id_credential_type", nullable = true)
	private CredentialType credentialType;

    @ManyToOne
	@JoinColumn(name = "id_namespace", nullable = true)
	private Namespace namespace;

    @ManyToOne
	@JoinColumn(name = "id_user", nullable = true)
	private User user;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

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

	public String getSshPublicKey() {
		return sshPublicKey;
	}

	public void setSshPublicKey(String sshPublicKey) {
		this.sshPublicKey = sshPublicKey;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
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