import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class RegistrationForm extends JFrame {
    private JTextField usernameField, emailField, studentIdField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton, resetButton;
    private JLabel messageLabel;

    public RegistrationForm() {
        // 设置窗口标题
        setTitle("校园图书管理系统 - 注册");
        // 设置窗口大小
        setSize(500, 450);
        // 设置窗口关闭操作
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口居中显示
        setLocationRelativeTo(null);
        // 设置窗口布局
        setLayout(new BorderLayout());

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 添加标题
        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 用户名
        formPanel.add(new JLabel("用户名:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        // 密码
        formPanel.add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        // 确认密码
        formPanel.add(new JLabel("确认密码:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        // 邮箱
        formPanel.add(new JLabel("邮箱:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        // 学号/工号
        formPanel.add(new JLabel("学号/工号:"));
        studentIdField = new JTextField();
        formPanel.add(studentIdField);

        // 用户角色
        formPanel.add(new JLabel("用户角色:"));
        String[] roles = {"学生", "教师", "管理员"};
        roleComboBox = new JComboBox<>(roles);
        formPanel.add(roleComboBox);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 消息标签
        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        registerButton = new JButton("注册");
        registerButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(registerButton);

        resetButton = new JButton("重置");
        resetButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(resetButton);

        mainPanel.add(buttonPanel);

        // 添加主面板到窗口
        add(mainPanel, BorderLayout.CENTER);

        // 注册按钮事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        // 重置按钮事件
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // 验证输入
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || studentId.isEmpty()) {
            messageLabel.setText("请填写所有必填字段");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("两次输入的密码不一致");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            messageLabel.setText("请输入有效的邮箱地址");
            return;
        }

        // 连接数据库
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bms_db", "root", "2028915986hcc")) {

            // 检查用户名是否已存在
            String checkQuery = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        messageLabel.setText("用户名已存在");
                        return;
                    }
                }
            }

            // 插入用户数据
            String insertQuery = "INSERT INTO users (username, password, email, student_id, role) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, email);
                pstmt.setString(4, studentId);
                pstmt.setString(5, role);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    messageLabel.setText("注册成功！请登录");
                    // 注册成功后可以延迟关闭窗口或跳转到登录页面
                    Timer timer = new Timer(2000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dispose();
                            // 这里可以添加跳转到登录页面的代码
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    messageLabel.setText("注册失败，请重试");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            messageLabel.setText("数据库错误，请联系管理员");
        }
    }

    private void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        emailField.setText("");
        studentIdField.setText("");
        messageLabel.setText("");
    }

    public static void main(String[] args) {
        // 在事件调度线程上创建和显示GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RegistrationForm form = new RegistrationForm();
                form.setVisible(true);
            }
        });
    }
}    