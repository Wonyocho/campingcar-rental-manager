package com.rental;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("로그인");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(320, 150);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel label = new JLabel("어느 계정으로 접속하시겠습니까?");
        label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(label, c);

        JButton adminBtn = new JButton("관리자");
        JButton memberBtn = new JButton("회원");

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0;
        panel.add(adminBtn, c);
        c.gridx = 1;
        panel.add(memberBtn, c);

        // 관리자 버튼: 바로 로그인 처리
        adminBtn.addActionListener(e -> {
            try {
                Connection conn = DBConnector.getConnection(true); // root/1234
                if (conn != null) conn.close(); // 연결 성공 확인 후 닫기
                JOptionPane.showMessageDialog(this, "관리자 로그인 성공! (root/1234)");
                new AdminMainFrame().setVisible(true);
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "관리자 DB 연결 실패: " + ex.getMessage());
            }
        });

        // 회원 버튼: ID/비밀번호 입력창 새로 오픈
        memberBtn.addActionListener(e -> {
            new MemberLoginDialog(this);
        });

        setContentPane(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

// 회원 로그인 다이얼로그
class MemberLoginDialog extends JDialog {
    public MemberLoginDialog(JFrame parent) {
        super(parent, "회원 로그인", true);
        setSize(320, 180);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel idLabel = new JLabel("ID:");
        JTextField idField = new JTextField(15);
        JLabel pwLabel = new JLabel("Password:");
        JPasswordField pwField = new JPasswordField(15);

        JButton loginBtn = new JButton("로그인");

        c.insets = new Insets(5,5,5,5);
        c.gridx = 0; c.gridy = 0;
        panel.add(idLabel, c);
        c.gridx = 1;
        panel.add(idField, c);
        c.gridy = 1; c.gridx = 0;
        panel.add(pwLabel, c);
        c.gridx = 1;
        panel.add(pwField, c);
        c.gridy = 2; c.gridx = 0; c.gridwidth = 2;
        panel.add(loginBtn, c);

        // 로그인 버튼
        loginBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword()).trim();
            // 실제 DB의 Customer 테이블에서 로그인 확인
            String licenseNumber = getLicenseNumber(id, pw);
            if (licenseNumber != null) {
                JOptionPane.showMessageDialog(this, "회원 로그인 성공!");
                new MemberMainFrame(licenseNumber).setVisible(true);
                this.dispose();
                parent.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패! 아이디/비밀번호 확인");
            }
        });

        setContentPane(panel);
        setVisible(true);
    }

    // 실제 DB에서 로그인 체크
    private String getLicenseNumber(String loginId, String password) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnector.getConnection(false);
            String sql = "SELECT license_number FROM Customer WHERE login_id = ? AND password = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, loginId);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("license_number");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
        return null;
    }
}