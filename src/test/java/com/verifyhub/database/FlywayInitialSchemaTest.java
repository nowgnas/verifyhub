package com.verifyhub.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlywayInitialSchemaTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("verifyhub")
            .withUsername("verifyhub")
            .withPassword("verifyhub");

    @Test
    void appliesInitialSchemaToMySql8() throws SQLException {
        migrate();

        try (Connection connection = openConnection()) {
            assertThat(tableNames(connection)).containsExactlyInAnyOrder(
                    "flyway_schema_history",
                    "verification_request",
                    "verification_history",
                    "provider_call_history",
                    "late_callback_history",
                    "provider_routing_policy",
                    "outbox_event"
            );
        }
    }

    @Test
    void createsVerificationRequestColumnsForIdempotencyOptimisticLockAndProviderCorrelation() throws SQLException {
        migrate();

        try (Connection connection = openConnection()) {
            assertThat(columnNames(connection, "verification_request")).contains(
                    "id",
                    "verification_id",
                    "user_id",
                    "purpose",
                    "idempotency_key",
                    "provider",
                    "status",
                    "provider_transaction_id",
                    "provider_request_no",
                    "web_transaction_id",
                    "routing_policy_version",
                    "requested_at",
                    "routed_at",
                    "provider_called_at",
                    "completed_at",
                    "version",
                    "created_at",
                    "updated_at"
            );
            assertThat(columnNames(connection, "verification_request")).doesNotContain("auth_url");
            assertThat(numericDefault(connection, "verification_request", "version")).isEqualTo("0");
        }
    }

    @Test
    void createsRequiredUniqueKeysAndIndexes() throws SQLException {
        migrate();

        try (Connection connection = openConnection()) {
            assertThat(indexNames(connection, "verification_request")).contains(
                    "uk_verification_id",
                    "uk_idempotency",
                    "uk_provider_transaction",
                    "uk_provider_request_no",
                    "uk_provider_web_transaction",
                    "idx_verification_status_created_at",
                    "idx_verification_provider_created_at"
            );
            assertThat(indexNames(connection, "provider_routing_policy")).contains(
                    "uk_provider_policy",
                    "idx_provider_policy_01"
            );
            assertThat(indexNames(connection, "outbox_event")).contains(
                    "idx_outbox_01",
                    "idx_outbox_02"
            );
        }
    }

    @Test
    void preventsDuplicateProviderCorrelationIdentifiers() throws SQLException {
        migrate();

        try (Connection connection = openConnection()) {
            insertVerificationRequest(
                    connection,
                    "verif_001",
                    "user-001",
                    "idem-001",
                    "provider-tx-001",
                    "provider-request-001",
                    "web-tx-001"
            );

            assertThatThrownBy(() -> insertVerificationRequest(
                    connection,
                    "verif_002",
                    "user-002",
                    "idem-002",
                    "provider-tx-001",
                    "provider-request-002",
                    "web-tx-002"
            )).isInstanceOf(SQLIntegrityConstraintViolationException.class);

            assertThatThrownBy(() -> insertVerificationRequest(
                    connection,
                    "verif_003",
                    "user-003",
                    "idem-003",
                    "provider-tx-003",
                    "provider-request-001",
                    "web-tx-003"
            )).isInstanceOf(SQLIntegrityConstraintViolationException.class);

            assertThatThrownBy(() -> insertVerificationRequest(
                    connection,
                    "verif_004",
                    "user-004",
                    "idem-004",
                    "provider-tx-004",
                    "provider-request-004",
                    "web-tx-001"
            )).isInstanceOf(SQLIntegrityConstraintViolationException.class);
        }
    }

    @Test
    void createsJsonPayloadColumns() throws SQLException {
        migrate();

        try (Connection connection = openConnection()) {
            assertThat(columnType(connection, "verification_history", "raw_payload")).isEqualTo("json");
            assertThat(columnType(connection, "provider_call_history", "request_payload")).isEqualTo("json");
            assertThat(columnType(connection, "provider_call_history", "response_payload")).isEqualTo("json");
            assertThat(columnType(connection, "late_callback_history", "raw_payload")).isEqualTo("json");
            assertThat(columnType(connection, "outbox_event", "payload")).isEqualTo("json");
        }
    }

    private static void migrate() {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load()
                .clean();

        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    private static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private static void insertVerificationRequest(
            Connection connection,
            String verificationId,
            String userId,
            String idempotencyKey,
            String providerTransactionId,
            String providerRequestNo,
            String webTransactionId
    ) throws SQLException {
        String sql = """
                INSERT INTO verification_request (
                    verification_id,
                    user_id,
                    purpose,
                    idempotency_key,
                    provider,
                    status,
                    provider_transaction_id,
                    provider_request_no,
                    web_transaction_id,
                    requested_at,
                    created_at,
                    updated_at
                ) VALUES (?, ?, 'SIGN_UP', ?, 'NICE', 'IN_PROGRESS', ?, ?, ?, NOW(6), NOW(6), NOW(6))
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, verificationId);
            statement.setString(2, userId);
            statement.setString(3, idempotencyKey);
            statement.setString(4, providerTransactionId);
            statement.setString(5, providerRequestNo);
            statement.setString(6, webTransactionId);
            statement.executeUpdate();
        }
    }

    private static Set<String> tableNames(Connection connection) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(MYSQL.getDatabaseName(), null, "%", new String[]{"TABLE"})) {
            return resultSetToSet(resultSet, "TABLE_NAME");
        }
    }

    private static Set<String> columnNames(Connection connection, String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getColumns(MYSQL.getDatabaseName(), null, tableName, "%")) {
            return resultSetToSet(resultSet, "COLUMN_NAME");
        }
    }

    private static Set<String> indexNames(Connection connection, String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(MYSQL.getDatabaseName(), null, tableName, false, false)) {
            return resultSetToSet(resultSet, "INDEX_NAME");
        }
    }

    private static String columnType(Connection connection, String tableName, String columnName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getColumns(MYSQL.getDatabaseName(), null, tableName, columnName)) {
            resultSet.next();
            return resultSet.getString("TYPE_NAME").toLowerCase();
        }
    }

    private static String numericDefault(Connection connection, String tableName, String columnName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getColumns(MYSQL.getDatabaseName(), null, tableName, columnName)) {
            resultSet.next();
            return resultSet.getString("COLUMN_DEF");
        }
    }

    private static Set<String> resultSetToSet(ResultSet resultSet, String columnName) throws SQLException {
        Stream.Builder<String> values = Stream.builder();
        while (resultSet.next()) {
            values.add(resultSet.getString(columnName));
        }
        return values.build().collect(Collectors.toSet());
    }
}
