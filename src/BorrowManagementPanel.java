import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

// 从 JFrame 改为 JPanel
public class BorrowManagementPanel extends JPanel {
    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bms_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Lqf123000@";

    // 添加日志
    private static final Logger logger = Logger.getLogger(BorrowManagementPanel.class.getName());

    // 表格组件
    private JTable borrowTable;
    private DefaultTableModel tableModel;

    public BorrowManagementPanel() {
        // 设置面板属性
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(10, 10));

        // 初始化界面组件
        initComponents();

        // 加载借阅数据
        loadBorrowData();
    }

    private void initComponents() {
        // 顶部搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);

        // 改为局部变量
        JTextField txtUserId = new JTextField(10);
        JTextField txtBookId = new JTextField(10);

        searchPanel.add(new JLabel("用户ID:"));
        searchPanel.add(txtUserId);
        searchPanel.add(new JLabel("图书ID:"));
        searchPanel.add(txtBookId);

        JButton searchButton = new JButton("搜索");
        // Lambda 中使用的是方法内局部变量，满足有效 final 要求
        searchButton.addActionListener(event -> {
            searchBorrowData(txtUserId.getText().trim(), txtBookId.getText().trim());
        });
        searchPanel.add(searchButton);

        JButton addButton = new JButton("添加借阅记录");
        addButton.addActionListener(event -> addBorrowRecord());
        searchPanel.add(addButton);

        // 表格面板
        String[] columnNames = {"借阅ID", "用户ID", "用户名", "图书ID", "书名", "借阅日期", "应还日期", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0);
        borrowTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(borrowTable);

        // 添加组件到主面板
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    // 修改 searchBorrowData 方法，接收参数
    private void searchBorrowData(String userIdStr, String bookIdStr) {
        // 验证输入
        if (userIdStr.isEmpty() && bookIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请至少输入一个搜索条件！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 构建动态SQL查询
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT b.id, b.user_id, u.username, b.book_id, ");
            sqlBuilder.append("bk.title, b.borrow_date, b.due_date, b.status ");
            sqlBuilder.append("FROM borrow_records b ");
            sqlBuilder.append("JOIN users u ON b.user_id = u.id ");
            sqlBuilder.append("JOIN books bk ON b.book_id = bk.id ");
            sqlBuilder.append("WHERE 1=1 ");

            // 添加条件
            if (!userIdStr.isEmpty()) {
                sqlBuilder.append("AND b.user_id = ? ");
            }

            if (!bookIdStr.isEmpty()) {
                sqlBuilder.append("AND b.book_id = ? ");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
                int paramIndex = 1;

                if (!userIdStr.isEmpty()) {
                    pstmt.setInt(paramIndex++, Integer.parseInt(userIdStr));
                }

                if (!bookIdStr.isEmpty()) {
                    pstmt.setInt(paramIndex, Integer.parseInt(bookIdStr));
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    tableModel.setRowCount(0); // 清空表格

                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        Object[] row = {
                                rs.getInt("id"),
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getDate("borrow_date"),
                                rs.getDate("due_date"),
                                rs.getString("status")
                        };
                        tableModel.addRow(row);
                    }

                    if (!found) {
                        JOptionPane.showMessageDialog(this, "未找到匹配的借阅记录！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "用户ID和图书ID必须是数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "搜索借阅数据失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "搜索借阅数据失败", ex);
        }
    }

    private void loadBorrowData() {
        // 从数据库加载借阅数据
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT b.id, b.user_id, u.username, b.book_id, " +
                    "bk.title, b.borrow_date, b.due_date, b.status " +
                    "FROM borrow_records b " +
                    "JOIN users u ON b.user_id = u.id " +
                    "JOIN books bk ON b.book_id = bk.id";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                tableModel.setRowCount(0); // 清空表格

                while (rs.next()) {
                    Object[] row = {
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getDate("borrow_date"),
                            rs.getDate("due_date"),
                            rs.getString("status")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载借阅数据失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "加载借阅数据失败", ex);
        }
    }

    private void addBorrowRecord() {
        // 创建添加借阅记录的对话框
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "添加借阅记录", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField userIdField = new JTextField();
        JTextField bookIdField = new JTextField();
        JTextField borrowDateField = new JTextField();
        JTextField dueDateField = new JTextField();

        // 设置默认借阅日期为今天，应还日期为30天后
        Calendar calendar = Calendar.getInstance();
        Date today = new Date(calendar.getTimeInMillis());
        borrowDateField.setText(today.toString());

        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date initialDueDate = new Date(calendar.getTimeInMillis());
        dueDateField.setText(initialDueDate.toString());

        formPanel.add(new JLabel("用户ID:"));
        formPanel.add(userIdField);
        formPanel.add(new JLabel("图书ID:"));
        formPanel.add(bookIdField);
        formPanel.add(new JLabel("借阅日期 (yyyy-MM-dd):"));
        formPanel.add(borrowDateField);
        formPanel.add(new JLabel("应还日期 (yyyy-MM-dd):"));
        formPanel.add(dueDateField);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(event -> {
            try {
                int userId = Integer.parseInt(userIdField.getText());
                int bookId = Integer.parseInt(bookIdField.getText());
                Date borrowDate = Date.valueOf(borrowDateField.getText());
                // 重新在 Lambda 内定义变量，避免作用域问题
                Date dueDate = Date.valueOf(dueDateField.getText());

                // 验证日期
                if (dueDate.before(borrowDate)) {
                    JOptionPane.showMessageDialog(dialog, "应还日期必须晚于借阅日期", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 保存到数据库
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    // 检查用户是否存在
                    String checkUserSql = "SELECT id FROM users WHERE id = ?";
                    try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql)) {
                        checkUserStmt.setInt(1, userId);
                        try (ResultSet rs = checkUserStmt.executeQuery()) {
                            if (!rs.next()) {
                                JOptionPane.showMessageDialog(dialog, "用户不存在！", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    // 检查图书是否存在且可借
                    String checkBookSql = "SELECT id, available_quantity FROM books WHERE id = ?";
                    try (PreparedStatement checkBookStmt = conn.prepareStatement(checkBookSql)) {
                        checkBookStmt.setInt(1, bookId);
                        try (ResultSet rs = checkBookStmt.executeQuery()) {
                            if (!rs.next()) {
                                JOptionPane.showMessageDialog(dialog, "图书不存在！", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            int availableQuantity = rs.getInt("available_quantity");
                            if (availableQuantity <= 0) {
                                JOptionPane.showMessageDialog(dialog, "该图书已无可借阅副本！", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    // 开始事务
                    conn.setAutoCommit(false);

                    try {
                        // 插入借阅记录
                        String insertSql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) " +
                                "VALUES (?, ?, ?, ?, '借阅中')";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setInt(2, bookId);
                            insertStmt.setDate(3, borrowDate);
                            insertStmt.setDate(4, dueDate);
                            insertStmt.executeUpdate();
                        }

                        // 更新图书可借数量
                        String updateSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, bookId);
                            updateStmt.executeUpdate();
                        }

                        // 提交事务
                        conn.commit();
                        JOptionPane.showMessageDialog(dialog, "借阅记录添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();

                        // 刷新表格数据
                        loadBorrowData();
                    } catch (SQLException ex) {
                        // 回滚事务
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "用户ID和图书ID必须是数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "日期格式不正确，请使用yyyy-MM-dd格式", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "添加借阅记录失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                logger.log(Level.SEVERE, "添加借阅记录失败", ex);
            }
        });

        cancelButton.addActionListener(event -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}