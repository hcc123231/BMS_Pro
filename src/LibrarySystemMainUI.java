import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.logging.Logger;
//主界面
public class LibrarySystemMainUI extends JFrame {
    private JPanel rightContent;
    private JPanel leftNav;
    public JButton logoutButton;
    private GridBagConstraints gbc;
    private User currentUser;
    //private Connection conn;
    private SqlQuery m_query;
    String userName;
    private static final Logger logger = Logger.getLogger(LibrarySystemMainUI.class.getName());
    private BookManagerPanel bookManagerPanel; // 添加对图书管理面板的引用
    private BookEntryPanel bookEntryPanel;
    private BorrowManagementPanel borrowManagementPanel;
    private ReturnManagementPanel returnManagementPanel;
    private ReservationManagerPanel reservationManagerPanel;
    private UserManagementPanel userManagementPanel;
    private StatsAnalysisPanel statsAnalysisPanel;
    private BookSearchPanel bookSearchPanel;
    private MyBorrowPanel myBorrowPanel;
    private MyReservationsPanel myReservationsPanel;
    private UserInfoPanel userInfoPanel;
    private ChangePasswordPanel changePasswordPanel;


    public LibrarySystemMainUI(String username, String role,SqlQuery query) {
        setTitle("校园图书管理系统");
        userName=username;
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        currentUser = new User(username, role);
        //connectToDatabase();
        m_query=query;


        initializeMainUI();
    }

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
        logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setBackground(new Color(25, 118, 210));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        leftNav.add(logoutButton, gbc);

        // 右侧内容
        rightContent = new JPanel(new BorderLayout());
        rightContent.setBackground(Color.WHITE);
        rightContent.add(new JLabel("欢迎使用", SwingConstants.CENTER), BorderLayout.CENTER);

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
                    try {
                        rightContent.add(createFunctionPanel(text), BorderLayout.CENTER);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
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

    private JPanel createFunctionPanel(String functionName) throws SQLException {
        System.out.println("enter createFunctionPanel");
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 当选择"图书管理"时，创建BookManagerPanel实例
        if ("图书管理".equals(functionName)) {
            System.out.println("equals-'图书管理'");
            if (bookManagerPanel == null) {
                bookManagerPanel = new BookManagerPanel(m_query);
                bookManagerPanel.setVisible(true);
            }
            return bookManagerPanel;
        }
        else if("图书录入".equals(functionName)){
            System.out.println("图书录入lab clicked");
            if(bookEntryPanel==null){
                bookEntryPanel=new BookEntryPanel(m_query);
                bookEntryPanel.setVisible(true);
            }
            return bookEntryPanel;
        }
        else if("借阅管理".equals(functionName)){
            System.out.println("借阅管理 clicked");
            if(borrowManagementPanel==null){
                borrowManagementPanel=new BorrowManagementPanel(m_query);
                borrowManagementPanel.setVisible(true);
            }
            return borrowManagementPanel;
        }
        else if("归还管理".equals(functionName)){
            System.out.println("归还管理 clicked");
            if(returnManagementPanel==null){
                returnManagementPanel=new ReturnManagementPanel(m_query);
                returnManagementPanel.setVisible(true);
            }
            return returnManagementPanel;
        }
        else if("预约管理".equals(functionName)){
            System.out.println("预约管理 clicked");
            if(reservationManagerPanel==null){
                reservationManagerPanel=new ReservationManagerPanel(m_query);
                reservationManagerPanel.setVisible(true);
            }
            return reservationManagerPanel;
        }
        else if("用户管理".equals(functionName)){
            System.out.println("用户管理 clicked");
            if(userManagementPanel==null){
                userManagementPanel=new UserManagementPanel(m_query);
                userManagementPanel.setVisible(true);
            }
            return userManagementPanel;
        }
        else if("统计分析".equals(functionName)){
            System.out.println("统计分析 clicked");
            if(statsAnalysisPanel==null){
                statsAnalysisPanel=new StatsAnalysisPanel();
                statsAnalysisPanel.setVisible(true);
            }
            return statsAnalysisPanel;
        }
        else if("图书检索".equals(functionName)){
            System.out.println("图书检索 clicked");
            if(bookSearchPanel==null){
                bookSearchPanel=new BookSearchPanel(this,m_query);
                bookSearchPanel.setVisible(true);
            }
            return bookSearchPanel;
        }
        else if("我的借阅".equals(functionName)){
            System.out.println("我的借阅 clicked");
            if(myBorrowPanel==null){
                myBorrowPanel=new MyBorrowPanel(m_query,userName);
                myBorrowPanel.setVisible(true);
            }else{
                myBorrowPanel=null;
                myBorrowPanel=new MyBorrowPanel(m_query,userName);
                myBorrowPanel.setVisible(true);
            }
            return myBorrowPanel;
        }
        else if("我的预约".equals(functionName)){
            System.out.println("我的预约 clicked");
            if(myReservationsPanel==null){
                myReservationsPanel=new MyReservationsPanel(m_query,userName);
                myReservationsPanel.setVisible(true);
            }
            return myReservationsPanel;
        }
        else if("个人信息".equals(functionName)){
            System.out.println("个人信息 clicked");
            if(userInfoPanel==null){
                userInfoPanel=new UserInfoPanel(m_query,userName);
                userInfoPanel.setVisible(true);
            }
            return userInfoPanel;
        }
        else if("修改密码".equals(functionName)){
            System.out.println("修改密码 clicked");
            if(changePasswordPanel==null){
                changePasswordPanel=new ChangePasswordPanel(m_query,Integer.parseInt(userName));
                changePasswordPanel.setVisible(true);
            }
            return changePasswordPanel;
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


}