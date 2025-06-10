import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BorrowManagementUI extends JFrame {
    // MySQL 配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "你的密码";

    // 标签页和面板
    private JTabbedPane tabbedPane;
    private JPanel panelBorrow, panelHistory;

    // 借阅登记组件
    private JTextField txtBookId, txtUserId, txtBorrowDate, txtDueDate;
    private JButton btnBorrow;

    // 借阅记录组件
    private JTable tableHistory;
    private DefaultTableModel modelHistory;
    private JTextField txtSearch;
    private JButton btnSearch, btnRefresh;

    public BorrowManagementUI() {
        setTitle("借阅管理 - 校园图书管理系统");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // 初始化标签页
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 初始化面板
        initBorrowPanel();
        initHistoryPanel();

        // 添加面板到标签页（移除了归还处理面板）
        tabbedPane.addTab("借阅登记", panelBorrow);
        tabbedPane.addTab("借阅记录", panelHistory);

        add(tabbedPane, BorderLayout.CENTER);
    }

    // 初始化借阅登记面板
    private void initBorrowPanel() {
        panelBorrow = new JPanel();
        panelBorrow.setBackground(Color.WHITE);
        panelBorrow.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;

        // 标题
        JLabel lblTitle = new JLabel("借阅登记");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 22));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelBorrow.add(lblTitle, gbc);

        // 图书ID
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblBookId = new JLabel("图书ID:");
        lblBookId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelBorrow.add(lblBookId, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        txtBookId = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panelBorrow.add(txtBookId, gbc);

        // 用户ID
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblUserId = new JLabel("用户ID:");
        lblUserId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelBorrow.add(lblUserId, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        txtUserId = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panelBorrow.add(txtUserId, gbc);

        // 借阅日期
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblBorrowDate = new JLabel("借阅日期:");
        lblBorrowDate.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelBorrow.add(lblBorrowDate, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        txtBorrowDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelBorrow.add(txtBorrowDate, gbc);

        // 应还日期
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblDueDate = new JLabel("应还日期:");
        lblDueDate.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelBorrow.add(lblDueDate, gbc);

        gbc.anchor = GridBagConstraints.WEST;
        txtDueDate = new JTextField(LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        gbc.gridx = 1;
        gbc.gridy = 4;
        panelBorrow.add(txtDueDate, gbc);

        // 确认借阅按钮
        btnBorrow = new JButton("确认借阅");
        btnBorrow.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnBorrow.setForeground(Color.WHITE);
        btnBorrow.setBackground(new Color(49, 130, 206));
        btnBorrow.setPreferredSize(new Dimension(120, 35));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelBorrow.add(btnBorrow, gbc);

        // 按钮事件
        btnBorrow.addActionListener(e -> processBorrow());
    }

    // 初始化借阅记录面板
    private void initHistoryPanel() {
        panelHistory = new JPanel();
        panelHistory.setBackground(Color.WHITE);
        panelHistory.setLayout(new BorderLayout(10, 10));

        // 搜索栏
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSearch.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("搜索:");
        lblSearch.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panelSearch.add(lblSearch);

        txtSearch = new JTextField(20);
        panelSearch.add(txtSearch);

        btnSearch = new JButton("搜索");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(49, 130, 206));
        panelSearch.add(btnSearch);

        btnRefresh = new JButton("刷新");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(170, 170, 170));
        panelSearch.add(btnRefresh);

        // 表格
        String[] columns = {"记录ID", "图书ID", "书名", "用户ID", "借阅日期", "应还日期", "归还日期", "状态", "罚金"};
        modelHistory = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableHistory = new JTable(modelHistory);
        tableHistory.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableHistory.setRowHeight(28);
        tableHistory.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tableHistory);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 组装面板
        panelHistory.add(panelSearch, BorderLayout.NORTH);
        panelHistory.add(scrollPane, BorderLayout.CENTER);

        // 按钮事件
        btnSearch.addActionListener(e -> searchRecords());
        btnRefresh.addActionListener(e -> loadHistory());

        // 初始化加载记录
        loadHistory();
    }

    // 处理借阅
    private void processBorrow() {
        String bookId = txtBookId.getText().trim();
        String userId = txtUserId.getText().trim();
        String borrowDate = txtBorrowDate.getText().trim();
        String dueDate = txtDueDate.getText().trim();

        // 非空校验
        if (bookId.isEmpty() || userId.isEmpty() || borrowDate.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 数字校验
        try {
            Integer.parseInt(bookId);
            Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "图书ID和用户ID必须是数字！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 日期校验
        try {
            LocalDate.parse(borrowDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate.parse(dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误！应为 yyyy-MM-dd", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 数据库操作
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // 开启事务
            try {
                // 检查图书是否可借
                String sqlCheckBook = "SELECT available_quantity FROM books WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlCheckBook)) {
                    pstmt.setInt(1, Integer.parseInt(bookId));
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next() || rs.getInt("available_quantity") <= 0) {
                        throw new SQLException("图书不可借或不存在");
                    }
                }

                // 检查用户是否存在
                String sqlCheckUser = "SELECT user_id FROM users WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlCheckUser)) {
                    pstmt.setInt(1, Integer.parseInt(userId));
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("用户不存在");
                    }
                }

                // 插入借阅记录
                String sqlInsert = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) " +
                        "VALUES (?, ?, ?, ?, '借阅中')";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    pstmt.setInt(1, Integer.parseInt(userId));
                    pstmt.setInt(2, Integer.parseInt(bookId));
                    pstmt.setString(3, borrowDate);
                    pstmt.setString(4, dueDate);
                    pstmt.executeUpdate();
                }

                // 更新图书库存
                String sqlUpdateBook = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdateBook)) {
                    pstmt.setInt(1, Integer.parseInt(bookId));
                    pstmt.executeUpdate();
                }

                conn.commit(); // 提交事务
                JOptionPane.showMessageDialog(this, "借阅成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                clearBorrowForm();
                loadHistory(); // 刷新记录
            } catch (SQLException e) {
                conn.rollback(); // 回滚事务
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 加载所有借阅记录
    private void loadHistory() {
        modelHistory.setRowCount(0); // 清空表格

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT br.record_id, br.book_id, b.title, br.user_id, br.borrow_date, " +
                    "br.due_date, br.return_date, br.status, br.fine " +
                    "FROM borrow_records br " +
                    "JOIN books b ON br.book_id = b.id " +
                    "ORDER BY br.borrow_date DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    modelHistory.addRow(new Object[]{
                            rs.getInt("record_id"),
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getInt("user_id"),
                            rs.getString("borrow_date"),
                            rs.getString("due_date"),
                            rs.getString("return_date"),
                            rs.getString("status"),
                            rs.getDouble("fine")
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载记录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 搜索借阅记录
    private void searchRecords() {
        String keyword = txtSearch.getText().trim();
        modelHistory.setRowCount(0); // 清空表格

        if (keyword.isEmpty()) {
            loadHistory(); // 关键词为空时加载全部
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT br.record_id, br.book_id, b.title, br.user_id, br.borrow_date, " +
                    "br.due_date, br.return_date, br.status, br.fine " +
                    "FROM borrow_records br " +
                    "JOIN books b ON br.book_id = b.id " +
                    "WHERE b.title LIKE ? OR CAST(br.user_id AS CHAR) LIKE ? " +
                    "ORDER BY br.borrow_date DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        modelHistory.addRow(new Object[]{
                                rs.getInt("record_id"),
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getInt("user_id"),
                                rs.getString("borrow_date"),
                                rs.getString("due_date"),
                                rs.getString("return_date"),
                                rs.getString("status"),
                                rs.getDouble("fine")
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "搜索记录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 清空借阅表单
    private void clearBorrowForm() {
        txtBookId.setText("");
        txtUserId.setText("");
        txtBorrowDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtDueDate.setText(LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    // 主方法测试
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BorrowManagementUI ui = new BorrowManagementUI();
            ui.setVisible(true);
        });
    }
}