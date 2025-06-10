import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class BookManagerPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(BookManagerPanel.class.getName());
    private final Connection conn;  // 标记为final，确保不可变
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BookManagerPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        initComponents();
        loadBooks(); // 初始化时加载图书数据
    }

    private void initComponents() {
        // 1. 顶部搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        JButton addButton = new JButton("添加图书");
        JButton modifyButton = new JButton("修改图书");
        JButton deleteButton = new JButton("删除图书");

        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(addButton);
        searchPanel.add(modifyButton);
        searchPanel.add(deleteButton);

        // 2. 图书表格
        String[] columnNames = {"图书ID", "图书名称", "分类", "作者", "状态", "借阅次数", "入库日期"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止直接编辑表格
            }
        };
        bookTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookTable);

        // 3. 添加到主面板
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 4. 绑定事件
        searchButton.addActionListener(e -> searchBooks());
        addButton.addActionListener(e -> new BookAddDialog((JFrame) SwingUtilities.getWindowAncestor(this), conn).setVisible(true));
        modifyButton.addActionListener(e -> modifyBook());
        deleteButton.addActionListener(e -> deleteBook());
    }

    // 加载所有图书
    private void loadBooks() {
        tableModel.setRowCount(0);
        try (CallableStatement cstmt = conn.prepareCall("{call sp_get_all_books()}")) {
            ResultSet rs = cstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("bid"),
                        rs.getString("bname"),
                        rs.getString("category"),
                        rs.getString("author"),
                        rs.getString("status"),
                        rs.getInt("ref_cnt"),
                        rs.getDate("entry_date")
                });
            }
        } catch (SQLException ex) {
            logger.severe("加载图书失败: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "加载图书失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 搜索图书
    private void searchBooks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadBooks(); // 关键词为空时加载全部
            return;
        }

        tableModel.setRowCount(0);
        try (CallableStatement cstmt = conn.prepareCall("{call sp_search_books(?)}")) {
            cstmt.setString(1, keyword);
            ResultSet rs = cstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("bid"),
                        rs.getString("bname"),
                        rs.getString("category"),
                        rs.getString("author"),
                        rs.getString("status"),
                        rs.getInt("ref_cnt"),
                        rs.getDate("entry_date")
                });
            }
        } catch (SQLException ex) {
            logger.severe("搜索图书失败: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "搜索图书失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 修改图书
    private void modifyBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的图书", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bid = (int) tableModel.getValueAt(selectedRow, 0);
        new BookModifyDialog((JFrame) SwingUtilities.getWindowAncestor(this), conn, bid).setVisible(true);
        loadBooks(); // 修改后刷新表格
    }

    // 删除图书
    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的图书", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bid = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除？删除后不可恢复！", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (CallableStatement cstmt = conn.prepareCall("{call sp_delete_book(?)}")) {
                cstmt.setInt(1, bid);
                ResultSet rs = cstmt.executeQuery();
                if (rs.next()) {
                    String message = rs.getString("message");
                    JOptionPane.showMessageDialog(this, message, "结果", JOptionPane.INFORMATION_MESSAGE);
                    loadBooks(); // 删除后刷新表格
                }
            } catch (SQLException ex) {
                logger.severe("删除图书失败: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "删除图书失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// 添加图书对话框
class BookAddDialog extends JDialog {
    private JTextField nameField, categoryField, authorField;
    private Connection conn;

    public BookAddDialog(JFrame parent, Connection conn) {
        super(parent, "添加图书", true);
        this.conn = conn;
        setLayout(new GridLayout(6, 2));
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // 初始化界面组件
        JLabel nameLabel = new JLabel("图书名称:");
        nameField = new JTextField();
        JLabel categoryLabel = new JLabel("分类:");
        categoryField = new JTextField();
        JLabel authorLabel = new JLabel("作者:");
        authorField = new JTextField();
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        // 添加组件到对话框
        add(nameLabel);
        add(nameField);
        add(categoryLabel);
        add(categoryField);
        add(authorLabel);
        add(authorField);
        add(new JLabel()); // 占位
        add(new JLabel()); // 占位
        add(saveButton);
        add(cancelButton);

        // 绑定事件
        saveButton.addActionListener(e -> saveBook());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveBook() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String author = authorField.getText().trim();

        if (name.isEmpty() || category.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写完整信息", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO books (bname, category, author, status, entry_date) VALUES (?, ?, ?, '可借阅', CURDATE())")) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setString(3, author);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "图书添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "添加图书失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// 修改图书对话框
class BookModifyDialog extends JDialog {
    private JTextField nameField, categoryField, authorField, statusField;
    private Connection conn;
    private int bookId;

    public BookModifyDialog(JFrame parent, Connection conn, int bookId) {
        super(parent, "修改图书信息", true);
        this.conn = conn;
        this.bookId = bookId;
        setLayout(new GridLayout(7, 2));
        setSize(400, 350);
        setLocationRelativeTo(parent);

        // 初始化界面组件
        JLabel idLabel = new JLabel("图书ID:");
        JLabel idValueLabel = new JLabel(String.valueOf(bookId));
        JLabel nameLabel = new JLabel("图书名称:");
        nameField = new JTextField();
        JLabel categoryLabel = new JLabel("分类:");
        categoryField = new JTextField();
        JLabel authorLabel = new JLabel("作者:");
        authorField = new JTextField();
        JLabel statusLabel = new JLabel("状态:");
        statusField = new JTextField();
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        // 添加组件到对话框
        add(idLabel);
        add(idValueLabel);
        add(nameLabel);
        add(nameField);
        add(categoryLabel);
        add(categoryField);
        add(authorLabel);
        add(authorField);
        add(statusLabel);
        add(statusField);
        add(new JLabel()); // 占位
        add(new JLabel()); // 占位
        add(saveButton);
        add(cancelButton);

        // 加载图书信息
        loadBookInfo();

        // 绑定事件
        saveButton.addActionListener(e -> updateBook());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadBookInfo() {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM books WHERE bid = ?")) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("bname"));
                categoryField.setText(rs.getString("category"));
                authorField.setText(rs.getString("author"));
                statusField.setText(rs.getString("status"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载图书信息失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBook() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String author = authorField.getText().trim();
        String status = statusField.getText().trim();

        if (name.isEmpty() || category.isEmpty() || author.isEmpty() || status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写完整信息", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE books SET bname = ?, category = ?, author = ?, status = ? WHERE bid = ?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setString(3, author);
            pstmt.setString(4, status);
            pstmt.setInt(5, bookId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "图书信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "更新图书信息失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}