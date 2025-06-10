import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BookEntryUI extends JFrame {
    // MySQL 数据库连接信息（修改为你的实际配置）
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "你的MySQL密码";

    // Swing 组件
    private JTextField txtISBN, txtTitle, txtAuthor, txtPublisher, txtYear, txtTotal, txtAvailable;
    private JComboBox<String> cmbCategory;

    public BookEntryUI() {
        // 1. 初始化窗口
        setTitle("图书录入 - 校园图书管理系统");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 关闭后不退出程序
        setLocationRelativeTo(null); // 居中显示
        getContentPane().setBackground(Color.WHITE); // 设置背景色

        // 2. 创建表单面板（使用 GroupLayout 实现更灵活的布局）
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // 3. 添加组件到面板
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
                saveBookToDB(); // 点击按钮时执行“写入数据库”逻辑
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

        // 4. 布局设置
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

        // 5. 将面板加入窗口
        add(panel);
    }

    // 【关键】将表单数据写入 MySQL 数据库
    private void saveBookToDB() {
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

        // 3. JDBC 连接数据库并插入数据
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // SQL：使用 ? 占位符防止 SQL 注入
            String sql = "INSERT INTO books (isbn, title, author, publisher, publication_year, category, total_quantity, available_quantity) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 设置 SQL 参数（与 ? 顺序对应）
                pstmt.setString(1, isbn);
                pstmt.setString(2, title);
                pstmt.setString(3, author);
                pstmt.setString(4, publisher);
                pstmt.setInt(5, year);
                pstmt.setString(6, category);
                pstmt.setInt(7, total);
                pstmt.setInt(8, available);

                // 执行插入（返回影响行数）
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "图书录入成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(); // 清空表单，方便继续录入
                } else {
                    JOptionPane.showMessageDialog(this, "录入失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            // 数据库异常（如：ISBN 重复、连接失败）
            JOptionPane.showMessageDialog(this, "数据库错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // 控制台打印详细错误（调试用）
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

    // 测试入口：启动图书录入窗口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BookEntryUI entryUI = new BookEntryUI();
            entryUI.setVisible(true);
        });
    }
}