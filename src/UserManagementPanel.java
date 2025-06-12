import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


public class UserManagementPanel extends JPanel {
    // 组件声明
    private JPanel mainPanel;
    private JTextField txtUserId, txtUsername, txtSearch,txtPassword;
    private JButton btnAdd, btnDelete, btnSearch, btnRefresh;
    private JTable tableUsers;
    private DefaultTableModel modelUsers;
    private JComboBox<String> roleComboBox;
    private SqlQuery m_query;


    public UserManagementPanel(SqlQuery query) {
        setBackground(Color.WHITE);
        m_query=query;
        setLayout(new BorderLayout());

        initComponents();
        loadUsers(); // 加载所有用户
    }

    // 初始化组件
    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部搜索区域
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("搜索用户:");
        lblSearch.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(lblSearch);

        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        btnSearch = new JButton("搜索");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(49, 130, 206));
        btnSearch.addActionListener(e -> searchUsers());
        searchPanel.add(btnSearch);

        btnRefresh = new JButton("刷新");
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(170, 170, 170));
        btnRefresh.addActionListener(e -> loadUsers());
        searchPanel.add(btnRefresh);

        // 中间表格区域
        String[] columns = {"用户ID", "用户名", "角色", "创建时间"};
        modelUsers = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableUsers = new JTable(modelUsers);
        tableUsers.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableUsers.setRowHeight(28);
        tableUsers.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        // 双击表格自动填充用户信息
        tableUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableUsers.getSelectedRow();
                    if (row >= 0) {
                        txtUserId.setText(modelUsers.getValueAt(row, 0).toString());
                        txtUsername.setText(modelUsers.getValueAt(row, 1).toString());
                        roleComboBox.setSelectedItem(modelUsers.getValueAt(row, 2));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableUsers);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 底部表单区域
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        // 用户ID
        JLabel lblUserId = new JLabel("用户ID:");
        lblUserId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblUserId, gbc);

        txtUserId = new JTextField(15);
        txtUserId.setEditable(false); // 禁止编辑用户ID
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(txtUserId, gbc);

        // 用户名
        JLabel lblUsername = new JLabel("用户名:");
        lblUsername.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 2;
        gbc.gridy = 0;
        formPanel.add(lblUsername, gbc);

        txtUsername = new JTextField(15);
        gbc.gridx = 3;
        gbc.gridy = 0;
        formPanel.add(txtUsername, gbc);
        // 添加密码输入框
        JLabel lblPassword = new JLabel("密码:");
        lblPassword.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(txtPassword, gbc);
        // 用户角色
        JLabel lblRole = new JLabel("角色:");
        lblRole.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblRole, gbc);

        String[] roles = {"普通用户", "管理员"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(roleComboBox, gbc);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = new JButton("新增用户");
        btnAdd.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBackground(new Color(49, 130, 206));
        btnAdd.addActionListener(e -> addUser());
        buttonPanel.add(btnAdd);

        btnDelete = new JButton("删除用户");
        btnDelete.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setBackground(new Color(220, 53, 69));


        btnDelete.addActionListener(e -> deleteUser());


        buttonPanel.add(btnDelete);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        // 组装主面板
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
    // 加载所有用户
    private void loadUsers() {
        modelUsers.setRowCount(0); // 清空表格
        try{
            String sql="select uid,account,chara,created from user";
            m_query.mysqlConnect();
            ResultSet rset=m_query.selectQuery(1,new String[]{sql});
            while (rset.next()) {
                modelUsers.addRow(new Object[]{
                        rset.getInt("uid"),
                        rset.getString("account"),
                        rset.getString("chara"),
                        rset.getString("created")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // 搜索功能
    private void searchUsers() {
        String keyword = txtSearch.getText().trim();
        modelUsers.setRowCount(0); // 清空表格

        if (keyword.isEmpty()) {
            loadUsers(); // 关键词为空时加载全部
            return;
        }
        try{
            String sql="select uid,account,chara,created from user where uid like ? or account like ? or created like ?";
            String param="%"+keyword+"%";
            m_query.mysqlConnect();
            ResultSet rset=m_query.selectQuery(4,new String[]{sql,param,param,param});
            while (rset.next()) {
                modelUsers.addRow(new Object[]{
                        rset.getInt("uid"),
                        rset.getString("account"),
                        rset.getString("chara"),
                        rset.getString("created")
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // 添加用户
    private void addUser() {
        String username = txtUsername.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();
        //String userId=txtUserId.getText().trim();
        String passwd=txtPassword.getText().trim();
        String chara = "管理员".equals(role) ? "2" : "0"; // 角色转换

        // 非空校验
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 检查用户名是否已存在
        try{
            String sql="select count(*) as cnt from user where account=?";

            m_query.mysqlConnect();
            ResultSet rset=m_query.selectQuery(2,new String[]{sql,username});
            rset.next();
            if (rset.getInt("cnt")>=1) {
                JOptionPane.showMessageDialog(this, "用户名已存在！", "错误", JOptionPane.ERROR_MESSAGE);
                throw new SQLException("失败");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // 添加用户（注意：number字段需要默认值或用户输入）
        try{
            String sql="insert into user (account,password,chara,created) value (?,?,?,now())";

            m_query.mysqlConnect();
            int affectRows=m_query.updateQuery(4,new String[]{sql,username,passwd,chara});
            if (affectRows<=0) {
                JOptionPane.showMessageDialog(this, "用户添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
                throw new SQLException("失败");
            }
            JOptionPane.showMessageDialog(this, "添加成功！", "ok", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // 删除用户
    private void deleteUser() {
        String userIdStr = txtUserId.getText().trim();

        // 非空校验
        if (userIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的用户！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 数字校验
        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "用户ID必须是数字！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 确认删除
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除用户ID为 " + userId + " 的用户吗？此操作不可撤销！",
                "确认删除", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 检查用户是否有未归还的图书（practical_date > CURDATE()表示未归还）
        try{
            String sql="select count(*) as cnt from borrow_relation where uid=? and practical_date is null or practical_date is not null and practical_date>curdate() ";
            m_query.mysqlConnect();
            ResultSet rset=m_query.selectQuery(2,new String[]{sql,userIdStr});
            if (rset.next() && rset.getInt("cnt") > 0) {
                JOptionPane.showMessageDialog(this, "该用户有未归还的图书，无法删除！", "错误", JOptionPane.ERROR_MESSAGE);
                throw new SQLException("有未归de地图书");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try{
            String sql="delete from user where uid=? ";
            m_query.mysqlConnect();
            int affectRows=m_query.updateQuery(2,new String[]{sql,userIdStr});
            if(affectRows>=1) {
                JOptionPane.showMessageDialog(this, "用户删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    // 清空表单
    private void clearForm() {
        txtUserId.setText("");
        txtUsername.setText("");
        roleComboBox.setSelectedIndex(0);
    }

}
