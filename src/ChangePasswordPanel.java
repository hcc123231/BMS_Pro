import com.mysql.cj.exceptions.StreamingNotifiable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

public class ChangePasswordPanel extends JPanel {

    private int currentUserId;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private SqlQuery m_query;

    public ChangePasswordPanel(SqlQuery query, int userId) {
        m_query = query;
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initComponents();
    }

    private void initComponents() {
        // 标题
        JLabel titleLabel = new JLabel("修改密码");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 旧密码
        JLabel oldPasswordLabel = new JLabel("当前密码:");
        oldPasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(oldPasswordLabel, gbc);

        oldPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(oldPasswordField, gbc);

        // 新密码
        JLabel newPasswordLabel = new JLabel("新密码:");
        newPasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(newPasswordLabel, gbc);

        newPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(newPasswordField, gbc);

        // 确认新密码
        JLabel confirmPasswordLabel = new JLabel("确认新密码:");
        confirmPasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(confirmPasswordField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("保存");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(49, 130, 206));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(170, 170, 170));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFields();
            }
        });
        buttonPanel.add(cancelButton);

        // 添加到主面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        add(centerPanel, BorderLayout.CENTER);
    }

    private void changePassword() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // 验证输入
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有字段都必须填写", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的新密码不一致", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "新密码长度至少为6位", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 验证旧密码
        try {
            if (!validateOldPassword(oldPassword)) {
                JOptionPane.showMessageDialog(this, "当前密码不正确", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 更新密码
            updatePassword(newPassword);
            JOptionPane.showMessageDialog(this, "密码修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            resetFields();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "密码修改失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateOldPassword(String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE account = ? AND password = ?";
        m_query.mysqlConnect();
        ResultSet rset = m_query.selectQuery(3, new String[]{sql, String.valueOf(currentUserId), password});
        rset.next();
        return rset.getInt(1) > 0;

    }

    private void updatePassword(String newPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE account = ?";
        m_query.mysqlConnect();
        int affectedRows = m_query.updateQuery(3, new String[]{sql, newPassword, String.valueOf(currentUserId)});


    }

    private void resetFields() {
        oldPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }
}
