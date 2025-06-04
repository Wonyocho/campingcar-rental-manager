package com.rental;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AdminMainFrame extends JFrame {
    Connection conn; // root 계정으로 연결
    Statement stmt;

    public AdminMainFrame() {
        setTitle("관리자 메인화면");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // DB 연결 (root 계정)
        try {
            conn = DBConnector.getConnection(true);
            stmt = conn.createStatement();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 연결 실패: " + ex.getMessage());
            System.exit(1);
        }

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. DB 초기화 탭
        JPanel dbInitPanel = new JPanel();
        JButton resetBtn = new JButton("DB 초기화");
        dbInitPanel.add(resetBtn);
        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "DB를 초기화 하시겠습니까?", "초기화", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DBInitializer.initializeDatabase();
                JOptionPane.showMessageDialog(this, "초기화 후 재시작 해주세요!");
            }
        });
        tabbedPane.addTab("DB초기화", dbInitPanel);

        // 2. 전체 테이블 조회 탭
        JPanel viewPanel = new JPanel(new BorderLayout());
        JComboBox<String> tableCombo = new JComboBox<>();
        JTable table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        JButton refreshBtn = new JButton("조회");

        // 테이블 이름 불러오기
        try {
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            while (rs.next()) {
                tableCombo.addItem(rs.getString(1));
            }
            rs.close();
        } catch (Exception ex) {
            tableCombo.addItem("테이블없음");
        }
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("테이블:"));
        topPanel.add(tableCombo);
        topPanel.add(refreshBtn);

        viewPanel.add(topPanel, BorderLayout.NORTH);
        viewPanel.add(scroll, BorderLayout.CENTER);
        
        refreshBtn.addActionListener(e -> {
            String tablename = (String) tableCombo.getSelectedItem();
            if (tablename == null) return;
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + tablename);
                table.setModel(buildTableModel(rs));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "조회 실패: " + ex.getMessage());
            }
        });

        tabbedPane.addTab("테이블조회", viewPanel);

     // 3. CRUD 탭 (입력/삭제/수정 모두 필드 크기 통일)
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new BoxLayout(crudPanel, BoxLayout.Y_AXIS));

        // 입력: SQL 직접 입력 (INSERT문)
        JPanel insertPanel = new JPanel();
        insertPanel.setLayout(new BoxLayout(insertPanel, BoxLayout.Y_AXIS));
        JTextField insertField = new JTextField(50);
        insertField.setPreferredSize(new Dimension(800, 60)); // 가로 800, 세로 60
        insertField.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        JPanel insertFieldPanel = new JPanel();
        insertFieldPanel.setLayout(new BoxLayout(insertFieldPanel, BoxLayout.Y_AXIS));
        insertFieldPanel.add(insertField);
        JPanel insertBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton insertBtn = new JButton("입력(INSERT 실행)");
        JButton cancelInsertBtn = new JButton("취소");
        insertBtnPanel.add(insertBtn);
        insertBtnPanel.add(cancelInsertBtn);
        insertPanel.add(insertFieldPanel);
        insertPanel.add(insertBtnPanel);
        crudPanel.add(insertPanel);

        insertBtn.addActionListener(e -> {
            String sql = insertField.getText();
            try {
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "입력 성공!");
                insertField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "입력 오류: " + ex.getMessage());
            }
        });
        cancelInsertBtn.addActionListener(e -> insertField.setText(""));

        // 삭제: 조건식 입력 (크기 통일)
        JPanel deletePanel = new JPanel();
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));
        JTextField deleteTableField = new JTextField("테이블명(예: CarRental)", 50);
        deleteTableField.setPreferredSize(new Dimension(800, 60));
        deleteTableField.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        JTextField deleteCondField = new JTextField("조건식(예: license_number='98서울123456' AND duration_days > 3)", 50);
        deleteCondField.setPreferredSize(new Dimension(800, 60));
        deleteCondField.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        JPanel deleteFieldPanel = new JPanel();
        deleteFieldPanel.setLayout(new BoxLayout(deleteFieldPanel, BoxLayout.Y_AXIS));
        deleteFieldPanel.add(deleteTableField);
        deleteFieldPanel.add(Box.createVerticalStrut(5));
        deleteFieldPanel.add(deleteCondField);
        JPanel deleteBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton deleteBtn = new JButton("삭제");
        JButton cancelDeleteBtn = new JButton("취소"); // 취소 버튼 추가
        deleteBtnPanel.add(deleteBtn);
        deleteBtnPanel.add(cancelDeleteBtn); // 취소 버튼 패널에 추가
        deletePanel.add(deleteFieldPanel);
        deletePanel.add(deleteBtnPanel);
        crudPanel.add(deletePanel);

        deleteBtn.addActionListener(e -> {
            String t = deleteTableField.getText();
            String cond = deleteCondField.getText();
            try {
                String sql = "DELETE FROM " + t + " WHERE " + cond;
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "삭제 성공!");
                deleteTableField.setText("테이블명(예: CarRental)");
                deleteCondField.setText("조건식(예: license_number='98서울123456' AND duration_days > 3)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage());
            }
        });
        cancelDeleteBtn.addActionListener(e -> {
            deleteTableField.setText("예: CarRental");
            deleteCondField.setText("조건식(예: license_number='98서울123456' AND duration_days > 3)");
        });


        // 수정: SET/조건식 입력 (크기 통일)
        JPanel updatePanel = new JPanel();
        updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
        JTextField updateTableField = new JTextField("테이블명(예: Car)", 50);
        updateTableField.setPreferredSize(new Dimension(800, 60));
        updateTableField.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        JTextField updateSetField = new JTextField("SET(예:daily_price=50000)", 50);
        updateSetField.setPreferredSize(new Dimension(800, 60));
        updateSetField.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        JTextField updateCondField = new JTextField("조건식(예: car_id=1)", 50);
        updateCondField.setPreferredSize(new Dimension(800, 60));
        updateCondField.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        JPanel updateFieldPanel = new JPanel();
        updateFieldPanel.setLayout(new BoxLayout(updateFieldPanel, BoxLayout.Y_AXIS));
        updateFieldPanel.add(updateTableField);
        updateFieldPanel.add(Box.createVerticalStrut(5));
        updateFieldPanel.add(updateSetField);
        updateFieldPanel.add(Box.createVerticalStrut(5));
        updateFieldPanel.add(updateCondField);
        JPanel updateBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton updateBtn = new JButton("수정");
        JButton cancelUpdateBtn = new JButton("취소"); // 취소 버튼 추가
        updateBtnPanel.add(updateBtn);
        updateBtnPanel.add(cancelUpdateBtn); // 취소 버튼 패널에 추가
        updatePanel.add(updateFieldPanel);
        updatePanel.add(updateBtnPanel);
        crudPanel.add(updatePanel);

        updateBtn.addActionListener(e -> {
            String t = updateTableField.getText();
            String set = updateSetField.getText();
            String cond = updateCondField.getText();
            try {
                String sql = "UPDATE " + t + " SET " + set + " WHERE " + cond;
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "수정 성공!");
                updateTableField.setText("테이블명(예: Car)");
                updateSetField.setText("SET(예:daily_price=50000)");
                updateCondField.setText("조건식(예: car_id=1)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "수정 오류: " + ex.getMessage());
            }
        });
        cancelUpdateBtn.addActionListener(e -> {
            updateTableField.setText("테이블명(예: Car)");
            updateSetField.setText("SET(예:daily_price=50000)");
            updateCondField.setText("조건식(예: car_id=1)");
        });


        tabbedPane.addTab("테이블관리(CRUD)", crudPanel);



        // 4. 정비정보탭 (기본만 예시)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JComboBox<String> carCombo = new JComboBox<>();
        topRow.add(new JLabel("캠핑카 선택:"));
        topRow.add(carCombo);
        infoPanel.add(topRow, BorderLayout.NORTH);

        // 내부정비 테이블
        JTable internalTable = new JTable();
        JScrollPane internalScroll = new JScrollPane(internalTable);

        // 외부정비 테이블
        JTable externalTable = new JTable();
        JScrollPane externalScroll = new JScrollPane(externalTable);

        // 하단 상세: 부품/정비소
        JPanel bottomPanel = new JPanel(new GridLayout(1,2,10,0));
        JTable partDetailTable = new JTable();
        JTable shopDetailTable = new JTable();
        bottomPanel.add(new JScrollPane(partDetailTable));
        bottomPanel.add(new JScrollPane(shopDetailTable));

        // 중간에 내부/외부 정비 테이블
        JPanel middlePanel = new JPanel(new GridLayout(2,1,0,10));
        middlePanel.add(internalScroll);
        middlePanel.add(externalScroll);

        infoPanel.add(middlePanel, BorderLayout.CENTER);
        infoPanel.add(bottomPanel, BorderLayout.SOUTH);

        // 캠핑카 목록 로딩
        try {
            ResultSet rs = stmt.executeQuery("SELECT car_id, name FROM Car ORDER BY car_id");
            while (rs.next()) {
                carCombo.addItem(rs.getInt("car_id") + " - " + rs.getString("name"));
            }
            rs.close();
        } catch (Exception ex) {}

        // 캠핑카 선택시 해당 정비 내역 조회
        carCombo.addActionListener(e -> {
            if (carCombo.getSelectedItem() == null) return;
            int carId = Integer.parseInt(carCombo.getSelectedItem().toString().split(" - ")[0]);
            // 내부정비 (Maintenance)
            try {
                ResultSet rs = stmt.executeQuery(
                    "SELECT m.maintenance_id, m.date, m.duration_min, p.part_id, p.part_name, e.name AS employee " +
                    "FROM Maintenance m JOIN PartInventory p ON m.part_id=p.part_id " +
                    "JOIN Employee e ON m.employee_id = e.employee_id " +
                    "WHERE m.car_id = " + carId);
                internalTable.setModel(buildTableModel(rs));
            } catch (Exception ex2) {}
            // 외부정비 (ExternalRepair)
            try {
                ResultSet rs = stmt.executeQuery(
                    "SELECT er.repair_id, er.repair_date, er.repair_price, s.shop_id, s.name AS shop, er.detail " +
                    "FROM ExternalRepair er JOIN RepairShop s ON er.shop_id=s.shop_id " +
                    "WHERE er.car_id = " + carId);
                externalTable.setModel(buildTableModel(rs));
            } catch (Exception ex3) {}
            // 상세테이블 초기화
            partDetailTable.setModel(new DefaultTableModel());
            shopDetailTable.setModel(new DefaultTableModel());
        });

        // 내부정비에서 부품 선택시 상세정보
        internalTable.getSelectionModel().addListSelectionListener(e -> {
            int row = internalTable.getSelectedRow();
            if (row < 0) return;
            Object partIdObj = internalTable.getValueAt(row, 3); // part_id
            if (partIdObj == null) return;
            int partId = Integer.parseInt(partIdObj.toString());
            try {
                ResultSet rs = stmt.executeQuery(
                    "SELECT part_id, part_name, price, quantity, supplier_name FROM PartInventory WHERE part_id=" + partId);
                partDetailTable.setModel(buildTableModel(rs));
            } catch (Exception ex2) {}
        });

        // 외부정비에서 정비소 선택시 상세정보
        externalTable.getSelectionModel().addListSelectionListener(e -> {
            int row = externalTable.getSelectedRow();
            if (row < 0) return;
            Object shopIdObj = externalTable.getValueAt(row, 3); // shop_id
            if (shopIdObj == null) return;
            int shopId = Integer.parseInt(shopIdObj.toString());
            try {
                ResultSet rs = stmt.executeQuery(
                    "SELECT shop_id, name, address, phone, manager_name, manager_email FROM RepairShop WHERE shop_id=" + shopId);
                shopDetailTable.setModel(buildTableModel(rs));
            } catch (Exception ex2) {}
        });

        tabbedPane.addTab("정비정보", infoPanel);

        // 5. 임의SQL탭
        JPanel sqlPanel = new JPanel(new BorderLayout());
        JTextArea sqlInput = new JTextArea("SELECT * FROM Car;", 4, 60);
        JButton execBtn = new JButton("실행");
        JTable sqlTable = new JTable();
        JScrollPane sqlScroll = new JScrollPane(sqlTable);

        JPanel sqlTop = new JPanel();
        sqlTop.add(new JLabel("SELECT 질의:"));
        sqlTop.add(new JScrollPane(sqlInput));
        sqlTop.add(execBtn);

        sqlPanel.add(sqlTop, BorderLayout.NORTH);
        sqlPanel.add(sqlScroll, BorderLayout.CENTER);

        execBtn.addActionListener(e -> {
            String query = sqlInput.getText();
            try {
                ResultSet rs = stmt.executeQuery(query);
                sqlTable.setModel(buildTableModel(rs));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "SQL 실행 오류: " + ex.getMessage());
            }
        });

        tabbedPane.addTab("임의SQL", sqlPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    // JTable에 ResultSet을 매핑해주는 유틸 (기본)
    public static DefaultTableModel buildTableModel(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        // 컬럼명 추출
        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(meta.getColumnName(i));
        }

        // 데이터 추출
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }
        return new DefaultTableModel(data, columnNames);
    }
}
