import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

// 预约管理面板（核心业务 + 优化后的 UI）
public class ReservationManagerPanel extends JPanel {
    private SqlQuery m_query; // 设为final，因为初始化后不再修改

    private JTable reservationTable;
    private ReservationTableModel tableModel;
    private static final Logger logger = Logger.getLogger(ReservationManagerPanel.class.getName());
    // 样式常量（与系统风格统一）
    private static final Color LIGHT_COLOR = new Color(245, 245, 245);
    private static final Font MAIN_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    // 按钮组件（用于控制禁用状态）
    private JButton deleteButton;

    public ReservationManagerPanel(SqlQuery query) {
        m_query=query;

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initializeUI();
        loadReservations();
    }

    private void initializeUI() {
        // 顶部操作栏：按钮 + 搜索
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setBorder(BorderFactory.createTitledBorder("预约操作"));
        topBar.setBackground(LIGHT_COLOR);

        // 左侧按钮组
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        JButton addButton = new JButton("添加预约");
        deleteButton = new JButton("取消预约");
        JButton refreshButton = new JButton("刷新");
        addButton.setFont(MAIN_FONT);
        deleteButton.setFont(MAIN_FONT);
        deleteButton.setEnabled(false); // 初始无选中行，置灰
        refreshButton.setFont(MAIN_FONT);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // 右侧搜索组
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        JLabel searchLabel = new JLabel("搜索:");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        searchLabel.setFont(MAIN_FONT);
        searchField.setFont(MAIN_FONT);
        searchButton.setFont(MAIN_FONT);
        searchGroup.add(searchLabel);
        searchGroup.add(searchField);
        searchGroup.add(searchButton);

        // 组装顶部栏
        topBar.add(buttonPanel);
        topBar.add(Box.createHorizontalGlue());
        topBar.add(searchGroup);

        // 表格区
        tableModel = new ReservationTableModel();
        reservationTable = new JTable(tableModel);
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.setAutoCreateRowSorter(true);
        reservationTable.setFont(MAIN_FONT);
        reservationTable.getTableHeader().setFont(MAIN_FONT.deriveFont(Font.BOLD));
        reservationTable.setSelectionBackground(new Color(173, 216, 230));
        reservationTable.setSelectionForeground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("预约列表"));
        scrollPane.setPreferredSize(new Dimension(0, 300));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // 组装到主面板
        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 事件绑定
        addButton.addActionListener(e -> showAddReservationDialog());
        deleteButton.addActionListener(e -> cancelSelectedReservation());
        refreshButton.addActionListener(e -> loadReservations());
        searchButton.addActionListener(e -> searchReservations(searchField.getText()));
        // 表格选中监听（控制取消按钮状态）
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            deleteButton.setEnabled(selectedRow != -1);
        });
    }

    private void loadReservations() {
        try {
            List<Reservation> reservations = fetchReservationsFromDatabase();
            tableModel.setReservations(reservations);
            tableModel.fireTableDataChanged();
            deleteButton.setEnabled(false); // 刷新后重置按钮状态
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "加载预约数据失败", ex);
            JOptionPane.showMessageDialog(this, "加载预约数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Reservation> fetchReservationsFromDatabase() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql="select B.bname,A.bid,A.uid,A.id,A.start_date,A.end_date from reservation as A inner join bookinfo as B on A.bid=B.bid";
        m_query.mysqlConnect();
        ResultSet rset=m_query.selectQuery(1,new String[]{sql});
        while (rset.next()) {
            Reservation reservation = new Reservation();
            reservation.setId(rset.getInt("id"));
            reservation.setUserId(rset.getInt("uid"));
            reservation.setBookId(rset.getInt("bid"));
            reservation.setBookTitle(rset.getString("bname"));
            reservation.setReservationDate(rset.getDate("start_date"));
            reservation.setExpirationDate(rset.getDate("end_date"));
            reservations.add(reservation);
        }
        m_query.mysqlDisconnect();
        return reservations;
    }

    private void searchReservations(String keyword) {
        try {
            List<Reservation> reservations = searchReservationsFromDatabase(keyword);
            tableModel.setReservations(reservations);
            tableModel.fireTableDataChanged();
            deleteButton.setEnabled(false);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "搜索预约数据失败", ex);
            JOptionPane.showMessageDialog(this, "搜索预约数据失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Reservation> searchReservationsFromDatabase(String keyword) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql="select B.bname,A.bid,A.uid,A.id,A.start_date,A.end_date from reservation as A inner join bookinfo as B on A.bid=B.bid where A.id like ? or A.uid like ? or A.bid like ?";
        String param="%"+keyword+"%";
        m_query.mysqlConnect();
        ResultSet rset=m_query.selectQuery(4,new String[]{sql,param,param,param});
        while (rset.next()) {
            Reservation reservation = new Reservation();
            reservation.setId(rset.getInt("id"));
            reservation.setUserId(rset.getInt("uid"));
            reservation.setBookId(rset.getInt("bid"));
            reservation.setBookTitle(rset.getString("bname"));
            reservation.setReservationDate(rset.getDate("start_date"));
            reservation.setExpirationDate(rset.getDate("end_date"));
            reservations.add(reservation);
        }
        return reservations;
    }

    private void showAddReservationDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "添加预约", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userIdLabel = new JLabel("用户ID:");
        JTextField userIdField = new JTextField(15);

        JLabel bookIdLabel = new JLabel("图书ID:");
        JTextField bookIdField = new JTextField(15);

        JLabel reservationDateLabel = new JLabel("预约日期 (yyyy-MM-dd):");
        JTextField reservationDateField = new JTextField(15);

        JLabel expirationDateLabel = new JLabel("过期日期 (yyyy-MM-dd):");
        JTextField expirationDateField = new JTextField(15);

        userIdLabel.setFont(MAIN_FONT);
        userIdField.setFont(MAIN_FONT);
        bookIdLabel.setFont(MAIN_FONT);
        bookIdField.setFont(MAIN_FONT);
        reservationDateLabel.setFont(MAIN_FONT);
        reservationDateField.setFont(MAIN_FONT);
        expirationDateLabel.setFont(MAIN_FONT);
        expirationDateField.setFont(MAIN_FONT);

        // 自动填充日期
        java.util.Date currentDate = new java.util.Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // 设置预约日期为今天
        java.sql.Date reservationDate = new java.sql.Date(currentDate.getTime());
        reservationDateField.setText(reservationDate.toString());

        // 设置过期日期为一周后
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        java.sql.Date expirationDate = new java.sql.Date(calendar.getTimeInMillis());
        expirationDateField.setText(expirationDate.toString());

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(userIdLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(userIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(bookIdLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(bookIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(reservationDateLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(reservationDateField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(expirationDateLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(expirationDateField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.setFont(MAIN_FONT);
        cancelButton.setFont(MAIN_FONT);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            try {
                int userId = Integer.parseInt(userIdField.getText());
                int bookId = Integer.parseInt(bookIdField.getText());
                // 明确指定Date类型为java.sql.Date
                //java.sql.Date reservationDate = java.sql.Date.valueOf(reservationDateField.getText());
                //java.sql.Date expirationDate = java.sql.Date.valueOf(expirationDateField.getText());

                // 验证过期日期是否在预约日期之后
                if (expirationDate.before(reservationDate)) {
                    JOptionPane.showMessageDialog(dialog, "过期日期必须晚于预约日期", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                addReservationToDatabase(userId, bookId, reservationDate, expirationDate);
                loadReservations();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "用户ID和图书ID必须是数字", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "日期格式不正确，请使用yyyy-MM-dd格式", "输入错误", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "添加预约失败", ex);
                JOptionPane.showMessageDialog(dialog, "添加预约失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void addReservationToDatabase(int userId, int bookId, java.sql.Date reservationDate, java.sql.Date expirationDate) throws SQLException {
        String sql = "INSERT INTO reservation (uid, bid, start_date, end_date) VALUES (?, ?, ?, ?)";
        m_query.mysqlConnect();

        int affectRows=m_query.updateQuery(5,new String[]{sql,String.valueOf(userId),String.valueOf(bookId),reservationDate.toString(),expirationDate.toString()});
        if (affectRows > 0) {
            JOptionPane.showMessageDialog(this, "预约添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "预约添加失败", "错误", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void cancelSelectedReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要取消的预约", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int reservationId = tableModel.getReservationAt(selectedRow).getId();
        //String status = tableModel.getReservationAt(selectedRow).getStatus();
        //if (!"待处理".equals(status)) {
            //JOptionPane.showMessageDialog(this, "只有'待处理'状态的预约可以取消", "提示", JOptionPane.INFORMATION_MESSAGE);
            //return;
       //}
        int confirm = JOptionPane.showConfirmDialog(this, "确定要取消这个预约吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                cancelReservationInDatabase(reservationId);
                loadReservations();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "取消预约失败", ex);
                JOptionPane.showMessageDialog(this, "取消预约失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelReservationInDatabase(int reservationId) throws SQLException {
        String sql = "delete from reservation where id=?";
        m_query.mysqlConnect();
        int affectRow=m_query.updateQuery(2,new String[]{sql,String.valueOf(reservationId)});
        if (affectRow > 0) {
            JOptionPane.showMessageDialog(this, "预约已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "预约取消失败，可能状态已变更", "错误", JOptionPane.ERROR_MESSAGE);
        }


    }

    // 预约数据模型类
    private static class Reservation {
        private int id;
        private int userId;
        //private String username;
        private int bookId;
        private String bookTitle;
        private java.sql.Date reservationDate;
        private java.sql.Date expirationDate;
        //private String status;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        //public String getUsername() { return username; }
        //public void setUsername(String username) { this.username = username; }
        public int getBookId() { return bookId; }
        public void setBookId(int bookId) { this.bookId = bookId; }
        public String getBookTitle() { return bookTitle; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
        public java.sql.Date getReservationDate() { return reservationDate; }
        public void setReservationDate(java.sql.Date reservationDate) { this.reservationDate = reservationDate; }
        public java.sql.Date getExpirationDate() { return expirationDate; }
        public void setExpirationDate(java.sql.Date expirationDate) { this.expirationDate = expirationDate; }
        //public String getStatus() { return status; }
        //public void setStatus(String status) { this.status = status; }
    }

    // 表格模型类
    private static class ReservationTableModel extends AbstractTableModel {
        private List<Reservation> reservations = new ArrayList<>();
        private String[] columnNames = {"ID", "用户ID", "图书ID", "图书标题", "预约日期", "过期日期"};

        public void setReservations(List<Reservation> reservations) {
            this.reservations = reservations;
        }

        public Reservation getReservationAt(int row) {
            return reservations.get(row);
        }

        @Override
        public int getRowCount() {
            return reservations.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Reservation reservation = reservations.get(rowIndex);
            switch (columnIndex) {
                case 0: return reservation.getId();
                case 1: return reservation.getUserId();
                //case 2: return reservation.getUsername();
                case 2: return reservation.getBookId();
                case 3: return reservation.getBookTitle();
                case 4: return reservation.getReservationDate();
                case 5: return reservation.getExpirationDate();
                //case 7: return reservation.getStatus();
                default: return null;
            }
        }
    }
}
