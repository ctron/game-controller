package io.drogue.cloud.demo.game.controller.events.ditto;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
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
@Startup
public class Client {

    private final Logger log = LoggerFactory.getLogger(Client.class);

    private final Vertx vertx;
    private OAuth2WebClient client;

    @ConfigProperty(name = "ditto.api.url")
    URL dittoUrl;

    @ConfigProperty(name = "ditto.keycloak.url")
    URL keycloakUrl;

    @ConfigProperty(name = "ditto.api.client.id")
    String clientId;

    @ConfigProperty(name = "ditto.api.client.secret")
    String clientSecret;

    @Inject
    public Client(final Vertx vertx) {
        this.vertx = vertx;
    }

    @PostConstruct
    void start() throws Exception {
        this.log.info("Ditto client - SSO: {}, URL: {}, ClientId: {}", this.keycloakUrl, this.dittoUrl, this.clientId);

        final var client = WebClient.create(this.vertx);

        final var oauth2Auth = KeycloakAuth.discoverAndAwait(
                this.vertx,
                new OAuth2Options()
                        .setFlow(OAuth2FlowType.CLIENT)
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
                )
                .withCredentials(new UsernamePasswordCredentials(
                        this.clientId,
                        this.clientSecret));
    }

    @PreDestroy
    void stop() {
        this.client.close();
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
