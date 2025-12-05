package com.zekret.controller;

import java.util.List;

import org.jboss.logging.Logger;

import com.zekret.dto.APIResponseDTO;
import com.zekret.dto.CredentialRequestDTO;
import com.zekret.dto.CredentialResponseDTO;
import com.zekret.service.ICredentialService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/v1/credentials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CredentialController {
    private static final Logger LOG = Logger.getLogger(CredentialController.class.getName());

    private final ICredentialService credentialService;

    public CredentialController(ICredentialService credentialService) {
        this.credentialService = credentialService;
    }

    /**
     * Endpoint to create a new credential.
     */
    @POST
    @RolesAllowed("user")
    public Response createCredential(@Context SecurityContext securityContext, @Valid CredentialRequestDTO credentialRequestDTO) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Creating credential for user: %s", username);

        CredentialResponseDTO createdCredential = credentialService.registerCredential(username, credentialRequestDTO);

        return Response.ok(
            APIResponseDTO.success(
                "Credential created successfully.",
                createdCredential,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to update an existing credential.
     */
    @PUT
    @Path("/{zrn}")
    @RolesAllowed("user")
    public Response updateCredential(@Context SecurityContext securityContext, @PathParam("zrn") String zrn, @Valid CredentialRequestDTO credentialRequestDTO) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Updating credential %s for user: %s", zrn, username);

        CredentialResponseDTO updatedCredential = credentialService.updateCredential(username, zrn, credentialRequestDTO);

        return Response.ok(
            APIResponseDTO.success(
                "Credential updated successfully.",
                updatedCredential,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to get a credential by zrn.
     */
    @GET
    @Path("/{zrn}")
    @RolesAllowed("user")
    public Response getCredential(@Context SecurityContext securityContext,@PathParam("zrn") String zrn) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Getting credential %s for user: %s", zrn, username);

        CredentialResponseDTO credential = credentialService.getCredentialByZrnAndUserEmail(zrn, username);
        return Response.ok(
            APIResponseDTO.success(
                "Credential retrieved successfully.",
                credential,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to list all credentials for the authenticated user and namespace.
     */
    @GET
    @Path("/namespace/{namespaceZrn}")
    @RolesAllowed("user")
    public Response listCredentials(@Context SecurityContext securityContext, @PathParam("namespaceZrn") String namespaceZrn) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Listing credentials for namespace %s and user: %s", namespaceZrn, username);

        List<CredentialResponseDTO> credentials = credentialService.getCredentialsByNamespaceAndUserId(namespaceZrn, username);
        return Response.ok(
            APIResponseDTO.success(
                "Credentials listed successfully.",
                credentials,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to delete a credential by zrn.
     */
    @DELETE
    @Path("/{zrn}")
    @RolesAllowed("user")
    public Response deleteCredential(@Context SecurityContext securityContext, @PathParam("zrn") String zrn) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Deleting credential %s for user: %s", zrn, username);

        credentialService.deleteCredentialByZrnAndUserEmail(zrn, username);
    
        return Response.ok(
            APIResponseDTO.success(
                "Credential deleted successfully.",
                null,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }
}