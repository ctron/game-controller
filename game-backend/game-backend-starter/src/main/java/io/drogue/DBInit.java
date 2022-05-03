package io.drogue.demo;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class DBInit {

    private final PgPool client;
    private final boolean schemaCreate;

    public DBInit(PgPool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initdb();
        }
    }


    private void initdb() {
        client.query("DROP TABLE IF EXISTS devices").execute()
                .flatMap(r -> client.query("CREATE TABLE devices (id VARCHAR(256) PRIMARY KEY NOT NULL, user VARCHAR(256))").execute())
                .await().indefinitely();
    }
}