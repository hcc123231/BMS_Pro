import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;

public class UserInfoPanel extends JPanel {
    private Connection conn;
    private String currentUsername;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // 保存需要动态更新的组件为成员变量
    private JLabel usernameLabel;
    private JLabel registerDateLabel;
    private JLabel currentBorrowLabel;
    private JLabel overdueCountLabel;
    private DefaultTableModel borrowTableModel;

    public UserInfoPanel(Connection conn, String username) {
        this.conn = conn;
        this.currentUsername = username;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        // 加大整体边距，让内容不贴着边缘
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        initializeComponents();
        loadUserInfo();
    }

    private void initializeComponents() {
        // 标题
        JLabel titleLabel = new JLabel("个人信息", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 32));
        titleLabel.setForeground(new Color(34, 34, 34));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 信息面板，用圆角边框和浅灰色背景突出
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(new Color(247, 247, 247));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 228, 228), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // 用户名
        gbc.gridy = 0;
        JLabel usernameDescLabel = new JLabel("用户名:", SwingConstants.RIGHT);
        usernameDescLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        usernameDescLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(usernameDescLabel, gbc);
        usernameLabel = new JLabel("", SwingConstants.LEFT);
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(34, 34, 34));
        gbc.gridx = 1;
        infoPanel.add(usernameLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // 注册日期
        JLabel registerDateDescLabel = new JLabel("注册日期:", SwingConstants.RIGHT);
        registerDateDescLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        registerDateDescLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(registerDateDescLabel, gbc);
        registerDateLabel = new JLabel("", SwingConstants.LEFT);
        registerDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        registerDateLabel.setForeground(new Color(34, 34, 34));
        gbc.gridx = 1;
        infoPanel.add(registerDateLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // 借阅数量
        JLabel currentBorrowDescLabel = new JLabel("当前借阅数量:", SwingConstants.RIGHT);
        currentBorrowDescLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        currentBorrowDescLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(currentBorrowDescLabel, gbc);
        currentBorrowLabel = new JLabel("", SwingConstants.LEFT);
        currentBorrowLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        currentBorrowLabel.setForeground(new Color(34, 34, 34));
        gbc.gridx = 1;
        infoPanel.add(currentBorrowLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // 逾期数量
        JLabel overdueCountDescLabel = new JLabel("逾期图书数量:", SwingConstants.RIGHT);
        overdueCountDescLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        overdueCountDescLabel.setForeground(new Color(100, 100, 100));
        infoPanel.add(overdueCountDescLabel, gbc);
        overdueCountLabel = new JLabel("", SwingConstants.LEFT);
        overdueCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        overdueCountLabel.setForeground(new Color(204, 0, 0));
        gbc.gridx = 1;
        infoPanel.add(overdueCountLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // 分隔线
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        infoPanel.add(separator, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        // 最近借阅记录标题
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel recentLabel = new JLabel("最近借阅记录", SwingConstants.CENTER);
        recentLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        recentLabel.setForeground(new Color(34, 34, 34));
        infoPanel.add(recentLabel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;

        // 最近借阅表格
        String[] columnNames = {"图书名称", "借阅日期", "应还日期"};
        borrowTableModel = new DefaultTableModel(columnNames, 0);
        JTable borrowTable = new JTable(borrowTableModel);
        borrowTable.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        borrowTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 16));
        borrowTable.getTableHeader().setForeground(new Color(34, 34, 34));
        borrowTable.getTableHeader().setBackground(new Color(242, 242, 242));
        borrowTable.setRowHeight(30);
        borrowTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        borrowTable.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(borrowTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(228, 228, 228), 1));
        scrollPane.setPreferredSize(new Dimension(650, 200));
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        infoPanel.add(scrollPane, gbc);

        add(infoPanel, BorderLayout.CENTER);
    }

    private void loadUserInfo() {
        try {
            // 加载用户基本信息
            String userSql = "SELECT user_id, register_date, current_borrow " +
                    "FROM users WHERE user_id = ?";
            PreparedStatement userStmt = conn.prepareStatement(userSql);
            userStmt.setString(1, currentUsername);
            ResultSet userRs = userStmt.executeQuery();

            if (userRs.next()) {
                usernameLabel.setText(userRs.getString("user_id"));
                registerDateLabel.setText(dateFormat.format(userRs.getDate("register_date")));
                currentBorrowLabel.setText(String.valueOf(userRs.getInt("current_borrow")));
            }
            userRs.close();
            userStmt.close();

            // 计算逾期图书数量
            String overdueSql = "SELECT COUNT(*) as overdue_count " +
                    "FROM borrow_records " +
                    "WHERE user_id = ? AND return_date IS NULL AND due_date < CURDATE()";
            PreparedStatement overdueStmt = conn.prepareStatement(overdueSql);
            overdueStmt.setString(1, currentUsername);
            ResultSet overdueRs = overdueStmt.executeQuery();

            if (overdueRs.next()) {
                overdueCountLabel.setText(String.valueOf(overdueRs.getInt("overdue_count")));
            }
            overdueRs.close();
            overdueStmt.close();

            // 加载最近借阅记录（最多5条）
            String borrowSql = "SELECT b.title, l.borrow_date, l.due_date " +
                    "FROM borrow_records l " +
                    "JOIN books b ON l.book_id = b.book_id " +
                    "WHERE l.user_id = ? AND l.return_date IS NULL " +
                    "ORDER BY l.borrow_date DESC LIMIT 5";
            PreparedStatement borrowStmt = conn.prepareStatement(borrowSql);
            borrowStmt.setString(1, currentUsername);
            ResultSet borrowRs = borrowStmt.executeQuery();

            borrowTableModel.setRowCount(0);
            while (borrowRs.next()) {
                Object[] row = new Object[3];
                row[0] = borrowRs.getString("title");
                row[1] = dateFormat.format(borrowRs.getDate("borrow_date"));
                row[2] = dateFormat.format(borrowRs.getDate("due_date"));
                borrowTableModel.addRow(row);
            }
            borrowRs.close();
            borrowStmt.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "加载个人信息失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}