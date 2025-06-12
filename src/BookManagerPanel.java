import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class BookManagerPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(BookManagerPanel.class.getName());
    //private final Connection conn;  // 标记为final，确保不可变
    private SqlQuery m_query;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BookManagerPanel(SqlQuery query) throws SQLException {
        m_query=query;
        setLayout(new BorderLayout());
        initComponents();
        loadBooks(); // 初始化时加载图书数据
    }

    private void initComponents() {
        // 1. 顶部搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        //JButton addButton = new JButton("添加图书");
        JButton modifyButton = new JButton("修改图书");
        JButton deleteButton = new JButton("删除图书");

        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        //searchPanel.add(addButton);
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
        searchButton.addActionListener(e -> {
            try {
                searchBooks();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        //addButton.addActionListener(e -> new BookAddDialog((JFrame) SwingUtilities.getWindowAncestor(this), m_query.m_conn).setVisible(true));
        modifyButton.addActionListener(e -> {
            try {
                modifyBook();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteButton.addActionListener(e -> {
            try {
                deleteBook();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    // 加载所有图书
    private void loadBooks() throws SQLException {
        tableModel.setRowCount(0);
        m_query.mysqlConnect();
        String sql = "select bid,bname,category,author,available,ref_cnt,entry_date from bookinfo";
        ResultSet rset = m_query.selectQuery(1, new String[]{sql});
        while (rset.next()) {
            String availableStatus = rset.getInt("available") == 1 ? "可借阅" : "已借出";
            tableModel.addRow(new Object[]{
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getString("category"),
                    rset.getString("author"),
                    availableStatus,
                    rset.getInt("ref_cnt"),
                    rset.getDate("entry_date")
            });
        }
        m_query.mysqlDisconnect();
    }

    // 搜索图书
    private void searchBooks() throws SQLException {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadBooks(); // 关键词为空时加载全部
            return;
        }

        tableModel.setRowCount(0);
        m_query.mysqlConnect();
        String sql="select bid,bname,category,author,available,ref_cnt,entry_date from bookinfo where "+
                "bid like ? or "+
                "bname like ? or "+
                "category like ? or "+
                "author like ? or "+
                "available like ? or "+
                "ref_cnt like ? or "+
                "entry_date like ?";
        String kw="%"+keyword+"%";
        ResultSet rset=m_query.selectQuery(8,new String[]{sql,kw,kw,kw,kw,kw,kw,kw});
        while (rset.next()) {

            tableModel.addRow(new Object[]{
                    rset.getInt("bid"),
                    rset.getString("bname"),
                    rset.getString("category"),
                    rset.getString("author"),
                    rset.getString("available")=="1"?"已借出":"可借阅",

                    rset.getInt("ref_cnt"),

                    rset.getDate("entry_date")
            });
        }
        m_query.mysqlDisconnect();
    }

    // 修改图书
    private void modifyBook() throws SQLException {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的图书", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bid = (int) tableModel.getValueAt(selectedRow, 0);
        new BookModifyDialog((JFrame) SwingUtilities.getWindowAncestor(this), m_query, bid).setVisible(true);
        loadBooks(); // 修改后刷新表格
    }

    // 删除图书
    private void deleteBook() throws SQLException {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的图书", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bid = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "确定删除？删除后不可恢复！", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {

            m_query.mysqlConnect();
            try{
                m_query.bookManagerDeleteBook(bid);
            }catch (SQLException e){
                throw e;
            }


        }
    }
}

// 修改图书对话框
class BookModifyDialog extends JDialog {
    private JTextField nameField, categoryField, authorField, statusField;
    private Connection conn;
    private int bookId;
    SqlQuery m_query;
    public BookModifyDialog(JFrame parent, SqlQuery query, int bookId) throws SQLException {
        super(parent, "修改图书信息", true);
        m_query=query;
        //this.conn = conn;
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
        saveButton.addActionListener(e -> {
            try {
                updateBook();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadBookInfo() throws SQLException {
        String sql="select * from bookinfo where bid=?";
        m_query.mysqlConnect();
        ResultSet rset=m_query.selectQuery(2,new String[]{sql,String.valueOf(bookId)});
        if (rset.next()) {
            nameField.setText(rset.getString("bname"));
            categoryField.setText(rset.getString("category"));
            authorField.setText(rset.getString("author"));
            statusField.setText(rset.getString("available"));
        }
        m_query.mysqlDisconnect();

    }

    private void updateBook() throws SQLException {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String author = authorField.getText().trim();
        String status = statusField.getText().trim();

        if (name.isEmpty() || category.isEmpty() || author.isEmpty() || status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写完整信息", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql="update bookinfo set bname=?,category=?,author=?,available=? where bid=?";
        m_query.mysqlConnect();
        int affectRows=m_query.updateQuery(6,new String[]{sql,name,category,author,status,String.valueOf(bookId)});
        if(affectRows>=1){
            JOptionPane.showMessageDialog(this, "图书信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }else{
            JOptionPane.showMessageDialog(this, "更新图书信息失败: " , "错误", JOptionPane.ERROR_MESSAGE);
        }
        m_query.mysqlDisconnect();

    }
}