import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class MyBorrowPanel extends JPanel {
    private JTable borrowTable;
    private DefaultTableModel tableModel;
    private JButton renewButton;
    private JButton returnButton; // 新增归还按钮
    private JLabel statusLabel;
    private Connection conn;
    private String currentUsername;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MyBorrowPanel(Connection conn, String username) {
        this.conn = conn;
        this.currentUsername = username;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeComponents();
        loadBorrowData();
    }

    private void initializeComponents() {
        // 顶部标题
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("我的借阅");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 功能按钮（新增归还按钮）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        renewButton = new JButton("续借所选");
        renewButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        renewButton.addActionListener(new RenewButtonListener());
        buttonPanel.add(renewButton);

        returnButton = new JButton("归还所选"); // 新增归还按钮
        returnButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        returnButton.addActionListener(new ReturnButtonListener()); // 绑定归还事件
        buttonPanel.add(returnButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = {"图书ID", "书名", "作者", "出版社", "借阅日期", "应还日期", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0);
        borrowTable = new JTable(tableModel);
        borrowTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        borrowTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        borrowTable.setRowHeight(25);
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(borrowTable);
        add(scrollPane, BorderLayout.CENTER);

        // 底部状态
        statusLabel = new JLabel("准备就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadBorrowData() {
        tableModel.setRowCount(0);

        try {
            String sql = "SELECT b.book_id, b.title, b.author, b.publisher, " +
                    "l.borrow_date, l.due_date, " +
                    "CASE WHEN l.return_date IS NULL THEN '未归还' ELSE '已归还' END AS status " +
                    "FROM borrow_records l " +
                    "JOIN books b ON l.book_id = b.book_id " +
                    "WHERE l.user_id = ? AND l.return_date IS NULL";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[7];
                row[0] = rs.getString("book_id");
                row[1] = rs.getString("title");
                row[2] = rs.getString("author");
                row[3] = rs.getString("publisher");
                row[4] = dateFormat.format(rs.getDate("borrow_date"));
                row[5] = dateFormat.format(rs.getDate("due_date"));
                row[6] = rs.getString("status");

                tableModel.addRow(row);
            }

            rs.close();
            pstmt.close();

            if (tableModel.getRowCount() == 0) {
                statusLabel.setText("当前没有借阅记录");
            } else {
                statusLabel.setText("共 " + tableModel.getRowCount() + " 条借阅记录");
            }

        } catch (SQLException e) {
            statusLabel.setText("加载借阅数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private class RenewButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = borrowTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(MyBorrowPanel.this,
                        "请先选择要续借的图书", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String bookId = (String) tableModel.getValueAt(selectedRow, 0);
            String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
            String dueDateStr = (String) tableModel.getValueAt(selectedRow, 5);

            try {
                Date dueDate = dateFormat.parse(dueDateStr);
                Date today = new Date();

                if (dueDate.before(today)) {
                    JOptionPane.showMessageDialog(MyBorrowPanel.this,
                            "该书已逾期，无法续借", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(MyBorrowPanel.this,
                        "确定要续借《" + bookTitle + "》吗？\n当前应还日期: " + dueDateStr,
                        "确认续借", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    // 计算新的应还日期（假设续借30天）
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dueDate);
                    calendar.add(Calendar.DAY_OF_MONTH, 30);
                    Date newDueDate = calendar.getTime();
                    String newDueDateStr = dateFormat.format(newDueDate);

                    // 更新数据库
                    String sql = "UPDATE borrow_records SET due_date = ?, renew_count = renew_count + 1 " +
                            "WHERE user_id = ? AND book_id = ? AND return_date IS NULL";

                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setDate(1, new java.sql.Date(newDueDate.getTime()));
                    pstmt.setString(2, currentUsername);
                    pstmt.setString(3, bookId);

                    int rowsAffected = pstmt.executeUpdate();
                    pstmt.close();

                    if (rowsAffected > 0) {
                        // 更新表格
                        tableModel.setValueAt(newDueDateStr, selectedRow, 5);
                        statusLabel.setText("续借成功，新的应还日期: " + newDueDateStr);
                        JOptionPane.showMessageDialog(MyBorrowPanel.this,
                                "续借成功！新的应还日期为: " + newDueDateStr,
                                "成功", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("续借失败");
                        JOptionPane.showMessageDialog(MyBorrowPanel.this,
                                "续借失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                statusLabel.setText("续借操作失败: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(MyBorrowPanel.this,
                        "续借操作失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 新增归还按钮的事件处理类
    private class ReturnButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = borrowTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(MyBorrowPanel.this,
                        "请先选择要归还的图书", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String bookId = (String) tableModel.getValueAt(selectedRow, 0);
            String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
            String dueDateStr = (String) tableModel.getValueAt(selectedRow, 5);

            try {
                Date dueDate = dateFormat.parse(dueDateStr);
                Date today = new Date();
                int daysLate = 0;

                // 计算逾期天数
                if (dueDate.before(today)) {
                    long diff = today.getTime() - dueDate.getTime();
                    daysLate = (int) (diff / (1000 * 60 * 60 * 24));
                }

                // 显示归还确认对话框（包含逾期信息）
                String message = "确定要归还《" + bookTitle + "》吗？\n";
                if (daysLate > 0) {
                    message += "该书已逾期 " + daysLate + " 天，可能需要缴纳罚款。";
                } else {
                    message += "应还日期: " + dueDateStr;
                }

                int confirm = JOptionPane.showConfirmDialog(MyBorrowPanel.this,
                        message, "确认归还", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    // 更新借阅记录，设置归还日期为当前日期
                    String sql = "UPDATE borrow_records SET return_date = ? " +
                            "WHERE user_id = ? AND book_id = ? AND return_date IS NULL";

                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setDate(1, new java.sql.Date(today.getTime()));
                    pstmt.setString(2, currentUsername);
                    pstmt.setString(3, bookId);

                    int rowsAffected = pstmt.executeUpdate();
                    pstmt.close();

                    if (rowsAffected > 0) {
                        // 更新表格中的状态为"已归还"
                        tableModel.setValueAt("已归还", selectedRow, 6);
                        statusLabel.setText("图书归还成功！");

                        // 如果有逾期，提示可能的罚款
                        if (daysLate > 0) {
                            JOptionPane.showMessageDialog(MyBorrowPanel.this,
                                    "图书已成功归还，但已逾期 " + daysLate + " 天。\n" +
                                            "请联系管理员处理可能的罚款事宜。",
                                    "归还成功", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(MyBorrowPanel.this,
                                    "图书已成功归还，感谢您的配合！",
                                    "归还成功", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        statusLabel.setText("图书归还失败");
                        JOptionPane.showMessageDialog(MyBorrowPanel.this,
                                "归还失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                statusLabel.setText("归还操作失败: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(MyBorrowPanel.this,
                        "归还操作失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}