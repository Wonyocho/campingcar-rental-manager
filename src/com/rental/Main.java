package com.rental;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. GUI Look&Feel 설정 (선택)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored){}

        // 2. DB 초기화 기능을 수동/자동으로 실행 (테스트용, 필요시만 활성화)
         DBInitializer.initializeDatabase();  // 주석 해제 시 DB 초기화 실행

        // 3. 로그인 화면 실행
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
