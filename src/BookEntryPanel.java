import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class BookEntryPanel extends JPanel {

    private SqlQuery m_query;
    // Swing 组件
    private JTextField txtISBN, txtTitle, txtAuthor, txtPublisher, txtYear, txtTotal, txtAvailable;
    private JComboBox<String> cmbCategory;

    public BookEntryPanel(SqlQuery query) {
        m_query=query;
        // 设置面板背景色
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 创建表单面板（使用 GroupLayout 实现更灵活的布局）
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // 添加组件到面板
        JLabel lblTitle = new JLabel("图书录入");
        lblTitle.setFont(new Font("微软雅黑", Font.BOLD, 24));
        lblTitle.setForeground(new Color(51, 51, 51));

        JLabel lblISBN = new JLabel("ISBN（唯一标识）:");
        lblISBN.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtISBN = new JTextField();
        txtISBN.setPreferredSize(new Dimension(200, 30));

        JLabel lblBookTitle = new JLabel("书名:");
        lblBookTitle.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtTitle = new JTextField();
        txtTitle.setPreferredSize(new Dimension(200, 30));

        JLabel lblAuthor = new JLabel("作者:");
        lblAuthor.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtAuthor = new JTextField();
        txtAuthor.setPreferredSize(new Dimension(200, 30));

        JLabel lblPublisher = new JLabel("出版社:");
        lblPublisher.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtPublisher = new JTextField();
        txtPublisher.setPreferredSize(new Dimension(200, 30));

        JLabel lblYear = new JLabel("出版年份:");
        lblYear.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtYear = new JTextField();
        txtYear.setPreferredSize(new Dimension(200, 30));

        JLabel lblCategory = new JLabel("分类:");
        lblCategory.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        String[] categories = {"计算机", "文学", "历史", "科学", "艺术", "教育", "其他"};
        cmbCategory = new JComboBox<>(categories);
        cmbCategory.setPreferredSize(new Dimension(200, 30));

        JLabel lblTotal = new JLabel("总数量:");
        lblTotal.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtTotal = new JTextField();
        txtTotal.setPreferredSize(new Dimension(200, 30));

        JLabel lblAvailable = new JLabel("可借数量:");
        lblAvailable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtAvailable = new JTextField();
        txtAvailable.setPreferredSize(new Dimension(200, 30));

        JButton btnSubmit = new JButton("提交录入");
        btnSubmit.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(new Color(0, 123, 255));
        btnSubmit.setPreferredSize(new Dimension(120, 35));
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveBookToDB(); // 点击按钮时执行“写入数据库”逻辑
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        JButton btnClear = new JButton("清空表单");
        btnClear.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnClear.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(204, 204, 204));
        btnClear.setPreferredSize(new Dimension(120, 35));
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm(); // 清空表单
            }
        });

        // 布局设置
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(lblTitle)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblISBN)
                                        .addComponent(lblBookTitle)
                                        .addComponent(lblAuthor)
                                        .addComponent(lblPublisher)
                                        .addComponent(lblYear)
                                        .addComponent(lblCategory)
                                        .addComponent(lblTotal)
                                        .addComponent(lblAvailable))
                                .addGap(20)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(txtISBN)
                                        .addComponent(txtTitle)
                                        .addComponent(txtAuthor)
                                        .addComponent(txtPublisher)
                                        .addComponent(txtYear)
                                        .addComponent(cmbCategory)
                                        .addComponent(txtTotal)
                                        .addComponent(txtAvailable))
                        )
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSubmit)
                                .addGap(20)
                                .addComponent(btnClear))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addGap(20)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblISBN)
                                .addComponent(txtISBN))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblBookTitle)
                                .addComponent(txtTitle))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAuthor)
                                .addComponent(txtAuthor))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblPublisher)
                                .addComponent(txtPublisher))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblYear)
                                .addComponent(txtYear))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblCategory)
                                .addComponent(cmbCategory))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblTotal)
                                .addComponent(txtTotal))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAvailable)
                                .addComponent(txtAvailable))
                        .addGap(20)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(btnSubmit)
                                .addComponent(btnClear))
        );
    }

    // 【关键】将表单数据写入 MySQL 数据库
    private void saveBookToDB() throws SQLException {
        // 1. 获取表单输入（trim() 去除首尾空格）
        String isbn = txtISBN.getText().trim();
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String publisher = txtPublisher.getText().trim();
        String yearStr = txtYear.getText().trim();
        String totalStr = txtTotal.getText().trim();
        String availableStr = txtAvailable.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();

        // 2. 简单校验（非空、格式）
        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || publisher.isEmpty()
                || yearStr.isEmpty() || totalStr.isEmpty() || availableStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int year, total, available;
        try {
            year = Integer.parseInt(yearStr);       // 转换为整数
            total = Integer.parseInt(totalStr);
            available = Integer.parseInt(availableStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "年份/数量必须是数字！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (available > total || available < 0) {
            JOptionPane.showMessageDialog(this, "可借数量不能大于总数量或为负数！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. JDBC 连接数据库并插入数据
        String sql = "INSERT INTO bookinfo (isbn, bname, author, publisher, publication_year,category, available,ref_cnt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?,?)";
        m_query.mysqlConnect();
        int affectedRows = m_query.updateQuery(9, new String[]{sql, isbn, title, author, publisher, yearStr, category, "1", "0"});
        if (affectedRows > 0) {
            JOptionPane.showMessageDialog(this, "图书录入成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); // 清空表单，方便继续录入
        } else {
            JOptionPane.showMessageDialog(this, "录入失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
        }

        //同样还要插入一条book_count记录
        try{
            sql="insert into book_count (author,bname,availd,total) value (?,?,?,?)";
            m_query.mysqlConnect();
            m_query.updateQuery(5,new String[]{sql,author,title,availableStr,totalStr});
        }catch (SQLException e){
            throw e;
        }

    }

    // 清空表单内容
    private void clearForm() {
        txtISBN.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtPublisher.setText("");
        txtYear.setText("");
        txtTotal.setText("");
        txtAvailable.setText("");
        cmbCategory.setSelectedIndex(0); // 重置分类为第一个选项
    }


}