package org.example.realworldapi.web.resource;

import org.example.realworldapi.domain.entity.User;
import org.example.realworldapi.domain.security.Role;
import org.example.realworldapi.domain.service.UsersService;
import org.example.realworldapi.infrastructure.annotation.Secured;
import org.example.realworldapi.web.dto.UpdateUserDTO;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/user")
public class UserResource {

    private UsersService usersService;

    public UserResource(UsersService usersService){
        this.usersService = usersService;
    }

    @GET
    @Secured({Role.ADMIN, Role.USER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context SecurityContext securityContext){
        User user = usersService.findById(Long.valueOf(securityContext.getUserPrincipal().getName()));
        return Response.ok(user).status(Response.Status.OK).build();
    }

    @PUT
    @Secured({Role.USER, Role.USER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Context SecurityContext securityContext, @Valid UpdateUserDTO updateUserDTO){
        User updatedUser = usersService.update(updateUserDTO.toUser(Long.valueOf(securityContext.getUserPrincipal().getName())));
        return Response.ok(updatedUser).status(Response.Status.OK).build();
    }

}
