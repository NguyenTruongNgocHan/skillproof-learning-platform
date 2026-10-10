package com.skillproof.backend;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.flywaydb.core.Flyway;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest
@ActiveProfiles({"test","postgres-test"})
@EnabledIfEnvironmentVariable(named="POSTGRES_IT_ENABLED", matches="true")
class PostgreSqlSchemaIntegrationTest {
    @Autowired Flyway flyway;
    @Autowired JdbcTemplate jdbc;
    @Test void migrationsValidateAndTimeoutConstraintExists() {
        flyway.validate();
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success",Integer.class)).isEqualTo(10);
        String definition=jdbc.queryForObject("select pg_get_constraintdef(oid) from pg_constraint where conrelid='assessment_attempt'::regclass and conname='ck_attempt_terminal_score'",String.class);
        assertThat(definition).contains("TIMED_OUT");
    }
    @Test void ownershipAndSnapshotGuardsExist() {
        Integer guards=jdbc.queryForObject("select count(*) from pg_trigger where not tgisinternal and tgname in ('guard_progress_version','guard_lesson_progress','guard_assignment_owner','guard_assessment_owner','guard_version_snapshot','guard_media_target','guard_payment_product')",Integer.class);
        assertThat(guards).isEqualTo(7);
    }
    @Test @org.springframework.transaction.annotation.Transactional
    void resourceFromAnotherVersionCannotContributeProgress() {
        java.util.UUID user=java.util.UUID.randomUUID(),course=java.util.UUID.randomUUID(),v1=java.util.UUID.randomUUID(),v2=java.util.UUID.randomUUID(),module=java.util.UUID.randomUUID(),lesson=java.util.UUID.randomUUID(),resource=java.util.UUID.randomUUID(),enrollment=java.util.UUID.randomUUID();
        jdbc.update("insert into user_account(id,email,password_hash,display_name,role,status,created_at,updated_at) values (?,?,?,'Learner','LEARNER','ACTIVE',now(),now())",user,user+"@example.org","test-hash");
        jdbc.update("insert into course(id,slug,title,summary,created_by,created_at) values (?,?,'Course','Summary',?,now())",course,"course-"+course,user);
        jdbc.update("insert into course_version(id,course_id,version_no,status,created_at,published_at) values (?,?,1,'PUBLISHED',now(),now())",v1,course);
        jdbc.update("insert into course_version(id,course_id,version_no,status,created_at) values (?,?,2,'DRAFT',now())",v2,course);
        jdbc.update("insert into course_module(id,version_id,position,title) values (?,?,1,'Module')",module,v2);
        jdbc.update("insert into course_lesson(id,module_id,position,title,body) values (?,?,1,'Lesson','Text')",lesson,module);
        jdbc.update("insert into course_resource(id,module_id,lesson_id,position,kind,title,body) values (?,?,?,1,'ARTICLE','Resource','Text')",resource,module,lesson);
        jdbc.update("insert into enrollment(id,learner_id,course_id,version_id,status,enrolled_at) values (?,?,?,?,'ACTIVE',now())",enrollment,user,course,v1);
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
            ()->jdbc.update("insert into resource_progress(enrollment_id,resource_id,completed_at) values (?,?,now())",enrollment,resource));
    }
}

