import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.logging.Logger;

public class LibrarySystemMainUI extends JFrame {
    private JPanel rightContent;
    private JPanel leftNav;
    private GridBagConstraints gbc;
    private User currentUser;
    private Connection conn;
    private static final Logger logger = Logger.getLogger(LibrarySystemMainUI.class.getName());
    private BookManagerPanel bookManagerPanel; // 添加对图书管理面板的引用

    public LibrarySystemMainUI(String username, String role) {
        setTitle("校园图书管理系统");
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        currentUser = new User(username, role);
        //connectToDatabase();
        SqlQuery query=new SqlQuery();
        query.mysqlConnect();

        initializeMainUI();
    }

    /*private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/bms_db?useSSL=false&serverTimezone=UTC";
            String dbUser = "root";
            String dbPass = "Lqf123000@";
            conn = DriverManager.getConnection(url, dbUser, dbPass);
            logger.info("数据库连接成功");
        } catch (Exception e) {
            logger.severe("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }*/

    private void initializeMainUI() {
        getContentPane().removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());

        // 左侧导航
        leftNav = new JPanel(new GridBagLayout());
        leftNav.setPreferredSize(new Dimension(200, 0));
        leftNav.setBackground(new Color(40, 44, 52));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.NORTH;

        JLabel title = new JLabel("校园图书管理系统");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        leftNav.add(title, gbc);
        gbc.gridy++;

        if (currentUser.getRole().equals("2")) {
            createAdminMenus();
        } else {
            createUserMenus();
        }

        // 退出按钮
        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setBackground(new Color(25, 118, 210));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "确定退出？", "确认", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose(); // 关闭当前窗口，若需完全退出可 System.exit(0)
            }
        });

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        leftNav.add(logoutButton, gbc);

        // 右侧内容
        rightContent = new JPanel(new BorderLayout());
        rightContent.setBackground(Color.WHITE);
        rightContent.add(new JLabel("功能面板将在此展示", SwingConstants.CENTER), BorderLayout.CENTER);

        mainPanel.add(leftNav, BorderLayout.WEST);
        mainPanel.add(rightContent, BorderLayout.CENTER);
        getContentPane().add(mainPanel);
        revalidate();
        repaint();
        setVisible(true);
    }

    private void createAdminMenus() {
        String[] adminMenus = {
                "图书录入", "图书管理", "借阅管理",
                "归还管理", "预约管理", "用户管理",
                "图书检索", "统计分析"
        };
        addMenus(adminMenus);
    }

    private void createUserMenus() {
        String[] userMenus = {
                "我的借阅", "我的预约", "图书检索",
                "个人信息", "修改密码"
        };
        addMenus(userMenus);
    }

    private void addMenus(String[] menus) {
        for (String text : menus) {
            JLabel menuLabel = new JLabel(text);
            menuLabel.setForeground(Color.WHITE);
            menuLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            menuLabel.setOpaque(true);
            menuLabel.setBackground(new Color(40, 44, 52));
            menuLabel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            menuLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            menuLabel.setHorizontalAlignment(SwingConstants.LEFT);

            menuLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    clearMenuHighlights();
                    menuLabel.setBackground(new Color(64, 69, 82));
                    rightContent.removeAll();
                    rightContent.add(createFunctionPanel(text), BorderLayout.CENTER);
                    rightContent.revalidate();
                    rightContent.repaint();
                }
            });

            leftNav.add(menuLabel, gbc);
            gbc.gridy++;
        }
    }

    private void clearMenuHighlights() {
        for (Component comp : leftNav.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getBackground().equals(new Color(64, 69, 82))) {
                    label.setBackground(new Color(40, 44, 52));
                }
            }
        }
    }

    private JPanel createFunctionPanel(String functionName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 当选择"图书管理"时，创建BookManagerPanel实例
        if ("图书管理".equals(functionName)) {
            if (bookManagerPanel == null) {
                bookManagerPanel = new BookManagerPanel(conn);
            }
            return bookManagerPanel;
        }

        // 其他功能的默认面板
        JLabel titleLabel = new JLabel(functionName);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel contentLabel = new JLabel(functionName + " 功能面板", SwingConstants.CENTER);
        contentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        panel.add(contentLabel, BorderLayout.CENTER);

        return panel;
    }

    // 供外部启动（如登录后调用），替代原 main 直接创建
    public static void start(String username, String role) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.severe("设置外观失败: " + e.getMessage());
                e.printStackTrace();
            }
            new LibrarySystemMainUI(username, role).setVisible(true);
        });
    }

    // 用户类
    private static class User {
        private final String username;
        private final String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }

    // 若需单独测试操作界面，可保留 main，实际应通过登录后调用 start
    public static void main(String[] args) {
        start("admin", "管理员");
    }
}