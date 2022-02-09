package io.drogue.cloud.demo.game.controller.events.ditto;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
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
//                .addPathSegment(feature)
//                .addPathSegment("desiredProperties")
                .build().toString();

        final var json = new JsonObject()
                .put(feature, new JsonObject()
                        .put("desiredProperties", value));
        final var condition = buildCondition(String.format("features/%s/desiredProperties", feature), json);

        return this.client.patchAbs(url)
                .addQueryParam("channel", "twin")
                .addQueryParam("condition", condition)
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/merge-patch+json")
                .sendJsonObject(json);
    }

    static String buildCondition(final String prefix, final JsonObject value) {

        return expand(prefix, value)
                .map(entry -> String.format("ne(%s,%s)",
                        entry.name,
                        entry.value)
                )
                .collect(Collectors
                        .joining(",", "or(", ")"));

    }

    static class Pair {
        final String name;
        final String value;

        Pair(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
    }

    /*
     * Deep expand an object into JSON pointer / value pairs.
     */
    static Stream<Pair> expand(final String prefix, final Object value) {
        if (value == null) {
            return Stream.of(new Pair(prefix, "null"));
        } else if (value instanceof Boolean) {
            return Stream.of(new Pair(prefix, Boolean.toString((Boolean) value)));
        } else if (value instanceof Integer) {
            return Stream.of(new Pair(prefix, Integer.toString((Integer) value)));
        } else if (value instanceof Number) {
            return Stream.of(new Pair(prefix, Double.toString(((Number) value).doubleValue())));
        } else if (value instanceof String) {
            return Stream.of(new Pair(prefix, '"' + URLEncoder.encode((String) value, StandardCharsets.UTF_8) + '"'));
        } else {
            return JsonObject.mapFrom(value).stream().flatMap(entry -> {
                return expand(prefix + '/' + entry.getKey(), entry.getValue());
            });
        }
    }

}
