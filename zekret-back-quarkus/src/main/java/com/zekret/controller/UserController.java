package com.zekret.controller;

import org.jboss.logging.Logger;

import com.zekret.dto.APIResponseDTO;
import com.zekret.dto.UserRequestDTO;
import com.zekret.dto.UserResponseDTO;
import com.zekret.service.IUserService;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {
    private static final Logger LOG = Logger.getLogger(UserController.class.getName());

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid UserRequestDTO userRequest) {
        LOG.infof("Registering user: %s", userRequest.username());
        UserResponseDTO responseDTO = userService.register(userRequest);
        return Response.status(Response.Status.CREATED).entity(
            APIResponseDTO.success(
                "User registered successfully.",
                responseDTO,
                Response.Status.CREATED.getStatusCode()
            )
        ).build();
    }

}
