package com.skillproof.backend.freeze;

import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Separately proves V7 -> V8 upgrade; fresh-database migration alone does not prove upgrade safety.
 */
@EnabledIfEnvironmentVariable(named = "POSTGRES_IT_ENABLED", matches = "true")
class FreezeMigrationUpgradeTest {
  @Test
  void existingVersionSevenRowsSurviveTheAdditiveMigration() throws Exception {
    String url = System.getenv("TEST_DATABASE_URL"),
        user = System.getenv("TEST_DATABASE_USER"),
        password = System.getenv("TEST_DATABASE_PASSWORD");
    String schema = "upgrade_" + UUID.randomUUID().toString().replace("-", "");
    try (var connection = DriverManager.getConnection(url, user, password)) {
      Flyway.configure()
          .dataSource(url, user, password)
          .schemas(schema)
          .defaultSchema(schema)
          .target("7")
          .load()
          .migrate();
      connection.setSchema(schema);
      UUID learner = UUID.randomUUID(), course = UUID.randomUUID(), version = UUID.randomUUID();
      try (var insert =
          connection.prepareStatement(
              "insert into"
                  + " user_account(id,email,password_hash,display_name,role,status,created_at,updated_at)"
                  + " values (?,?,?,'User','LEARNER','ACTIVE',now(),now())")) {
        insert.setObject(1, learner);
        insert.setString(2, learner + "@example.org");
        insert.setString(3, "test-hash");
        insert.executeUpdate();
      }
      try (var insert =
          connection.prepareStatement(
              "insert into course(id,slug,title,summary,created_by,created_at) values"
                  + " (?,?,'Existing course','Summary',?,now())")) {
        insert.setObject(1, course);
        insert.setString(2, "course-" + course);
        insert.setObject(3, learner);
        insert.executeUpdate();
      }
      try (var insert =
          connection.prepareStatement(
              "insert into course_version(id,course_id,version_no,status,created_at) values"
                  + " (?,?,1,'DRAFT',now())")) {
        insert.setObject(1, version);
        insert.setObject(2, course);
        insert.executeUpdate();
      }
      Flyway.configure()
          .dataSource(url, user, password)
          .schemas(schema)
          .defaultSchema(schema)
          .load()
          .migrate();
      try (var query =
          connection.prepareStatement(
              "select domain_tag,difficulty,skills::text from course_version where id=?")) {
        query.setObject(1, version);
        try (var row = query.executeQuery()) {
          assertThat(row.next()).isTrue();
          assertThat(row.getString(1)).isEqualTo("general");
          assertThat(row.getString(2)).isEqualTo("UNSPECIFIED");
          assertThat(row.getString(3)).isEqualTo("[]");
        }
      }
      try (var drop = connection.createStatement()) {
        drop.execute("drop schema " + schema + " cascade");
      }
    }
  }
}
