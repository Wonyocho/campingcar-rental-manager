package com.rental;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MemberMainFrame extends JFrame {
    private String licenseNumber; // 로그인한 회원의 면허번호

    public MemberMainFrame(String licenseNumber) {
        this.licenseNumber = licenseNumber;
        setTitle("회원 메인화면");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 1, 10, 10));

        JButton btnViewCampers = new JButton("캠핑카 조회");
        JButton btnViewAvailableDates = new JButton("대여 가능 일자 조회");
        JButton btnRegisterRental = new JButton("대여 등록");
        JButton btnViewMyRentals = new JButton("내 대여 정보 조회");
        JButton btnDeleteRental = new JButton("대여 정보 삭제");
        JButton btnChangeCamper = new JButton("대여 캠핑카 변경");
        JButton btnChangeDate = new JButton("대여 일정 변경");
        JButton btnRequestMaintenance = new JButton("정비 의뢰");

        btnViewCampers.addActionListener(e -> viewCampers());
        btnViewAvailableDates.addActionListener(e -> viewAvailableDates());
        btnRegisterRental.addActionListener(e -> registerRental());
        btnViewMyRentals.addActionListener(e -> viewMyRentals());
        btnDeleteRental.addActionListener(e -> deleteRental());
        btnChangeCamper.addActionListener(e -> changeCamper());
        btnChangeDate.addActionListener(e -> changeDate());
        btnRequestMaintenance.addActionListener(e -> requestMaintenance());

        panel.add(btnViewCampers);
        panel.add(btnViewAvailableDates);
        panel.add(btnRegisterRental);
        panel.add(btnViewMyRentals);
        panel.add(btnDeleteRental);
        panel.add(btnChangeCamper);
        panel.add(btnChangeDate);
        panel.add(btnRequestMaintenance);

        add(panel);
    }
  
    private void viewCampers() {
        // 캠핑카 조회 기능 구현
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false); // 일반회원 계정
            String sql = "SELECT car_id, name, plate_number, capacity, daily_price FROM Car";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
    
            // ResultSet을 JTable로 변환
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            String[] colNames = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                colNames[i] = meta.getColumnName(i + 1);
            }
    
            java.util.Vector<String[]> data = new java.util.Vector<>();
            while (rs.next()) {
                String[] row = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                data.add(row);
            }
    
            String[][] arr = data.toArray(new String[0][]);
            JTable table = new JTable(arr, colNames);
            JScrollPane scroll = new JScrollPane(table);
    
            JDialog dialog = new JDialog(this, "캠핑카 목록", true);
            dialog.setSize(700, 300);
            dialog.setLocationRelativeTo(this);
            dialog.add(scroll);
            dialog.setVisible(true);
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void viewAvailableDates() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);
            String sql = "SELECT car_id, name FROM Car";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
    
            java.util.Vector<String> camperList = new java.util.Vector<>();
            java.util.Vector<Integer> camperIdList = new java.util.Vector<>();
            while (rs.next()) {
                camperIdList.add(rs.getInt("car_id"));
                camperList.add(rs.getString("name") + " (ID:" + rs.getInt("car_id") + ")");
            }
            rs.close();
            ps.close();
    
            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "대여 가능 일자를 조회할 캠핑카를 선택하세요.",
                    "캠핑카 선택",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    camperList.toArray(),
                    camperList.size() > 0 ? camperList.get(0) : null
            );
            if (selected == null) return;
    
            int idx = camperList.indexOf(selected);
            int carId = camperIdList.get(idx);
    
            sql = "SELECT start_date, duration_days FROM CarRental WHERE car_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, carId);
            rs = ps.executeQuery();
    
            StringBuilder sb = new StringBuilder();
            sb.append("이미 대여된 기간(시작일 ~ 종료일):\n");
            boolean hasRental = false;
            while (rs.next()) {
                hasRental = true;
                java.sql.Date start = rs.getDate("start_date");
                int days = rs.getInt("duration_days");
                java.sql.Date end = new java.sql.Date(start.getTime() + (days - 1) * 24L * 60 * 60 * 1000);
                sb.append(start).append(" ~ ").append(end).append("\n");
            }
            if (!hasRental) {
                sb.append("현재 대여된 기간이 없습니다. 모든 날짜가 예약 가능합니다.");
            }
    
            JOptionPane.showMessageDialog(this, sb.toString(), "대여 가능 일자", JOptionPane.INFORMATION_MESSAGE);
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void registerRental() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);

            // 1. 캠핑카 목록 불러오기
            String sql = "SELECT car_id, name FROM Car";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            java.util.Vector<String> camperList = new java.util.Vector<>();
            java.util.Vector<Integer> camperIdList = new java.util.Vector<>();
            while (rs.next()) {
                camperIdList.add(rs.getInt("car_id"));
                camperList.add(rs.getString("name") + " (ID:" + rs.getInt("car_id") + ")");
            }
            rs.close();
            ps.close();

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "대여할 캠핑카를 선택하세요.",
                    "캠핑카 선택",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    camperList.toArray(),
                    camperList.size() > 0 ? camperList.get(0) : null
            );
            if (selected == null) return;
            int idx = camperList.indexOf(selected);
            int carId = camperIdList.get(idx);

            // 2. 대여 시작일, 기간 입력
            String startDate = JOptionPane.showInputDialog(this, "대여 시작일을 입력하세요 (YYYY-MM-DD):");
            if (startDate == null || startDate.trim().isEmpty()) return;
            String durationStr = JOptionPane.showInputDialog(this, "대여 기간(일수)을 입력하세요:");
            if (durationStr == null || durationStr.trim().isEmpty()) return;
            int duration = Integer.parseInt(durationStr);

            // 3. 캠핑카 회사 ID 및 가격 조회
            sql = "SELECT company_id, daily_price FROM Car WHERE car_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, carId);
            rs = ps.executeQuery();
            int companyId = 0;
            int dailyPrice = 0;
            if (rs.next()) {
                companyId = rs.getInt("company_id");
                dailyPrice = rs.getInt("daily_price");
            }
            rs.close();
            ps.close();

            // 4. 총 가격 계산
            int totalPrice = dailyPrice * duration;

            // 5. DB에 대여 등록
            sql = "INSERT INTO CarRental (car_id, license_number, company_id, start_date, duration_days, total_price, pay_due_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, carId);
            ps.setString(2, licenseNumber); // 동적으로 전달받은 값 사용
            ps.setInt(3, companyId);
            ps.setString(4, startDate);
            ps.setInt(5, duration);
            ps.setInt(6, totalPrice);
            ps.setString(7, startDate); // pay_due_date는 시작일과 동일하게 처리
            int result = ps.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "대여 등록이 완료되었습니다!");
            } else {
                JOptionPane.showMessageDialog(this, "대여 등록에 실패했습니다.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void viewMyRentals() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);
            String sql = "SELECT r.rental_id, c.name AS car_name, c.plate_number, r.start_date, r.duration_days, r.total_price " +
                         "FROM CarRental r JOIN Car c ON r.car_id = c.car_id " +
                         "WHERE r.license_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, licenseNumber);
            rs = ps.executeQuery();
    
            // ResultSet을 JTable로 변환
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            String[] colNames = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                colNames[i] = meta.getColumnName(i + 1);
            }
    
            java.util.Vector<String[]> data = new java.util.Vector<>();
            while (rs.next()) {
                String[] row = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                data.add(row);
            }
    
            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(this, "대여 내역이 없습니다.");
                return;
            }
    
            String[][] arr = data.toArray(new String[0][]);
            JTable table = new JTable(arr, colNames);
            JScrollPane scroll = new JScrollPane(table);
    
            JDialog dialog = new JDialog(this, "내 대여 정보", true);
            dialog.setSize(700, 300);
            dialog.setLocationRelativeTo(this);
            dialog.add(scroll);
            dialog.setVisible(true);
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void deleteRental() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);
            // 1. 본인 대여 목록 조회
            String sql = "SELECT r.rental_id, c.name AS car_name, r.start_date, r.duration_days " +
                         "FROM CarRental r JOIN Car c ON r.car_id = c.car_id " +
                         "WHERE r.license_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, licenseNumber);
            rs = ps.executeQuery();
    
            java.util.Vector<String> rentalList = new java.util.Vector<>();
            java.util.Vector<Integer> rentalIdList = new java.util.Vector<>();
            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                String info = "ID:" + rentalId + " | " + rs.getString("car_name") +
                              " | 시작일:" + rs.getString("start_date") +
                              " | 기간:" + rs.getInt("duration_days") + "일";
                rentalList.add(info);
                rentalIdList.add(rentalId);
            }
            rs.close();
            ps.close();
    
            if (rentalList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "삭제할 대여 정보가 없습니다.");
                return;
            }
    
            // 2. 삭제할 대여 선택
            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "삭제할 대여를 선택하세요.",
                    "대여 정보 삭제",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    rentalList.toArray(),
                    rentalList.get(0)
            );
            if (selected == null) return;
            int idx = rentalList.indexOf(selected);
            int rentalId = rentalIdList.get(idx);
    
            // 3. 삭제 실행
            sql = "DELETE FROM CarRental WHERE rental_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, rentalId);
            int result = ps.executeUpdate();
    
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "대여 정보가 삭제되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "삭제에 실패했습니다.");
            }
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void changeCamper() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);
    
            // 1. 본인 대여 목록 조회
            String sql = "SELECT r.rental_id, c.name AS car_name, r.start_date, r.duration_days " +
                         "FROM CarRental r JOIN Car c ON r.car_id = c.car_id " +
                         "WHERE r.license_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, licenseNumber);
            rs = ps.executeQuery();
    
            java.util.Vector<String> rentalList = new java.util.Vector<>();
            java.util.Vector<Integer> rentalIdList = new java.util.Vector<>();
            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                String info = "ID:" + rentalId + " | " + rs.getString("car_name") +
                              " | 시작일:" + rs.getString("start_date") +
                              " | 기간:" + rs.getInt("duration_days") + "일";
                rentalList.add(info);
                rentalIdList.add(rentalId);
            }
            rs.close();
            ps.close();
    
            if (rentalList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "변경할 대여 정보가 없습니다.");
                return;
            }
    
            // 2. 변경할 대여 선택
            String selectedRental = (String) JOptionPane.showInputDialog(
                    this,
                    "캠핑카를 변경할 대여를 선택하세요.",
                    "대여 선택",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    rentalList.toArray(),
                    rentalList.get(0)
            );
            if (selectedRental == null) return;
            int rentalIdx = rentalList.indexOf(selectedRental);
            int rentalId = rentalIdList.get(rentalIdx);
    
            // 3. 캠핑카 목록 불러오기
            sql = "SELECT car_id, name FROM Car";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            java.util.Vector<String> camperList = new java.util.Vector<>();
            java.util.Vector<Integer> camperIdList = new java.util.Vector<>();
            while (rs.next()) {
                camperIdList.add(rs.getInt("car_id"));
                camperList.add(rs.getString("name") + " (ID:" + rs.getInt("car_id") + ")");
            }
            rs.close();
            ps.close();
    
            String selectedCamper = (String) JOptionPane.showInputDialog(
                    this,
                    "변경할 캠핑카를 선택하세요.",
                    "캠핑카 선택",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    camperList.toArray(),
                    camperList.get(0)
            );
            if (selectedCamper == null) return;
            int camperIdx = camperList.indexOf(selectedCamper);
            int newCarId = camperIdList.get(camperIdx);
    
            // 4. 캠핑카 변경 실행
            sql = "UPDATE CarRental SET car_id = ? WHERE rental_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, newCarId);
            ps.setInt(2, rentalId);
            int result = ps.executeUpdate();
    
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "캠핑카가 성공적으로 변경되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "캠핑카 변경에 실패했습니다.");
            }
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void changeDate() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);
    
            // 1. 본인 대여 목록 조회
            String sql = "SELECT r.rental_id, c.name AS car_name, r.start_date, r.duration_days " +
                         "FROM CarRental r JOIN Car c ON r.car_id = c.car_id " +
                         "WHERE r.license_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, licenseNumber);
            rs = ps.executeQuery();
    
            java.util.Vector<String> rentalList = new java.util.Vector<>();
            java.util.Vector<Integer> rentalIdList = new java.util.Vector<>();
            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                String info = "ID:" + rentalId + " | " + rs.getString("car_name") +
                              " | 시작일:" + rs.getString("start_date") +
                              " | 기간:" + rs.getInt("duration_days") + "일";
                rentalList.add(info);
                rentalIdList.add(rentalId);
            }
            rs.close();
            ps.close();
    
            if (rentalList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "변경할 대여 정보가 없습니다.");
                return;
            }
    
            // 2. 변경할 대여 선택
            String selectedRental = (String) JOptionPane.showInputDialog(
                    this,
                    "일정을 변경할 대여를 선택하세요.",
                    "대여 선택",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    rentalList.toArray(),
                    rentalList.get(0)
            );
            if (selectedRental == null) return;
            int rentalIdx = rentalList.indexOf(selectedRental);
            int rentalId = rentalIdList.get(rentalIdx);
    
            // 3. 새 시작일, 새 기간 입력
            String newStartDate = JOptionPane.showInputDialog(this, "새 대여 시작일을 입력하세요 (YYYY-MM-DD):");
            if (newStartDate == null || newStartDate.trim().isEmpty()) return;
            String newDurationStr = JOptionPane.showInputDialog(this, "새 대여 기간(일수)을 입력하세요:");
            if (newDurationStr == null || newDurationStr.trim().isEmpty()) return;
            int newDuration = Integer.parseInt(newDurationStr);
    
            // 4. 일정 변경 실행
            sql = "UPDATE CarRental SET start_date = ?, duration_days = ? WHERE rental_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStartDate);
            ps.setInt(2, newDuration);
            ps.setInt(3, rentalId);
            int result = ps.executeUpdate();
    
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "대여 일정이 성공적으로 변경되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "대여 일정 변경에 실패했습니다.");
            }
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    private void requestMaintenance() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);

            // 1. 본인 대여 목록 조회
            String sql = "SELECT r.rental_id, c.name AS car_name, r.start_date, r.duration_days " +
                        "FROM CarRental r JOIN Car c ON r.car_id = c.car_id " +
                        "WHERE r.license_number = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, licenseNumber);
            rs = ps.executeQuery();

            java.util.Vector<String> rentalList = new java.util.Vector<>();
            java.util.Vector<Integer> rentalIdList = new java.util.Vector<>();
            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                String info = "ID:" + rentalId + " | " + rs.getString("car_name") +
                            " | 시작일:" + rs.getString("start_date") +
                            " | 기간:" + rs.getInt("duration_days") + "일";
                rentalList.add(info);
                rentalIdList.add(rentalId);
            }
            rs.close();
            ps.close();

            if (rentalList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "정비를 의뢰할 대여 정보가 없습니다.");
                return;
            }

            // 2. 정비 의뢰할 대여 선택
            String selectedRental = (String) JOptionPane.showInputDialog(
                    this,
                    "정비를 의뢰할 대여를 선택하세요.",
                    "정비 의뢰",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    rentalList.toArray(),
                    rentalList.get(0)
            );
            if (selectedRental == null) return;
            int rentalIdx = rentalList.indexOf(selectedRental);
            int rentalId = rentalIdList.get(rentalIdx);

            // 3. 정비 의뢰 내용 입력
            String content = JOptionPane.showInputDialog(this, "정비 요청 내용을 입력하세요:");
            if (content == null || content.trim().isEmpty()) return;

            // 4. 정비 의뢰 등록 (Maintenance 테이블에 insert)
            sql = "INSERT INTO Maintenance (rental_id, request_content, request_date, status) VALUES (?, ?, NOW(), '요청')";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, rentalId);
            ps.setString(2, content);
            int result = ps.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "정비 의뢰가 정상적으로 등록되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "정비 의뢰 등록에 실패했습니다.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }
}