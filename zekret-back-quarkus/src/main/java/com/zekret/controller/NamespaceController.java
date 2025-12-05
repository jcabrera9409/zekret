package com.zekret.controller;

import java.util.List;

import org.jboss.logging.Logger;

import com.zekret.dto.APIResponseDTO;
import com.zekret.dto.NamespaceRequestDTO;
import com.zekret.dto.NamespaceResponseDTO;
import com.zekret.service.INamespaceService;

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

@Path("/v1/namespaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NamespaceController {
    private static final Logger LOG = Logger.getLogger(NamespaceController.class.getName());

    private final INamespaceService namespaceService;

    public NamespaceController(INamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * Endpoint to create a new namespace.
     */
    @POST
    @RolesAllowed("user")
    public Response createNamespace(@Context SecurityContext securityContext, @Valid NamespaceRequestDTO namespaceRequest) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Creating namespace for user: %s", username);

        NamespaceResponseDTO createdNamespace = namespaceService.registerNamespace(username, namespaceRequest);

        return Response.ok(
            APIResponseDTO.success(
                "Namespace created successfully.",
                createdNamespace,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to update an existing namespace by zrn.
     */
    @PUT
    @Path("/{zrn}")
    @RolesAllowed("user")
    public Response updateNamespace(@Context SecurityContext securityContext, @PathParam("zrn") String zrn, @Valid NamespaceRequestDTO namespaceRequest) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Updating namespace with ZRN: %s for user: %s", zrn, username);

        NamespaceResponseDTO updatedNamespace = namespaceService.updateNamespace(username, zrn, namespaceRequest);

        return Response.ok(
            APIResponseDTO.success(
                "Namespace updated successfully.",
                updatedNamespace,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to retrieve a namespace by zrn.
     */
    @GET
    @Path("/{zrn}")
    @RolesAllowed("user")
    public Response getNamespace(@Context SecurityContext securityContext, @PathParam("zrn") String zrn) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Getting namespace with ZRN: %s for user: %s", zrn, username);

        NamespaceResponseDTO namespace = namespaceService.getNamespaceByZrnAndUserEmail(zrn, username);

        return Response.ok(
            APIResponseDTO.success(
                "Namespace retrieved successfully.",
                namespace,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to list all namespaces.
     */
    @GET
    @RolesAllowed("user")
    public Response listNamespaces(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Listing namespaces for user: %s", username);

        List<NamespaceResponseDTO> namespaces = namespaceService.getNamespacesByUserEmail(username);

        return Response.ok(
            APIResponseDTO.success(
                "Namespaces retrieved successfully.",
                namespaces,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }

    /**
     * Endpoint to delete a namespace by zrn.
     */
    @DELETE
    @Path("/{zrn}")
    @RolesAllowed("user")
    public Response deleteNamespace(@Context SecurityContext securityContext, @PathParam("zrn") String zrn) {
        String username = securityContext.getUserPrincipal().getName();

        LOG.infof("Deleting namespace with ZRN: %s for user: %s", zrn, username);

        namespaceService.deleteNamespaceByZrnAndUserEmail(zrn, username);

        return Response.ok(
            APIResponseDTO.success(
                "Namespace deleted successfully.",
                null,
                Response.Status.OK.getStatusCode()
            )
        ).build();
    }
}