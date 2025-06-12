import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BmsLogin {
    public BmsLogin() {
        frmGlobal = new JFrame("登录");
        pnGlobal = new JPanel();
        btnLogin = new JButton("登录");
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = edtAccount.getText();
                // 获取密码
                char[] passwordChars = edtPassword.getPassword();
                String password = new String(passwordChars);
                // 这里可以添加登录验证逻辑
                System.out.println("点击了登录按钮");
            }
        });
        btnRegister = new JButton("注册");

        pnButtn = new JPanel();

        edtAccount = new JEditorPane();
        edtAccount.setMaximumSize(new Dimension(300, 30));
        // 使用 JPasswordField 替代 JEditorPane 用于密码输入
        edtPassword = new JPasswordField();
        edtPassword.setMaximumSize(new Dimension(300, 30));
        labAccount = new JLabel("账号：");
        labPassword = new JLabel("密码：");
        pnAccount = new JPanel();
        pnPassword = new JPanel();
        glGlobal = new GridLayout(4, 1);
        blAccount = new BoxLayout(pnAccount, BoxLayout.X_AXIS);
        blPassword = new BoxLayout(pnPassword, BoxLayout.X_AXIS);
        blButtn = new BoxLayout(pnButtn, BoxLayout.X_AXIS);

        initLayout();
    }

    public void initLayout() {
        pnGlobal.setLayout(glGlobal);

        pnAccount.setLayout(blAccount);
        pnAccount.add(Box.createHorizontalStrut(80));
        pnAccount.add(labAccount);
        pnAccount.add(edtAccount);

        pnPassword.setLayout(blPassword);
        pnPassword.add(Box.createHorizontalStrut(80));
        pnPassword.add(labPassword);
        pnPassword.add(edtPassword);

        pnButtn.setLayout(blButtn);
        pnButtn.add(Box.createHorizontalStrut(150));
        pnButtn.add(btnLogin);
        pnButtn.add(Box.createHorizontalStrut(100));
        pnButtn.add(btnRegister);

        frmGlobal.add(pnGlobal);

        pnGlobal.add(pnAccount);
        pnGlobal.add(pnPassword);
        pnGlobal.add(pnButtn);

        frmGlobal.setSize(500, 400);
        frmGlobal.setResizable(false);
        frmGlobal.setVisible(true);
    }

    public JFrame frmGlobal; // 登录界面的全局框架
    private JPanel pnGlobal;
    public JButton btnLogin; // 登录界面的登录按钮
    public JButton btnRegister; // 登录界面的注册按钮
    private JPanel pnButtn;

    public JEditorPane edtAccount;
    public JPasswordField edtPassword; // 使用 JPasswordField 替代 JEditorPane 用于密码输入
    private JLabel labAccount;
    private JLabel labPassword;
    private JPanel pnAccount;
    private JPanel pnPassword;
    private GridLayout glGlobal;
    private BoxLayout blAccount; // 第一行也就是账号行的一个横向布局管理器
    private BoxLayout blPassword; // 第二行也就是密码行的横向布局管理器
    private BoxLayout blButtn;

    public static void main(String[] args) {
        new BmsLogin();
    }
}