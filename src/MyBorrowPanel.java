import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

public class MyBorrowPanel extends JPanel {
    private JTable borrowTable;
    private DefaultTableModel tableModel;
    private JButton renewButton;
    //private Connection conn;
    private JButton returnButton; // 新增归还按钮
    private JLabel statusLabel;
    private String currentUsername;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SqlQuery m_query;


    public MyBorrowPanel(SqlQuery query, String username) throws SQLException {
        System.out.println("username:"+username);
        m_query=query;
        //this.conn=conn;
        this.currentUsername = username;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initializeComponents();
        loadBorrowData(username);
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
        String[] columnNames = {"记录id","图书ID", "书名", "作者", "出版社", "借阅日期", "应还日期"};
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

    private void loadBorrowData(String username) throws SQLException {
        tableModel.setRowCount(0);
        String sql="select A.id,A.is_ret,A.start_date,A.end_date,A.practical_date,B.bid,B.bname,B.publisher,B.author from borrow_relation as A inner join bookinfo as B on A.bid=B.bid where A.uid=? and A.is_ret=0 and A.practical_date is null";
        m_query.mysqlConnect();
        ResultSet rset=m_query.selectQuery(2,new String[]{sql,username});
        while (rset.next()) {
            Object[] row = new Object[7];
            row[0] = rset.getString("id");
            row[1] = rset.getString("bid");
            row[2] = rset.getString("bname");
            row[3] = rset.getString("author");
            row[4] = rset.getString("publisher");
            row[5] = dateFormat.format(rset.getDate("start_date"));
            row[6] = dateFormat.format(rset.getDate("end_date"));
            tableModel.addRow(row);
        }
        m_query.mysqlDisconnect();
        if (tableModel.getRowCount() == 0) {
            statusLabel.setText("当前没有借阅记录");
        } else {
            statusLabel.setText("共 " + tableModel.getRowCount() + " 条借阅记录");
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
                    String sql="update borrow_relation set end_date=? where uid=? and bid=? and practical_date is null";
                    m_query.mysqlConnect();
                    int affectRows=m_query.updateQuery(4,new String[]{sql,newDueDateStr,currentUsername,bookId});
                    if (affectRows > 0) {
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
            String id = (String) tableModel.getValueAt(selectedRow, 0);
            String bookId = (String) tableModel.getValueAt(selectedRow, 1);
            String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);
            String dueDateStr = (String) tableModel.getValueAt(selectedRow, 6);

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

                    m_query.mysqlConnect();
                    // 获取当前日期
                    LocalDate now = LocalDate.now();
                    // 定义格式化模式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    // 格式化日期
                    String formattedDate = now.format(formatter);
                    m_query.returnTransaction(id,formattedDate);

                    m_query.mysqlDisconnect();

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