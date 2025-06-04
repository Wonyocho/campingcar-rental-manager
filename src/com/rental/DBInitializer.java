package com.rental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class DBInitializer {
    public static void main(String[] args) {
        initializeDatabase();
    }

    public static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // JDBC 드라이버 로드
            String jdbcUrl = "jdbc:mysql://localhost:3306/?allowMultiQueries=true";
            String user = "root";
            String password = "1234";
            conn = DriverManager.getConnection(jdbcUrl, user, password);
            System.out.println("DB 연결 완료");

            // SQL 파일 읽기 (UTF-8 인코딩)
            String sqlFilePath = "init.sql";
            String sql = new String(Files.readAllBytes(Paths.get(sqlFilePath)), "UTF-8");

            // 세미콜론(;) 단위로 구문 분리 후 한 개씩 실행
            String[] queries = sql.split("(?m);\\s*\\n"); // 세미콜론+줄바꿈 기준
            for (String rawQuery : queries) {
                String query = rawQuery.trim();
                if (query.isEmpty()) continue;
                try (Statement st = conn.createStatement()) {
                    st.execute(query); // 한 구문씩 실행
                } catch (SQLException e) {
                    // 이미 있는 객체/권한/데이터 관련 오류 등은 무시(초기화 특성)
                    System.err.println("SQL 오류 [" + query.replace("\n"," ") + "]\n" + e.getMessage());
                }
            }
            System.out.println("DB 초기화가 완료되었습니다!");

        } catch (ClassNotFoundException e) {
            System.out.println("JDBC 드라이버 로드 오류");
        } catch (SQLException e) {
            System.out.println("SQL 실행 오류: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("SQL 파일 읽기 오류: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }
}
