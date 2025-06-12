import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyReservationsPanel extends JPanel {
    // 数据库配置
    /*private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "你的密码";*/

    // 当前登录用户ID（实际应从系统登录态获取）
    private int currentUserId ;

    // 组件声明
    private JTable reservationTable;
    private DefaultTableModel reservationModel;
    private JButton btnCancelReservation; // 取消预约按钮
    private JButton btnRefresh;           // 刷新按钮
    private SqlQuery m_query;

    public MyReservationsPanel(SqlQuery query,String userName) throws SQLException {
        setBackground(Color.WHITE);
        m_query=query;
        currentUserId=Integer.parseInt(userName);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        loadReservations(); // 加载预约记录
    }

    // 初始化界面组件
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // 标题
        JLabel titleLabel = new JLabel("我的预约记录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 预约表格
        String[] tableColumns = {"预约ID", "图书ID", "书名", "作者", "预约日期", "状态", "操作"};
        reservationModel = new DefaultTableModel(tableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 仅"操作"列可编辑
            }
        };

        reservationTable = new JTable(reservationModel);
        reservationTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        reservationTable.setRowHeight(30);
        reservationTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        JScrollPane tableScrollPane = new JScrollPane(reservationTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // 底部按钮栏
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        btnCancelReservation = new JButton("取消选中预约");
        btnCancelReservation.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnCancelReservation.setForeground(Color.WHITE);
        btnCancelReservation.setBackground(new Color(220, 53, 69));
        //btnCancelReservation.addActionListener(e -> cancelReservation());
        buttonPanel.add(btnCancelReservation);

        btnRefresh = new JButton("刷新记录");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.addActionListener(e -> {
            try {
                loadReservations();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        buttonPanel.add(btnRefresh);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // 加载预约记录
    private void loadReservations() throws SQLException {
        reservationModel.setRowCount(0); // 清空表格
        String sql="select B.author,B.bname,A.bid,A.uid,A.id,A.start_date,A.end_date from reservation as A inner join bookinfo as B on A.bid=B.bid where A.uid=?";
        m_query.mysqlConnect();
        ResultSet rset=m_query.selectQuery(2,new String[]{sql,String.valueOf(currentUserId)});
        while (rset.next()) {
            //String status = rset.getString("status");
            String actionBtn = "";

            // 根据状态显示操作按钮
            /*if ("预约中".equals(status)) {
                actionBtn = "取消预约";
            } else if ("已生效".equals(status)) {
                actionBtn = "等待取书";
            } else if ("已过期".equals(status)) {
                actionBtn = "预约已过期";
            }*/

            reservationModel.addRow(new Object[]{
                    rset.getInt("id"),
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getString("author"),
                    //formatDate(rset.getDate("start_date")),
                    "2344-12-32",
                    //status,
                    actionBtn
            });
        }
        /*String sql = "SELECT " +
                "r.id AS reservation_id, " +
                "b.id AS book_id, " +
                "b.title, " +
                "b.author, " +
                "r.reservation_date, " +
                "r.status " +
                "FROM reservations r " +
                "JOIN books b ON r.book_id = b.id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.reservation_date DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                String actionBtn = "";

                // 根据状态显示操作按钮
                if ("预约中".equals(status)) {
                    actionBtn = "取消预约";
                } else if ("已生效".equals(status)) {
                    actionBtn = "等待取书";
                } else if ("已过期".equals(status)) {
                    actionBtn = "预约已过期";
                }

                reservationModel.addRow(new Object[]{
                        rs.getInt("reservation_id"),
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        formatDate(rs.getDate("reservation_date")),
                        status,
                        actionBtn
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载预约记录失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }*/
    }

    // 取消预约
    /*private void cancelReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要取消的预约！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int reservationId = (int) reservationModel.getValueAt(selectedRow, 0);
        String status = (String) reservationModel.getValueAt(selectedRow, 5);

        if (!"预约中".equals(status)) {
            JOptionPane.showMessageDialog(this, "该预约不可取消（状态：" + status + "）", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 确认对话框
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要取消该预约吗？",
                "确认取消",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // 更新预约状态为"已取消"
            String sql = "UPDATE reservations " +
                    "SET status = '已取消', cancellation_date = NOW() " +
                    "WHERE id = ? AND user_id = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, reservationId);
                pstmt.setInt(2, currentUserId);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "预约取消成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    loadReservations(); // 刷新表格
                } else {
                    JOptionPane.showMessageDialog(this, "取消失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "取消失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 日期格式化
    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    // 主方法
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("我的预约 - 校园图书管理系统");
            frame.setSize(900, 500);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            MyReservationsPanel panel = new MyReservationsPanel();
            frame.add(panel);

            frame.setVisible(true);
        });
    }*/
}