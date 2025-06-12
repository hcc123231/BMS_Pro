import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate; // 添加缺少的导入
import java.util.ArrayList;
import java.util.List;

public class BookSearchPanel extends JPanel {
    // MySQL 配置
    /*private static final String DB_URL = "jdbc:mysql://localhost:3306/bms_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Lqf123000@";*/

    // 组件声明
    private JTextField txtSearch, txtTitle, txtAuthor, txtIsbn, txtPublisher;
    private JComboBox<String> categoryComboBox, availabilityComboBox;
    private JButton btnSearch, btnReset, btnViewDetails;
    private JTable tableBooks;
    private DefaultTableModel modelBooks;
    private JSpinner publicationYearSpinner;
    private JFrame m_frame;
    private SqlQuery m_query;

    public BookSearchPanel(JFrame frame,SqlQuery query) throws SQLException {
        m_frame=frame;
        m_query=query;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        initComponents();
        loadCategories(); // 加载图书分类
        loadBooks(); // 加载所有图书
    }

    // 初始化组件
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
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
        btnSearch.addActionListener(e -> {
            try {
                performQuickSearch();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        searchPanel.add(btnSearch, gbc);

        btnReset = new JButton("重置");
        btnReset.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnReset.setForeground(Color.WHITE);
        btnReset.setBackground(new Color(170, 170, 170));
        btnReset.addActionListener(e -> {
            try {
                resetSearch();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
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
        btnAdvancedSearch.addActionListener(e -> {
            try {
                performAdvancedSearch();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 5, 10);
        searchPanel.add(btnAdvancedSearch, gbc);

        // 中间表格区域
        // 修改表格列名（第132行）
        String[] columns = {"图书ID", "书名", "作者", "ISBN", "出版年份", "分类", "可借数量", "状态"};
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
                        try {
                            showBookDetails(bookId);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
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
                try {
                    showBookDetails(bookId);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
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


    // 加载图书分类（修改表名和字段名）
    private void loadCategories() throws SQLException {
        categoryComboBox.addItem("全部");
        m_query.mysqlConnect();
        String sql="select distinct category from bookinfo order by category";
        ResultSet rset=m_query.selectQuery(1,new String[]{sql});
        while (rset.next()) {
            categoryComboBox.addItem(rset.getString("category"));
        }

    }

    // 加载所有图书
    private void loadBooks() throws SQLException {
        modelBooks.setRowCount(0);
        String sql="select * from bookinfo";
        m_query.mysqlConnect();
        ResultSet rset=m_query.selectQuery(1,new String[]{sql});
        while (rset.next()) {
            int available = rset.getInt("available");
            String status = available > 0 ? "可借阅" : "已借出";

            // 修复：统一用 Date 转年份
            Date pubDate = rset.getDate("publication_year");
            int pubYear = pubDate != null ? pubDate.toLocalDate().getYear() : 0;

            modelBooks.addRow(new Object[]{
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getString("author"),
                    rset.getString("isbn"),
                    pubYear,  // 正确的年份
                    rset.getString("category"),
                    rset.getInt("available"),
                    status
            });
        }



    }

    // 执行快速搜索
    private void performQuickSearch() throws SQLException {
        String keyword = txtSearch.getText().trim();
        modelBooks.setRowCount(0); // 清空表格

        if (keyword.isEmpty()) {
            loadBooks(); // 关键词为空时加载全部
            return;
        }
        String sql="select * from book_count as A inner join bookinfo as B on A.bname=B.bname and A.author=B.author where B.bname like ? or B.author like ? or B.isbn like ? order by B.bname";
        m_query.mysqlConnect();
        String param="%"+keyword+"%";
        ResultSet rset=m_query.selectQuery(4,new String[]{sql,param,param,param});
        while (rset.next()) {
            int available = rset.getInt("available");
            int total = rset.getInt("total");
            String status = available > 0 ? "可借阅" : "已借出";

            // 处理出版年份
            Date pubDate = rset.getDate("publication_year");
            int pubYear = pubDate != null ? pubDate.getYear() + 1900 : 0;

            modelBooks.addRow(new Object[]{
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getString("author"),
                    rset.getString("isbn"),
                    "", // 出版社字段留空
                    pubYear,
                    rset.getString("category"),
                    available,
                    status
            });
        }

    }

    // 执行高级搜索
    private void performAdvancedSearch() throws SQLException {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String isbn = txtIsbn.getText().trim();
        String publisher = txtPublisher.getText().trim(); // 注意：你的数据库中无此字段，此条件无效
        String category = (String) categoryComboBox.getSelectedItem();
        int publicationYear = (int) publicationYearSpinner.getValue();
        String availability = (String) availabilityComboBox.getSelectedItem();
        List<String> params =new ArrayList<>();
        modelBooks.setRowCount(0); // 清空表格
        m_query.mysqlConnect();
        String sql="select * from book_count as A inner join bookinfo as B on A.bname=B.bname and A.author=B.author where 1=1 ";
        if (!title.isEmpty()) {
            sql+="and B.bname = ? ";
            params.add(title);

        }

        if (!author.isEmpty()) {
            sql+="and B.author = ? ";
            params.add(author);
        }

        if (!isbn.isEmpty()) {
            sql+="and B.isbn = ? ";
            params.add(isbn);
        }

        // 注意：出版社字段在你的数据库中不存在，此条件可移除或保留但无效
        if (!publisher.isEmpty()) {
            sql+="and B.publisher = ? ";
            params.add(publisher);
        }

        if (!category.equals("全部")) {

            sql+="and B.category = ? ";
            params.add(category);
        }

        if (publicationYear != 2000) { // 如果不是默认值

            sql+="and year(B.publication_year) = ? ";
            params.add(String.valueOf(publicationYear));
        }

        if (!availability.equals("全部")) {
            if (availability.equals("可借阅")) {

                sql+="and A.avalid>0 ";
            } else {
                sql+="and A.avalid=0 ";
            }
        }
        System.out.println("size:"+params.size());
        String aaa[]=new String[params.size()+1];
        aaa[0]=sql;
        for(int i=1;i<=params.size();i++){
            aaa[i]=params.get(i-1);
        }
        ResultSet rset=m_query.selectQuery(params.size()+1,aaa);
        while (rset.next()) {
            int available = rset.getInt("available");
            int total = rset.getInt("total");
            String status = available > 0 ? "可借阅" : "已借出";

            // 处理出版年份
            Date pubDate = rset.getDate("publication_year");
            int pubYear = pubDate != null ? pubDate.getYear() + 1900 : 0;

            modelBooks.addRow(new Object[]{
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getString("author"),
                    rset.getString("isbn"),
                    "", // 出版社字段留空
                    pubYear,
                    rset.getString("category"),
                    available,
                    status
            });
        }

        /*try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT bi.bid, bi.bname, bi.author, bi.isbn, ");
            sqlBuilder.append("bi.publication_year, bi.category, bc.avalid, bc.total ");
            sqlBuilder.append("FROM bookinfo bi ");
            sqlBuilder.append("JOIN book_count bc ON bi.bname = bc.bname AND bi.author = bc.author ");
            sqlBuilder.append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (!title.isEmpty()) {
                sqlBuilder.append("AND bi.bname LIKE ? ");
                params.add("%" + title + "%");
            }

            if (!author.isEmpty()) {
                sqlBuilder.append("AND bi.author LIKE ? ");
                params.add("%" + author + "%");
            }

            if (!isbn.isEmpty()) {
                sqlBuilder.append("AND bi.isbn LIKE ? ");
                params.add("%" + isbn + "%");
            }

            // 注意：出版社字段在你的数据库中不存在，此条件可移除或保留但无效
            if (!publisher.isEmpty()) {
                sqlBuilder.append("AND 1=2 "); // 使条件永远不成立
            }

            if (!category.equals("全部")) {
                sqlBuilder.append("AND bi.category = ? ");
                params.add(category);
            }

            if (publicationYear != 2000) { // 如果不是默认值
                sqlBuilder.append("AND YEAR(bi.publication_year) = ? ");
                params.add(publicationYear);
            }

            if (!availability.equals("全部")) {
                if (availability.equals("可借阅")) {
                    sqlBuilder.append("AND bc.avalid > 0 ");
                } else {
                    sqlBuilder.append("AND bc.avalid = 0 ");
                }
            }

            sqlBuilder.append("ORDER BY bi.bname");

            try (PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int available = rs.getInt("avalid");
                        int total = rs.getInt("total");
                        String status = available > 0 ? "可借阅" : "已借出";

                        // 处理出版年份
                        Date pubDate = rs.getDate("publication_year");
                        int pubYear = pubDate != null ? pubDate.getYear() + 1900 : 0;

                        modelBooks.addRow(new Object[]{
                                rs.getInt("bid"),
                                rs.getString("bname"),
                                rs.getString("author"),
                                rs.getString("isbn"),
                                "", // 出版社字段留空
                                pubYear,
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
        }*/
    }

    // 重置搜索条件
    private void resetSearch() throws SQLException {
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
    private void showBookDetails(int bookId) throws SQLException {
        String sql = "SELECT bi.bid, bi.bname, bi.author, bi.isbn, " +
                "bi.publication_year, bi.category, bc.availd, bc.total " +
                "FROM bookinfo bi " +
                "JOIN book_count bc ON bi.bname = bc.bname AND bi.author = bc.author " +
                "WHERE bi.bid = ?";
        m_query.mysqlConnect();
        ResultSet rset=m_query.selectQuery(2,new String[]{sql,String.valueOf(bookId)});
        /*String sql = "SELECT bi.bid, bi.bname, bi.author, bi.isbn, " +
                "bi.publication_year, bi.category, bc.avalid, bc.total " +
                "FROM bookinfo bi " +
                "JOIN book_count bc ON bi.bname = bc.bname AND bi.author = bc.author " +
                "WHERE bi.bid = ?";*/
        if (rset.next()) {
            // 创建详情对话框
            JDialog dialog;
            Window mainWindow = SwingUtilities.getWindowAncestor(this);
            if (mainWindow != null) {
                dialog = new JDialog(m_frame, "图书详情", true);
            } else {
                dialog = new JDialog();
                dialog.setTitle("图书详情");
            }

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

            JLabel lblBookId = new JLabel(rset.getString("bid"));
            lblBookId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 1;
            panel.add(lblBookId, gbc);

            JLabel lblBookTitle = new JLabel("书名:");
            lblBookTitle.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(lblBookTitle, gbc);

            JLabel lblTitleValue = new JLabel(rset.getString("bname"));
            lblTitleValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 2;
            panel.add(lblTitleValue, gbc);

            JLabel lblBookAuthor = new JLabel("作者:");
            lblBookAuthor.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(lblBookAuthor, gbc);

            JLabel lblAuthorValue = new JLabel(rset.getString("author"));
            lblAuthorValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 3;
            panel.add(lblAuthorValue, gbc);

            JLabel lblBookIsbn = new JLabel("ISBN:");
            lblBookIsbn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(lblBookIsbn, gbc);

            JLabel lblIsbnValue = new JLabel(rset.getString("isbn"));
            lblIsbnValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 4;
            panel.add(lblIsbnValue, gbc);

            // 出版社字段留空或添加其他信息
            JLabel lblBookPublisher = new JLabel("出版社:");
            lblBookPublisher.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 5;
            panel.add(lblBookPublisher, gbc);

            JLabel lblPublisherValue = new JLabel("");
            lblPublisherValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 5;
            panel.add(lblPublisherValue, gbc);

            JLabel lblBookYear = new JLabel("出版年份:");
            lblBookYear.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 6;
            panel.add(lblBookYear, gbc);

            Date pubDate = rset.getDate("publication_year");
            int pubYear = pubDate != null ? pubDate.getYear() + 1900 : 0;
            JLabel lblYearValue = new JLabel(String.valueOf(pubYear));
            lblYearValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 6;
            panel.add(lblYearValue, gbc);

            JLabel lblBookCategory = new JLabel("分类:");
            lblBookCategory.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 7;
            panel.add(lblBookCategory, gbc);

            JLabel lblCategoryValue = new JLabel(rset.getString("category"));
            lblCategoryValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 7;
            panel.add(lblCategoryValue, gbc);

            JLabel lblBookTotal = new JLabel("总藏书量:");
            lblBookTotal.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 8;
            panel.add(lblBookTotal, gbc);

            JLabel lblTotalValue = new JLabel(String.valueOf(rset.getInt("total")));
            lblTotalValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 8;
            panel.add(lblTotalValue, gbc);

            JLabel lblBookAvailable = new JLabel("可借数量:");
            lblBookAvailable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 9;
            panel.add(lblBookAvailable, gbc);

            JLabel lblAvailableValue = new JLabel(String.valueOf(rset.getInt("availd")));
            lblAvailableValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 9;
            panel.add(lblAvailableValue, gbc);

            // 状态
            int available = rset.getInt("availd");
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
        /*try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // 联合查询bookinfo和book_count表

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // 创建详情对话框
                        JDialog dialog;
                        Window mainWindow = SwingUtilities.getWindowAncestor(this);
                        if (mainWindow != null) {
                            dialog = new JDialog(m_frame, "图书详情", true);
                        } else {
                            dialog = new JDialog();
                            dialog.setTitle("图书详情");
                        }

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

                        JLabel lblBookId = new JLabel(rs.getString("bid"));
                        lblBookId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 1;
                        panel.add(lblBookId, gbc);

                        JLabel lblBookTitle = new JLabel("书名:");
                        lblBookTitle.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 2;
                        panel.add(lblBookTitle, gbc);

                        JLabel lblTitleValue = new JLabel(rs.getString("bname"));
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

                        // 出版社字段留空或添加其他信息
                        JLabel lblBookPublisher = new JLabel("出版社:");
                        lblBookPublisher.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 5;
                        panel.add(lblBookPublisher, gbc);

                        JLabel lblPublisherValue = new JLabel("");
                        lblPublisherValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 5;
                        panel.add(lblPublisherValue, gbc);

                        JLabel lblBookYear = new JLabel("出版年份:");
                        lblBookYear.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 6;
                        panel.add(lblBookYear, gbc);

                        Date pubDate = rs.getDate("publication_year");
                        int pubYear = pubDate != null ? pubDate.getYear() + 1900 : 0;
                        JLabel lblYearValue = new JLabel(String.valueOf(pubYear));
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

                        JLabel lblTotalValue = new JLabel(String.valueOf(rs.getInt("total")));
                        lblTotalValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 8;
                        panel.add(lblTotalValue, gbc);

                        JLabel lblBookAvailable = new JLabel("可借数量:");
                        lblBookAvailable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 0;
                        gbc.gridy = 9;
                        panel.add(lblBookAvailable, gbc);

                        JLabel lblAvailableValue = new JLabel(String.valueOf(rs.getInt("avalid")));
                        lblAvailableValue.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                        gbc.gridx = 1;
                        gbc.gridy = 9;
                        panel.add(lblAvailableValue, gbc);

                        // 状态
                        int available = rs.getInt("avalid");
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
        }*/
    }

    // 测试入口
    /*public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("图书检索 - 校园图书管理系统");
            BookSearchPanel panel = new BookSearchPanel();
            frame.add(panel);
            frame.setSize(1000, 650);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }*/
}