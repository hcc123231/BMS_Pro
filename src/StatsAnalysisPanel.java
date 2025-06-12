import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import java.util.Calendar;

public class StatsAnalysisPanel extends JPanel {
    // 数据库配置（需与系统保持一致）
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bms_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "2028915986hcc";

    // 组件声明
    private JComboBox<String> statsTypeCombo; // 统计类型（借阅趋势、热门图书、用户排行等）
    private JComboBox<String> timeRangeCombo; // 时间范围（周、月、季、年）
    private JTable statsTable;
    private DefaultTableModel statsModel;
    private JLabel totalBorrowLabel; // 总借阅量
    private JLabel avgBorrowLabel;   // 人均借阅量
    private JLabel topBookLabel;     // 最热门图书

    public StatsAnalysisPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        initComponents();
        loadStats(); // 初始化加载统计数据
    }

    // 初始化界面组件
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部筛选区域
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);

        JLabel statsTypeLabel = new JLabel("统计类型:");
        statsTypeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterPanel.add(statsTypeLabel);

        String[] statsTypes = {
                "图书借阅趋势",
                "热门图书排行（Top10）",
                "用户借阅排行（Top10）",
                "分类借阅占比"
        };
        statsTypeCombo = new JComboBox<>(statsTypes);
        statsTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statsTypeCombo.addActionListener(e -> loadStats());
        filterPanel.add(statsTypeCombo);

        JLabel timeRangeLabel = new JLabel("时间范围:");
        timeRangeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterPanel.add(timeRangeLabel);

        String[] timeRanges = {"本周", "本月", "本季度", "本年", "全部"};
        timeRangeCombo = new JComboBox<>(timeRanges);
        timeRangeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        timeRangeCombo.addActionListener(e -> loadStats());
        filterPanel.add(timeRangeCombo);

        // 核心统计指标区域
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        metricsPanel.setBackground(Color.WHITE);

        totalBorrowLabel = new JLabel("总借阅量: 0");
        totalBorrowLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        metricsPanel.add(totalBorrowLabel);

        avgBorrowLabel = new JLabel("人均借阅量: 0");
        avgBorrowLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        metricsPanel.add(avgBorrowLabel);

        topBookLabel = new JLabel("最热门图书: 无");
        topBookLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        metricsPanel.add(topBookLabel);

        // 统计结果表格
        String[] tableColumns = {"项目", "数量", "占比"};
        statsModel = new DefaultTableModel(tableColumns, 0);
        statsTable = new JTable(statsModel);
        statsTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statsTable.setRowHeight(28);
        statsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        JScrollPane tableScrollPane = new JScrollPane(statsTable);

        // 组装界面
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(metricsPanel, BorderLayout.CENTER);
        mainPanel.add(tableScrollPane, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // 核心方法：加载统计数据
    private void loadStats() {
        String statsType = (String) statsTypeCombo.getSelectedItem();
        String timeRange = (String) timeRangeCombo.getSelectedItem();

        // 1. 清空表格和指标
        statsModel.setRowCount(0);
        totalBorrowLabel.setText("总借阅量: 0");
        avgBorrowLabel.setText("人均借阅量: 0");
        topBookLabel.setText("最热门图书: 无");

        // 2. 根据统计类型执行查询
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            switch (statsType) {
                case "图书借阅趋势":
                    loadBorrowTrend(conn, timeRange);
                    break;
                case "热门图书排行（Top10）":
                    loadTopBooks(conn, timeRange);
                    break;
                case "用户借阅排行（Top10）":
                    loadTopUsers(conn, timeRange);
                    break;
                case "分类借阅占比":
                    loadCategoryRatio(conn, timeRange);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "统计失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------- 具体统计逻辑 -------------------------

    // 1. 图书借阅趋势（按时间维度统计）
    private void loadBorrowTrend(Connection conn, String timeRange) throws SQLException {
        String sql = "SELECT " +
                "DATE_FORMAT(start_date, ?) AS time_unit, " +
                "COUNT(*) AS borrow_count " +
                "FROM borrow_relation " +
                "WHERE start_date >= ? " +
                "GROUP BY time_unit " +
                "ORDER BY time_unit";

        // 计算时间范围
        Date startDate = getTimeRangeStart(timeRange);
        String format = getTimeFormat(timeRange);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, format); // 时间格式化（如 %Y-%m 按月分组）
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                statsModel.addRow(new Object[]{
                        rs.getString("time_unit"),
                        rs.getInt("borrow_count"),
                        "" // 占比在趋势中可不显示
                });
            }
        }

        // 更新核心指标
        updateCoreMetrics(conn, timeRange);
    }

    // 2. 热门图书排行（Top10）
    private void loadTopBooks(Connection conn, String timeRange) throws SQLException {
        String sql = "SELECT " +
                "bi.bname, COUNT(*) AS borrow_count " +
                "FROM borrow_relation br " +
                "JOIN bookinfo bi ON br.bid = bi.bid " +
                "WHERE br.start_date >= ? " +
                "GROUP BY br.bid " +
                "ORDER BY borrow_count DESC " +
                "LIMIT 10";

        Date startDate = getTimeRangeStart(timeRange);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                statsModel.addRow(new Object[]{
                        rs.getString("bname"),
                        rs.getInt("borrow_count"),
                        calcRatio(rs.getInt("borrow_count"), getTotalBorrow(conn, timeRange)) + "%"
                });
            }
        }

        // 更新核心指标（取第一名作为最热门图书）
        if (statsModel.getRowCount() > 0) {
            topBookLabel.setText("最热门图书: " + statsModel.getValueAt(0, 0));
        }
        updateCoreMetrics(conn, timeRange);
    }

    // 3. 用户借阅排行（Top10）
    private void loadTopUsers(Connection conn, String timeRange) throws SQLException {
        String sql = "SELECT " +
                "u.number, COUNT(*) AS borrow_count " +
                "FROM borrow_relation br " +
                "JOIN user u ON br.uid = u.uid " +
                "WHERE br.start_date >= ? " +
                "GROUP BY br.uid " +
                "ORDER BY borrow_count DESC " +
                "LIMIT 10";

        Date startDate = getTimeRangeStart(timeRange);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                statsModel.addRow(new Object[]{
                        rs.getString("account"),
                        rs.getInt("borrow_count"),
                        calcRatio(rs.getInt("borrow_count"), getTotalBorrow(conn, timeRange)) + "%"
                });
            }
        }

        updateCoreMetrics(conn, timeRange);
    }

    // 4. 分类借阅占比
    private void loadCategoryRatio(Connection conn, String timeRange) throws SQLException {
        String sql = "SELECT " +
                "bi.category, COUNT(*) AS borrow_count " +
                "FROM borrow_relation br " +
                "JOIN bookinfo bi ON br.bid = bi.bid " +
                "WHERE br.start_date >= ? " +
                "GROUP BY bi.category " +
                "ORDER BY borrow_count DESC";

        Date startDate = getTimeRangeStart(timeRange);
        int total = getTotalBorrow(conn, timeRange);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                statsModel.addRow(new Object[]{
                        rs.getString("category"),
                        rs.getInt("borrow_count"),
                        calcRatio(rs.getInt("borrow_count"), total) + "%"
                });
            }
        }

        updateCoreMetrics(conn, timeRange);
    }

    // ------------------------- 工具方法 -------------------------

    // 根据时间范围计算起始日期
    private Date getTimeRangeStart(String timeRange) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // 当前时间

        switch (timeRange) {
            case "本周":
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 本周一
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                break;
            case "本月":
                cal.set(Calendar.DAY_OF_MONTH, 1); // 本月1日
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                break;
            case "本季度":
                int quarterMonth = (cal.get(Calendar.MONTH) / 3) * 3; // 季度起始月
                cal.set(Calendar.MONTH, quarterMonth);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                break;
            case "本年":
                cal.set(Calendar.MONTH, 0); // 本年1月
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                break;
            case "全部":
                cal.setTime(new Date(0)); // 初始日期
                break;
        }
        return cal.getTime();
    }

    // 获取时间格式化字符串（用于分组）
    private String getTimeFormat(String timeRange) {
        switch (timeRange) {
            case "本周": return "%Y-%m-%d"; // 按天
            case "本月": return "%Y-%m";    // 按月
            case "本季度": return "%Y-Q%q"; // 按季度（需 MySQL 8.0+）
            case "本年": return "%Y";       // 按年
            default: return "%Y-%m-%d";    // 全部按天
        }
    }

    // 计算占比（保留1位小数）
    private String calcRatio(int count, int total) {
        if (total == 0) return "0.0";
        return String.format("%.1f", (count * 100.0) / total);
    }

    // 获取总借阅量
    private int getTotalBorrow(Connection conn, String timeRange) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_relation WHERE start_date >= ?";
        Date startDate = getTimeRangeStart(timeRange);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    // 获取总用户数（用于计算人均借阅量）
    private int getTotalUsers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // 更新核心指标（总借阅量、人均借阅量）
    private void updateCoreMetrics(Connection conn, String timeRange) {
        try {
            int totalBorrow = getTotalBorrow(conn, timeRange);
            int totalUsers = getTotalUsers(conn);
            double avg = (totalUsers == 0) ? 0 : totalBorrow * 1.0 / totalUsers;

            totalBorrowLabel.setText("总借阅量: " + totalBorrow);
            avgBorrowLabel.setText("人均借阅量: " + String.format("%.1f", avg));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 测试入口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("统计分析 - 校园图书管理系统");
            StatsAnalysisPanel panel = new StatsAnalysisPanel();
            frame.add(panel);
            frame.setSize(1000, 600);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
