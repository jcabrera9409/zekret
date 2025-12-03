package com.zekret.controller;

import org.jboss.logging.Logger;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/namespaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NamespaceController {
    private static final Logger LOG = Logger.getLogger(NamespaceController.class.getName());

    @POST
    @RolesAllowed("user")
    public Response createNamespace() {
        LOG.info("Create namespace endpoint called.");
        // Implementation for creating a namespace would go here.

        return Response.ok().build();
    }
}
