package io.drogue.demo;

import javax.inject.Inject;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.oidc.IdToken;
import io.quarkus.oidc.RefreshToken;


@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
/**
 * where the user links his device ID to his username, and manage his enrolled devices.
 */
public class GameService {

    /**
     * Injection point for the ID Token issued by the OpenID Connect Provider
     */
    @Inject
    @IdToken
    JsonWebToken idToken;

    /**
     * Injection point for the Access Token issued by the OpenID Connect Provider
     */
    @Inject
    JsonWebToken accessToken;

    /**
     * Injection point for the Refresh Token issued by the OpenID Connect Provider
     */
    @Inject
    RefreshToken refreshToken;

    private final PgPool client;

    public GameService(PgPool client) {
        this.client = client;
    }


    /**
     * Pull your profile from the db
     **
     * @return a map containing the username and the linked device
     */
    @GET
    @Path("start}")
    public Uni<Response> startGame() {

        String userName = this.idToken.getClaim("preferred_username");

        return Device.findByUser(client, userName)
                .onItem().transform(dev -> dev != null ? Response.ok(dev) : Response.status(Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }
}

