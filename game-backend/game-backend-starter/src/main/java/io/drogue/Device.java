/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.drogue.demo;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Device {

    public String id;

    public String pairedUser;

    public Device() {
        // default constructor
    }

    public Device(String id, String pairedUser) {
        this.id = id;
        this.pairedUser = pairedUser;
    }

    public static Multi<Device> findAll(PgPool client) {
        return client.query("SELECT id, user FROM devices ORDER BY user ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Device::from);
    }

    public static Uni<Device> findByUser(PgPool client, String user) {
        return client.preparedQuery("SELECT id, user FROM devices WHERE user = $1").execute(Tuple.of(user))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<String> save(PgPool client) {
        return client.preparedQuery("INSERT INTO devices (id, user) VALUES ($1, $2) RETURNING id").execute(Tuple.of(id, pairedUser))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getString("id"));
    }

    public static Uni<Boolean> delete(PgPool client, String id) {
        return client.preparedQuery("DELETE FROM devices WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> deleteByuser(PgPool client, String user) {
        return client.preparedQuery("DELETE FROM devices WHERE user = $1").execute(Tuple.of(user))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    private static Device from(Row row) {
        return new Device(row.getString("id"), row.getString("user"));
    }
}
