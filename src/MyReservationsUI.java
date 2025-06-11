import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyReservationsUI extends JFrame {
    // 数据库配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "你的密码";

    // 当前登录用户ID（实际应从系统登录态获取）
    private int currentUserId = 1;

    // 组件声明
    private JTable reservationTable;
    private DefaultTableModel reservationModel;
    private JButton btnCancelReservation; // 取消预约按钮
    private JButton btnRefresh;           // 刷新按钮

    public MyReservationsUI() {
        setTitle("我的预约 - 校园图书管理系统");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        initComponents();
        loadReservations(); // 加载预约记录
    }

    // 初始化界面组件
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 标题
        JLabel titleLabel = new JLabel("我的预约记录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

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
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // 底部按钮栏
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        btnCancelReservation = new JButton("取消选中预约");
        btnCancelReservation.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnCancelReservation.setForeground(Color.WHITE);
        btnCancelReservation.setBackground(new Color(220, 53, 69));
        btnCancelReservation.addActionListener(e -> cancelReservation());
        buttonPanel.add(btnCancelReservation);

        btnRefresh = new JButton("刷新记录");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.addActionListener(e -> loadReservations());
        buttonPanel.add(btnRefresh);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // 加载预约记录
    private void loadReservations() {
        reservationModel.setRowCount(0); // 清空表格

        String sql = "SELECT " +
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
        }
    }

    // 取消预约
    private void cancelReservation() {
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
            MyReservationsUI ui = new MyReservationsUI();
            ui.setVisible(true);
        });
    }
}