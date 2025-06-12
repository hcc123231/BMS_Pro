import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


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
    private SqlQuery m_query;

    public ReturnManagementPanel(SqlQuery query) throws SQLException {
        setBackground(Color.WHITE);
        m_query = query;
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
        btnSearch.addActionListener(e -> {
            try {
                searchBorrowedRecords();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        searchPanel.add(btnSearch);

        btnRefresh = new JButton("刷新");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(170, 170, 170));
        btnRefresh.addActionListener(e -> {
            try {
                loadBorrowedRecords();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
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
    private void loadBorrowedRecords() throws SQLException {
        modelRecords.setRowCount(0); // 清空表格
        String sql = "select B.id,B.bid,B.uid,B.start_date,B.end_date,B.practical_date,A.bname,B.is_ret from bookinfo as A inner join borrow_relation as B on A.bid=B.bid where B.is_ret=0 order by B.start_date";
        m_query.mysqlConnect();
        ResultSet rset = m_query.selectQuery(1, new String[]{sql});
        while (rset.next()) {
            Date endDate = rset.getDate("end_date");

            LocalDate endLocalDate = endDate.toLocalDate();
            LocalDate currentLocalDate=LocalDate.now();
            long overdueDays = ChronoUnit.DAYS.between(endLocalDate, currentLocalDate);
            //double fine = Math.max(0, overdueDays * 0.5); // 逾期天数为负时罚金为0
            double fine = 0;
            modelRecords.addRow(new Object[]{
                    rset.getInt("id"),
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getInt("uid"),
                    rset.getString("start_date"),
                    rset.getString("end_date"),
                    overdueDays > 0 ? overdueDays : "未逾期",
                    fine > 0 ? String.format("%.2f元", fine) : "无"
            });
        }
        m_query.mysqlDisconnect();

    }

    // 搜索一个未归还的借阅记录
    private void searchBorrowedRecords() throws SQLException {
        String keyword = txtSearch.getText().trim();
        modelRecords.setRowCount(0); // 清空表格

        if (keyword.isEmpty()) {
            loadBorrowedRecords(); // 关键词为空时加载全部
            return;
        }
        String sql = "select B.id,B.bid,B.uid,B.start_date,B.end_date,B.practical_date,A.bname,B.is_ret from bookinfo as A inner join borrow_relation as B on A.bid=B.bid where B.is_ret=0 and B.id like ? or B.bid like ? or B.uid like ? or A.bname like ? order by B.start_date";
        m_query.mysqlConnect();
        String param = "%" + keyword + "%";
        ResultSet rset = m_query.selectQuery(5, new String[]{sql, param, param, param, param});
        while (rset.next()) {
            Date endDate = rset.getDate("end_date");

            LocalDate endLocalDate = endDate.toLocalDate();
            LocalDate currentLocalDate=LocalDate.now();
            long overdueDays = ChronoUnit.DAYS.between(endLocalDate, currentLocalDate);
            //double fine = Math.max(0, overdueDays * 0.5); // 逾期天数为负时罚金为0
            double fine = 0;
            modelRecords.addRow(new Object[]{
                    rset.getInt("id"),
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getInt("uid"),
                    rset.getString("start_date"),
                    rset.getString("end_date"),
                    overdueDays > 0 ? overdueDays : "未逾期",
                    fine > 0 ? String.format("%.2f元", fine) : "无"
            });
        }
        m_query.mysqlDisconnect();
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
        m_query.mysqlConnect();
        try{
            m_query.returnTransaction(recordId,returnDate);
            // 清空表单并刷新记录
            txtRecordId.setText("");
            loadBorrowedRecords();
            JOptionPane.showMessageDialog(this,"成功归还","操作提示",JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "归还失败：" + e.getMessage(), "错误提示", JOptionPane.ERROR_MESSAGE);
        }

    }
}

