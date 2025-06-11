import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ReturnManagementPanel extends JPanel {
    // MySQL 数据库连接信息（修改为你的实际配置）
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "你的MySQL密码";

    // 组件声明
    private JPanel mainPanel;
    private JTextField txtRecordId, txtReturnDate;
    private JButton btnReturn, btnSearch, btnRefresh;
    private JTable tableRecords;
    private DefaultTableModel modelRecords;
    private JTextField txtSearch;

    public ReturnManagementPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        initComponents();
        loadBorrowedRecords(); // 加载未归还的记录
    }

    // 初始化组件
    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部搜索区域
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("搜索借阅记录:");
        lblSearch.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(lblSearch);

        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        btnSearch = new JButton("搜索");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(49, 130, 206));
        btnSearch.addActionListener(e -> searchBorrowedRecords());
        searchPanel.add(btnSearch);

        btnRefresh = new JButton("刷新");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(170, 170, 170));
        btnRefresh.addActionListener(e -> loadBorrowedRecords());
        searchPanel.add(btnRefresh);

        // 中间表格区域
        String[] columns = {"记录ID", "图书ID", "书名", "用户ID", "借阅日期", "应还日期", "逾期天数", "罚金"};
        modelRecords = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableRecords = new JTable(modelRecords);
        tableRecords.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableRecords.setRowHeight(28);
        tableRecords.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        // 双击表格自动填充记录ID
        tableRecords.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableRecords.getSelectedRow();
                    if (row >= 0) {
                        txtRecordId.setText(modelRecords.getValueAt(row, 0).toString());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableRecords);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 底部归还表单
        JPanel returnFormPanel = new JPanel(new GridBagLayout());
        returnFormPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 记录ID
        JLabel lblRecordId = new JLabel("借阅记录ID:");
        lblRecordId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        returnFormPanel.add(lblRecordId, gbc);

        txtRecordId = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        returnFormPanel.add(txtRecordId, gbc);

        // 归还日期
        JLabel lblReturnDate = new JLabel("归还日期:");
        lblReturnDate.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 2;
        gbc.gridy = 0;
        returnFormPanel.add(lblReturnDate, gbc);

        txtReturnDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        gbc.gridx = 3;
        gbc.gridy = 0;
        returnFormPanel.add(txtReturnDate, gbc);

        // 确认归还按钮
        btnReturn = new JButton("确认归还");
        btnReturn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnReturn.setForeground(Color.WHITE);
        btnReturn.setBackground(new Color(49, 130, 206));
        btnReturn.setPreferredSize(new Dimension(120, 35));
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 20, 10, 10);
        returnFormPanel.add(btnReturn, gbc);

        // 按钮事件
        btnReturn.addActionListener(e -> processReturn());

        // 组装主面板
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(returnFormPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // 加载所有未归还的借阅记录
    private void loadBorrowedRecords() {
        modelRecords.setRowCount(0); // 清空表格

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT br.record_id, br.book_id, b.title, br.user_id, br.borrow_date, " +
                    "br.due_date, DATEDIFF(CURDATE(), br.due_date) AS overdue_days, " +
                    "DATEDIFF(CURDATE(), br.due_date) * 0.5 AS fine " +
                    "FROM borrow_records br " +
                    "JOIN books b ON br.book_id = b.id " +
                    "WHERE br.status = '借阅中' " +
                    "ORDER BY br.due_date ASC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    int overdueDays = rs.getInt("overdue_days");
                    double fine = Math.max(0, overdueDays * 0.5); // 逾期天数为负时罚金为0

                    modelRecords.addRow(new Object[]{
                            rs.getInt("record_id"),
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getInt("user_id"),
                            rs.getString("borrow_date"),
                            rs.getString("due_date"),
                            overdueDays > 0 ? overdueDays : "未逾期",
                            fine > 0 ? String.format("%.2f元", fine) : "无"
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载借阅记录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 搜索一个未归还的借阅记录
    private void searchBorrowedRecords() {
        String keyword = txtSearch.getText().trim();
        modelRecords.setRowCount(0); // 清空表格

        if (keyword.isEmpty()) {
            loadBorrowedRecords(); // 关键词为空时加载全部
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT br.record_id, br.book_id, b.title, br.user_id, br.borrow_date, " +
                    "br.due_date, DATEDIFF(CURDATE(), br.due_date) AS overdue_days, " +
                    "DATEDIFF(CURDATE(), br.due_date) * 0.5 AS fine " +
                    "FROM borrow_records br " +
                    "JOIN books b ON br.book_id = b.id " +
                    "WHERE br.status = '借阅中' " +
                    "AND (b.title LIKE ? OR CAST(br.user_id AS CHAR) LIKE ?) " +
                    "ORDER BY br.due_date ASC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int overdueDays = rs.getInt("overdue_days");
                        double fine = Math.max(0, overdueDays * 0.5); // 逾期天数为负时罚金为0

                        modelRecords.addRow(new Object[]{
                                rs.getInt("record_id"),
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getInt("user_id"),
                                rs.getString("borrow_date"),
                                rs.getString("due_date"),
                                overdueDays > 0 ? overdueDays : "未逾期",
                                fine > 0 ? String.format("%.2f元", fine) : "无"
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "搜索借阅记录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 处理归还操作
    private void processReturn() {
        String recordId = txtRecordId.getText().trim();
        String returnDate = txtReturnDate.getText().trim();

        // 非空校验
        if (recordId.isEmpty() || returnDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 数字校验
        try {
            Integer.parseInt(recordId);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "记录ID必须是数字！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 日期校验
        try {
            LocalDate.parse(returnDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误！应为 yyyy-MM-dd", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 数据库操作
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // 开启事务
            try {
                // 检查记录状态
                String sqlCheck = "SELECT status, book_id, due_date FROM borrow_records WHERE record_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
                    pstmt.setInt(1, Integer.parseInt(recordId));
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next() || !"借阅中".equals(rs.getString("status"))) {
                        throw new SQLException("记录不存在或已归还");
                    }

                    int bookId = rs.getInt("book_id");
                    String dueDate = rs.getString("due_date");

                    // 计算罚金
                    LocalDate dueDateObj = LocalDate.parse(dueDate);
                    LocalDate returnDateObj = LocalDate.parse(returnDate);
                    double fine = 0.0;
                    if (returnDateObj.isAfter(dueDateObj)) {
                        long overdueDays = returnDateObj.toEpochDay() - dueDateObj.toEpochDay();
                        fine = overdueDays * 0.5; // 每天0.5元罚金
                    }

                    // 更新借阅记录
                    String sqlUpdateRecord = "UPDATE borrow_records " +
                            "SET return_date = ?, status = '已归还', fine = ? " +
                            "WHERE record_id = ?";
                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateRecord)) {
                        pstmtUpdate.setString(1, returnDate);
                        pstmtUpdate.setDouble(2, fine);
                        pstmtUpdate.setInt(3, Integer.parseInt(recordId));
                        pstmtUpdate.executeUpdate();
                    }

                    // 更新图书库存
                    String sqlUpdateBook = "UPDATE books " +
                            "SET available_quantity = available_quantity + 1 " +
                            "WHERE id = ?";
                    try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateBook)) {
                        pstmtUpdate.setInt(1, bookId);
                        pstmtUpdate.executeUpdate();
                    }
                }

                conn.commit(); // 提交事务

                // 显示归还成功信息
                if (modelRecords.getRowCount() > 0) {
                    int row = -1;
                    for (int i = 0; i < modelRecords.getRowCount(); i++) {
                        if (modelRecords.getValueAt(i, 0).toString().equals(recordId)) {
                            row = i;
                            break;
                        }
                    }

                    if (row >= 0) {
                        String fineStr = modelRecords.getValueAt(row, 7).toString();
                        if (fineStr.equals("无")) {
                            JOptionPane.showMessageDialog(this, "归还成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "归还成功！需缴纳罚金：" + fineStr,
                                    "成功", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "归还成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "归还成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                }

                // 清空表单并刷新记录
                txtRecordId.setText("");
                loadBorrowedRecords();

            } catch (SQLException e) {
                conn.rollback(); // 回滚事务
                JOptionPane.showMessageDialog(this, "归还失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 测试入口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("图书归还管理 - 校园图书管理系统");
            ReturnManagementPanel panel = new ReturnManagementPanel();
            frame.add(panel);
            frame.setSize(900, 600);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
