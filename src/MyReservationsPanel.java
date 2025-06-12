import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyReservationsPanel extends JPanel {


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
        String[] tableColumns = {"预约ID", "图书ID", "书名", "作者", "预约日期"};
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
        btnCancelReservation.addActionListener(e -> {
            try {
                cancelReservation();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
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

    }

    // 取消预约
    private void cancelReservation() throws SQLException {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要取消的预约！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int reservationId = (int) reservationModel.getValueAt(selectedRow, 0);


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
            String sql="delete from reservation where id=?";
            m_query.mysqlConnect();
            int affectRows=m_query.updateQuery(2,new String[]{sql,String.valueOf(reservationId)});
            if (affectRows > 0) {
                JOptionPane.showMessageDialog(this, "预约取消成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadReservations(); // 刷新表格
            } else {
                JOptionPane.showMessageDialog(this, "取消失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    // 日期格式化
    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }


}