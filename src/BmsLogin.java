import org.w3c.dom.ls.LSOutput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.SQLOutput;

public class BmsLogin {
    public BmsLogin(){
        frmGlobal=new JFrame("登录");
        pnGlobal=new JPanel();
        btnLogin=new JButton("登录");
        btnLogin.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("clicked");
                String account =edtAccount.getText();
                String password=edtPassword.getText();
                SqlQuery query=new SqlQuery();
                query.mysqlConnect();
                String sql="select uid from user where account=? and password=?";
                try {
                    query.selectQuery(3,new String[]{sql,account,password});
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        btnRegister=new JButton(("注册"));
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("clicked register");
                RegistrationForm registerInterface=new RegistrationForm();
                frmGlobal.setVisible(false);
                registerInterface.setVisible(true);
            }
        });
        pnButtn=new JPanel();

        edtAccount=new JEditorPane();
        edtAccount.setMaximumSize(new Dimension(300,30));
        edtPassword=new JEditorPane();
        edtPassword.setMaximumSize(new Dimension(300,30));
        labAccount=new JLabel("账号：");
        labPassword=new JLabel("密码：");
        pnAccount=new JPanel();
        pnPassword=new JPanel();
        //blGlobal=new BoxLayout(pnGlobal,BoxLayout.Y_AXIS);
        glGlobal=new GridLayout(4,1);
        blAccount=new BoxLayout(pnAccount,BoxLayout.X_AXIS);
        blPassword=new BoxLayout(pnPassword,BoxLayout.X_AXIS);
        blButtn=new BoxLayout(pnButtn,BoxLayout.X_AXIS);


        initLayout();
    }
    public void initLayout(){
        //pnGlobal.setLayout(blGlobal);
        pnGlobal.setLayout(glGlobal);

        pnAccount.setLayout(blAccount);
        pnAccount.add(Box.createHorizontalStrut(100));
        pnAccount.add(labAccount);
        pnAccount.add(edtAccount);

        pnPassword.setLayout(blPassword);
        pnPassword.add(Box.createHorizontalStrut(100));
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
        frmGlobal.setSize(500,400);
        frmGlobal.setResizable(false);
        frmGlobal.setVisible(true);
    }
    private JFrame frmGlobal;//登录界面的全局框架
    private JPanel pnGlobal;
    private JButton btnLogin;//登录界面的登录按钮
    private JButton btnRegister;//登录界面的注册按钮
    private JPanel pnButtn;

    private JEditorPane edtAccount;
    private JEditorPane edtPassword;
    private JLabel labAccount;
    private JLabel labPassword;
    private JPanel pnAccount;
    private JPanel pnPassword;
    //private BoxLayout blGlobal;
    private GridLayout glGlobal;
    private BoxLayout blAccount;//第一行也就是账号行的一个横向布局管理器
    private BoxLayout blPassword;//第二行也就是密码行的横向布局管理器
    private BoxLayout blButtn;


}
