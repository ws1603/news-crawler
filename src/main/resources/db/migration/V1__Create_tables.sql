create table news (
id bigint primary key auto_increment,
title text,
content text,
url varchar(500),
created_at timestamp,
modified_at timestamp
);

create table links_to_be_processed (link varchar(500));
create table links_already_processed (link varchar(500));


--package db.migration;
--
--import org.flywaydb.core.api.migration.BaseJavaMigration;
--import org.flywaydb.core.api.migration.Context;
--import java.sql.PreparedStatement;
--
--/**
-- * Example of a Java-based migration.
-- */
--public class V1__Create_tables extends BaseJavaMigration {
--    public void migrate(Context context) throws Exception {
--        try (PreparedStatement statement =
--                 context
--                     .getConnection()
--                     .prepareStatement("INSERT INTO test_user (name) VALUES ('Obelix')")) {
--            statement.execute();
--        }
--    }
--}


