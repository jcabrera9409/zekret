package com.zekret.controller;

import org.jboss.logging.Logger;

import com.zekret.dto.APIResponseDTO;
import com.zekret.dto.AuthResponseDTO;
import com.zekret.service.IAuthService;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {
    private static final Logger LOG = Logger.getLogger(AuthController.class);

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @GET
    @Path("/login")
    @PermitAll
    public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {
        LOG.infof("Login attempt for user: %s", username);

        AuthResponseDTO authResponse = authService.authenticate(username, password);
        
        return Response.ok(APIResponseDTO.success(
            authResponse.message(),
            authResponse,
            200
        )).build();
    }

}