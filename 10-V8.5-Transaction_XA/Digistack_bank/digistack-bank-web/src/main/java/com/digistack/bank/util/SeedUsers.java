package com.digistack.bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * SeedUsers — P01 v2
 *
 * Standalone utility that connects to the digistack_bank database
 * and updates the two seed users (customer1, admin1) with correctly
 * computed SHA-256 password hashes.
 *
 * Run ONCE after V2__create_users.sql migration, before deploying v2.
 *
 * How to compile and run (from digistack-bank-parent on Linux):
 *
 *   0. Copy config/db-local.properties.template to
 *      config/db-local.properties and fill in real DB credentials.
 *      This file is gitignored — never committed to source control.
 *
 *   1. Build the project so PasswordUtil is compiled:
 *        mvn clean package
 *
 *   2. Compile SeedUsers with the PostgreSQL JDBC driver on the classpath:
 *        javac -cp "digistack-bank-web/target/classes:/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar" \
 *          digistack-bank-web/src/main/java/com/digistack/bank/util/SeedUsers.java \
 *          digistack-bank-web/src/main/java/com/digistack/bank/util/PasswordUtil.java \
 *          -d digistack-bank-web/target/classes
 *
 *   3. Run (from the project root so config/db-local.properties is found):
 *        java -cp "digistack-bank-web/target/classes:/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar" \
 *          com.digistack.bank.util.SeedUsers
 *
 * Expected output:
 *   Connected to digistack_bank on dsb-db.
 *   Updated customer1 with correct password hash.
 *   Updated admin1 with correct password hash.
 *   Seed complete. Both users ready for login.
 */

public class SeedUsers {

    // Credentials are read at runtime from config/db-local.properties.
    // That file is gitignored — credentials never appear in source code.
    //
    // This utility runs on the dev laptop, not inside WAS — it cannot
    // use the JNDI DataSource (jdbc/BankDS) because JNDI requires a
    // running WAS JVM context. Properties file is the correct
    // credential-externalization approach for a standalone Java utility.
    private static final String PROPS_FILE = "config/db-local.properties";

    // Seed credentials — these match the values in V2__create_users.sql
    private static final String CUSTOMER_USERNAME = "customer1";
    private static final String CUSTOMER_PASSWORD = "Customer@123";
    private static final String CUSTOMER_SALT     = "a1b2c3d4e5f6a1b2";

    private static final String ADMIN_USERNAME = "admin1";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String ADMIN_SALT     = "f6e5d4c3b2a1f6e5";

    public static void main(String[] args) throws Exception {

        // ── Load credentials from properties file ──
        // Credentials are never in source code — only in the
        // gitignored config/db-local.properties file.
        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream fis =
                new java.io.FileInputStream(PROPS_FILE)) {
            props.load(fis);
        } catch (java.io.FileNotFoundException e) {
            System.err.println(
                "ERROR: " + PROPS_FILE + " not found.");
            System.err.println(
                "Copy config/db-local.properties.template to " +
                "config/db-local.properties and fill in your " +
                "DB credentials. Run this utility from the " +
                "project root directory.");
            return;
        } catch (java.io.IOException e) {
            System.err.println(
                "ERROR: Could not read " + PROPS_FILE +
                " — " + e.getMessage());
            return;
        }

        String jdbcUrl  = props.getProperty("db.url");
        String jdbcUser = props.getProperty("db.user");
        String jdbcPass = props.getProperty("db.password");

        if (jdbcUrl == null || jdbcUser == null || jdbcPass == null) {
            System.err.println(
                "ERROR: One or more required properties missing " +
                "in " + PROPS_FILE + ".");
            System.err.println(
                "Required keys: db.url, db.user, db.password");
            return;
        }

        // Load PostgreSQL JDBC driver
        Class.forName("org.postgresql.Driver");

        try (Connection conn = DriverManager.getConnection(
                jdbcUrl, jdbcUser, jdbcPass)) {

            System.out.println("Connected to digistack_bank on dsb-db.");

            // Compute correct hashes using the same PasswordUtil
            // that LoginServlet will use — guarantees they match.
            String customerHash = PasswordUtil.hash(
                CUSTOMER_SALT, CUSTOMER_PASSWORD);
            String adminHash = PasswordUtil.hash(
                ADMIN_SALT, ADMIN_PASSWORD);

            // Update customer1
            String sql = "UPDATE users SET password_hash = ? " +
                         "WHERE username = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, customerHash);
                ps.setString(2, CUSTOMER_USERNAME);
                int rows = ps.executeUpdate();
                if (rows == 1) {
                    System.out.println(
                        "Updated customer1 with correct password hash.");
                } else {
                    System.out.println(
                        "WARNING: customer1 not found — " +
                        "did V2 migration run?");
                }
            }

            // Update admin1
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, adminHash);
                ps.setString(2, ADMIN_USERNAME);
                int rows = ps.executeUpdate();
                if (rows == 1) {
                    System.out.println(
                        "Updated admin1 with correct password hash.");
                } else {
                    System.out.println(
                        "WARNING: admin1 not found — " +
                        "did V2 migration run?");
                }
            }

            System.out.println(
                "Seed complete. Both users ready for login.");
        }
    }
}
