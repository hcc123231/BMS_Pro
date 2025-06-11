import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate; // 添加缺少的导入
import java.util.ArrayList;
import java.util.List;

public class BookSearchUI extends JFrame {
    // MySQL 配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "你的密码";

    // 组件声明
    private JPanel mainPanel;
    private JTextField txtSearch, txtTitle, txtAuthor, txtIsbn, txtPublisher;
    private JComboBox<String> categoryComboBox, availabilityComboBox;
    private JButton btnSearch, btnReset, btnViewDetails;
    private JTable tableBooks;
    private DefaultTableModel modelBooks;
    private JSpinner publicationYearSpinner;

    public BookSearchUI() {
        setTitle("图书检索 - 校园图书管理系统");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        initComponents();
        loadCategories(); // 加载图书分类
        loadBooks(); // 加载所有图书
    }

    // 初始化组件
    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部搜索区域
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 快速搜索
        JLabel lblQuickSearch = new JLabel("快速搜索:");
        lblQuickSearch.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        searchPanel.add(lblQuickSearch, gbc);

        txtSearch = new JTextField(30);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        searchPanel.add(txtSearch, gbc);

        btnSearch = new JButton("搜索");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(49, 130, 206));
        btnSearch.addActionListener(e -> performQuickSearch());
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        searchPanel.add(btnSearch, gbc);

        btnReset = new JButton("重置");
        btnReset.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnReset.setForeground(Color.WHITE);
        btnReset.setBackground(new Color(170, 170, 170));
        btnReset.addActionListener(e -> resetSearch());
        gbc.gridx = 5;
        gbc.gridy = 0;
        searchPanel.add(btnReset, gbc);

        // 高级筛选
        JLabel lblAdvancedFilter = new JLabel("高级筛选:");
        lblAdvancedFilter.setFont(new Font("微软雅黑", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 10, 5, 10);
        searchPanel.add(lblAdvancedFilter, gbc);

        // 书名
        JLabel lblTitle = new JLabel("书名:");
        lblTitle.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        searchPanel.add(lblTitle, gbc);

        txtTitle = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        searchPanel.add(txtTitle, gbc);

        // 作者
        JLabel lblAuthor = new JLabel("作者:");
        lblAuthor.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 2;
        gbc.gridy = 2;
        searchPanel.add(lblAuthor, gbc);

        txtAuthor = new JTextField(15);
        gbc.gridx = 3;
        gbc.gridy = 2;
        searchPanel.add(txtAuthor, gbc);

        // ISBN
        JLabel lblIsbn = new JLabel("ISBN:");
        lblIsbn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 4;
        gbc.gridy = 2;
        searchPanel.add(lblIsbn, gbc);

        txtIsbn = new JTextField(15);
        gbc.gridx = 5;
        gbc.gridy = 2;
        searchPanel.add(txtIsbn, gbc);

        // 出版社
        JLabel lblPublisher = new JLabel("出版社:");
        lblPublisher.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        searchPanel.add(lblPublisher, gbc);

        txtPublisher = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 3;
        searchPanel.add(txtPublisher, gbc);

        // 分类
        JLabel lblCategory = new JLabel("分类:");
        lblCategory.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 2;
        gbc.gridy = 3;
        searchPanel.add(lblCategory, gbc);

        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryComboBox.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 3;
        gbc.gridy = 3;
        searchPanel.add(categoryComboBox, gbc);

        // 出版年份
        JLabel lblPublicationYear = new JLabel("出版年份:");
        lblPublicationYear.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 4;
        gbc.gridy = 3;
        searchPanel.add(lblPublicationYear, gbc);

        SpinnerNumberModel yearModel = new SpinnerNumberModel(
                2000, // 默认值
                1900, // 最小值
                LocalDate.now().getYear(), // 最大值
                1     // 步长
        );
        publicationYearSpinner = new JSpinner(yearModel);
        gbc.gridx = 5;
        gbc.gridy = 3;
        searchPanel.add(publicationYearSpinner, gbc);

        // 可用性
        JLabel lblAvailability = new JLabel("可用性:");
        lblAvailability.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        searchPanel.add(lblAvailability, gbc);

        String[] availabilityOptions = {"全部", "可借阅", "已借出"};
        availabilityComboBox = new JComboBox<>(availabilityOptions);
        availabilityComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        availabilityComboBox.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 1;
        gbc.gridy = 4;
        searchPanel.add(availabilityComboBox, gbc);

        // 高级搜索按钮
        JButton btnAdvancedSearch = new JButton("高级搜索");
        btnAdvancedSearch.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnAdvancedSearch.setForeground(Color.WHITE);
        btnAdvancedSearch.setBackground(new Color(49, 130, 206));
        btnAdvancedSearch.addActionListener(e -> performAdvancedSearch());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 5, 10);
        searchPanel.add(btnAdvancedSearch, gbc);

        // 中间表格区域
        String[] columns = {"图书ID", "书名", "作者", "ISBN", "出版社", "出版年份", "分类", "可借数量", "状态"};
        modelBooks = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableBooks = new JTable(modelBooks);
        tableBooks.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tableBooks.setRowHeight(28);
        tableBooks.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));

        // 双击表格查看详情
        tableBooks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableBooks.getSelectedRow();
                    if (row >= 0) {
                        int bookId = (int) modelBooks.getValueAt(row, 0);
                        showBookDetails(bookId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableBooks);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 底部按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        btnViewDetails = new JButton("查看详情");
        btnViewDetails.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnViewDetails.setForeground(Color.WHITE);
        btnViewDetails.setBackground(new Color(49, 130, 206));
        btnViewDetails.addActionListener(e -> {
            int selectedRow = tableBooks.getSelectedRow();
            if (selectedRow >= 0) {
                int bookId = (int) modelBooks.getValueAt(selectedRow, 0);
                showBookDetails(bookId);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择一本书！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        buttonPanel.add(btnViewDetails);

        // 组装主面板
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // 加载图书分类
    private void loadCategories() {
        categoryComboBox.addItem("全部");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT DISTINCT category FROM books ORDER BY category";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    categoryComboBox.addItem(rs.getString("category"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载图书分类失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 加载所有图书
    private void loadBooks() {
        modelBooks.setRowCount(0); // 清空表格

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, title, author, isbn, publisher, publication_year, category, " +
                    "available_quantity, total_quantity " +
                    "FROM books " +
                    "ORDER BY title";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    int available = rs.getInt("available_quantity");
                    int total = rs.getInt("total_quantity");
                    String status = available > 0 ? "可借阅" : "已借出";

                    modelBooks.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getString("publisher"),
                            rs.getInt("publication_year"),
                            rs.getString("category"),
                            available,
                            status
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载图书列表失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 执行快速搜索
    private void performQuickSearch() {
        String keyword = txtSearch.getText().trim();
        modelBooks.setRowCount(0); // 清空表格

        if (keyword.isEmpty()) {
            loadBooks(); // 关键词为空时加载全部
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, title, author, isbn, publisher, publication_year, category, " +
                    "available_quantity, total_quantity " +
                    "FROM books " +
                    "WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR publisher LIKE ? " +
                    "ORDER BY title";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + keyword + "%");
                pstmt.setString(2, "%" + keyword + "%");
                pstmt.setString(3, "%" + keyword + "%");
                pstmt.setString(4, "%" + keyword + "%");

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int available = rs.getInt("available_quantity");
                        int total = rs.getInt("total_quantity");
                        String status = available > 0 ? "可借阅" : "已借出";

                        modelBooks.addRow(new Object[]{
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("isbn"),
                                rs.getString("publisher"),
                                rs.getInt("publication_year"),
                                rs.getString("category"),
                                available,
                                status
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "搜索图书失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 执行高级搜索
    private void performAdvancedSearch() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String isbn = txtIsbn.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();
        int publicationYear = (int) publicationYearSpinner.getValue();
        String availability = (String) availabilityComboBox.getSelectedItem();

        modelBooks.setRowCount(0); // 清空表格

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT id, title, author, isbn, publisher, publication_year, category, ");
            sqlBuilder.append("available_quantity, total_quantity ");
            sqlBuilder.append("FROM books ");
            sqlBuilder.append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (!title.isEmpty()) {
                sqlBuilder.append("AND title LIKE ? ");
                params.add("%" + title + "%");
            }

            if (!author.isEmpty()) {
                sqlBuilder.append("AND author LIKE ? ");
                params.add("%" + author + "%");
            }

            if (!isbn.isEmpty()) {
                sqlBuilder.append("AND isbn LIKE ? ");
                params.add("%" + isbn + "%");
            }

            if (!publisher.isEmpty()) {
                sqlBuilder.append("AND publisher LIKE ? ");
                params.add("%" + publisher + "%");
            }

            if (!category.equals("全部")) {
                sqlBuilder.append("AND category = ? ");
                params.add(category);
            }

            if (publicationYear != 2000) { // 如果不是默认值
                sqlBuilder.append("AND publication_year = ? ");
                params.add(publicationYear);
            }

            if (!availability.equals("全部")) {
                if (availability.equals("可借阅")) {
                    sqlBuilder.append("AND available_quantity > 0 ");
                } else {
                    sqlBuilder.append("AND available_quantity = 0 ");
                }
            }

            sqlBuilder.append("ORDER BY title");

            try (PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int available = rs.getInt("available_quantity");
                        int total = rs.getInt("total_quantity");
                        String status = available > 0 ? "可借阅" : "已借出";

                        modelBooks.addRow(new Object[]{
                                rs.getInt("id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("isbn"),
                                rs.getString("publisher"),
                                rs.getInt("publication_year"),
                                rs.getString("category"),
                                available,
                                status
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "高级搜索失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 重置搜索条件
    private void resetSearch() {
        txtSearch.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtIsbn.setText("");
        txtPublisher.setText("");
        categoryComboBox.setSelectedIndex(0);
        publicationYearSpinner.setValue(2000);
        availabilityComboBox.setSelectedIndex(0);

        loadBooks();
    }

    // 显示图书详情
    private void showBookDetails(int bookId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT * FROM books WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // 创建详情对话框
                        JDialog dialog = new JDialog(this, "图书详情", true);
                        dialog.setSize(500, 400);
                        dialog.setLocationRelativeTo(this);
                        dialog.getContentPane().setBackground(Color.WHITE);

                        JPanel panel = new JPanel(new GridBagLayout());
                        panel.setBackground(Color.WHITE);
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.insets = new Insets(10, 15, 10, 15);
                        gbc.anchor = GridBagConstraints.WEST;

                        // 标题
                        JLabel lblTitle = new JLabel("图书详情");
                        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
                        gbc.gridx = 0;
                        gbc.gridy = 0;
                        gbc.gridwidth = 2;
                        gbc.anchor = GridBagConstraints.CENTER;
                        gbc.insets = new Insets(15, 15, 15, 15);
                        panel.add(lblTitle, gbc);

                        // 图书信息
                        gbc.gridwidth = 1;
                        gbc.anchor = GridBagConstraints.EAST;
                        gbc.insets = new Insets(5, 15, 5, 15);

                        JLabel lblId = new JLabel("图书ID:");
                        lblId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 1;
                        panel.add(lblId, gbc);

                        JLabel lblBookId = new JLabel(rs.getString("id"));
                        lblBookId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 1;
                        panel.add(lblBookId, gbc);

                        JLabel lblBookTitle = new JLabel("书名:");
                        lblBookTitle.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 2;
                        panel.add(lblBookTitle, gbc);

                        JLabel lblTitleValue = new JLabel(rs.getString("title"));
                        lblTitleValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 2;
                        panel.add(lblTitleValue, gbc);

                        JLabel lblBookAuthor = new JLabel("作者:");
                        lblBookAuthor.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 3;
                        panel.add(lblBookAuthor, gbc);

                        JLabel lblAuthorValue = new JLabel(rs.getString("author"));
                        lblAuthorValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 3;
                        panel.add(lblAuthorValue, gbc);

                        JLabel lblBookIsbn = new JLabel("ISBN:");
                        lblBookIsbn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 4;
                        panel.add(lblBookIsbn, gbc);

                        JLabel lblIsbnValue = new JLabel(rs.getString("isbn"));
                        lblIsbnValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 4;
                        panel.add(lblIsbnValue, gbc);

                        JLabel lblBookPublisher = new JLabel("出版社:");
                        lblBookPublisher.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 5;
                        panel.add(lblBookPublisher, gbc);

                        JLabel lblPublisherValue = new JLabel(rs.getString("publisher"));
                        lblPublisherValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 5;
                        panel.add(lblPublisherValue, gbc);

                        JLabel lblBookYear = new JLabel("出版年份:");
                        lblBookYear.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 6;
                        panel.add(lblBookYear, gbc);

                        JLabel lblYearValue = new JLabel(String.valueOf(rs.getInt("publication_year")));
                        lblYearValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 6;
                        panel.add(lblYearValue, gbc);

                        JLabel lblBookCategory = new JLabel("分类:");
                        lblBookCategory.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 7;
                        panel.add(lblBookCategory, gbc);

                        JLabel lblCategoryValue = new JLabel(rs.getString("category"));
                        lblCategoryValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 7;
                        panel.add(lblCategoryValue, gbc);

                        JLabel lblBookTotal = new JLabel("总藏书量:");
                        lblBookTotal.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 8;
                        panel.add(lblBookTotal, gbc);

                        JLabel lblTotalValue = new JLabel(String.valueOf(rs.getInt("total_quantity")));
                        lblTotalValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 8;
                        panel.add(lblTotalValue, gbc);

                        JLabel lblBookAvailable = new JLabel("可借数量:");
                        lblBookAvailable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 9;
                        panel.add(lblBookAvailable, gbc);

                        JLabel lblAvailableValue = new JLabel(String.valueOf(rs.getInt("available_quantity")));
                        lblAvailableValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 9;
                        panel.add(lblAvailableValue, gbc);

                        // 状态
                        int available = rs.getInt("available_quantity");
                        String status = available > 0 ? "可借阅" : "已借出";

                        JLabel lblBookStatus = new JLabel("状态:");
                        lblBookStatus.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 10;
                        panel.add(lblBookStatus, gbc);

                        JLabel lblStatusValue = new JLabel(status);
                        lblStatusValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        lblStatusValue.setForeground(available > 0 ? Color.GREEN : Color.RED);
                        gbc.gridx = 1;
                        gbc.gridy = 10;
                        panel.add(lblStatusValue, gbc);

                        // 确定按钮
                        JButton btnOK = new JButton("确定");
                        btnOK.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                        btnOK.setForeground(Color.WHITE);
                        btnOK.setBackground(new Color(49, 130, 206));
                        btnOK.addActionListener(e -> dialog.dispose());
                        gbc.gridx = 0;
                        gbc.gridy = 11;
                        gbc.gridwidth = 2;
                        gbc.anchor = GridBagConstraints.CENTER;
                        gbc.insets = new Insets(15, 15, 15, 15);
                        panel.add(btnOK, gbc);

                        dialog.add(panel);
                        dialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "未找到图书信息！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "查询图书详情失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 主方法测试
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BookSearchUI ui = new BookSearchUI();
            ui.setVisible(true);
        });
    }
}