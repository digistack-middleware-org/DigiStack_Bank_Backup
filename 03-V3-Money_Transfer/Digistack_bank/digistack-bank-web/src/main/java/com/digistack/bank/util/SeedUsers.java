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
 *   1. Build the project so PasswordUtil is compiled:
 *        mvn clean package
 *
 *   2. Compile SeedUsers with the PostgreSQL JDBC driver on the classpath:
 *        javac -cp "digistack-bank-web/target/classes:/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar" \
 *          digistack-bank-web/src/main/java/com/digistack/bank/util/SeedUsers.java \
 *          digistack-bank-web/src/main/java/com/digistack/bank/util/PasswordUtil.java \
 *          -d digistack-bank-web/target/classes
 *
 *   3. Run:
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

    // Direct JDBC — this is a local admin utility, not a WAS-deployed class.
    // It runs on your Windows laptop and connects to dsb-db directly.
    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    // Seed credentials — these match the values in V2__create_users.sql
    private static final String CUSTOMER_USERNAME = "customer1";
    private static final String CUSTOMER_PASSWORD = "Customer@123";
    private static final String CUSTOMER_SALT     = "a1b2c3d4e5f6a1b2";

    private static final String ADMIN_USERNAME = "admin1";
    private static final String ADMIN_PASSWORD = "Admin@123";
    private static final String ADMIN_SALT     = "f6e5d4c3b2a1f6e5";

    public static void main(String[] args) throws Exception {

        // Load PostgreSQL JDBC driver
        Class.forName("org.postgresql.Driver");

        try (Connection conn = DriverManager.getConnection(
                JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {

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
