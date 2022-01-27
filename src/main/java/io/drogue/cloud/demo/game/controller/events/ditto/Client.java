package io.drogue.cloud.demo.game.controller.events.ditto;

import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.web.client.OAuth2WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.OAuth2WebClient;
import io.vertx.mutiny.ext.web.client.WebClient;
import okhttp3.HttpUrl;

@ApplicationScoped
public class Client {

    private final OAuth2WebClient client;

    @ConfigProperty(name = "ditto.api.url")
    URL dittoUrl;

    @ConfigProperty(name = "keycloak.url")
    URL keycloakUrl;

    @ConfigProperty(name = "ditto.client.id")
    String clientId;

    @ConfigProperty(name = "ditto.client.secret")
    String clientSecret;

    @Inject
    public Client(final Vertx vertx) {
        final var client = WebClient.create(vertx);

        final var oauth2Auth = KeycloakAuth.discoverAndAwait(
                vertx,
                new OAuth2Options()
                        .setSite(this.keycloakUrl.toString())
                        .setClientId(this.clientId)
                        .setClientSecret(this.clientSecret)
        );

        this.client = OAuth2WebClient.create(
                client,
                oauth2Auth,
                new OAuth2WebClientOptions()
                        .setLeeway(5)
                        .setRenewTokenOnForbidden(true)
        );
    }

    public Uni<HttpResponse<Buffer>> setDesiredProperties(final String thingId, final String feature, final Object value) {

        final var url = HttpUrl.get(this.dittoUrl).newBuilder()
                .addPathSegment("things")
                .addPathSegment(thingId)
                .addPathSegment("features")
                .addPathSegment(feature)
                .addPathSegment("desiredProperties")
                .build().toString();

        return this.client.putAbs(url)
                .addQueryParam("channel", "twin")
                .sendJson(value);
    }

}
