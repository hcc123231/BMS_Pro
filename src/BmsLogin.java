import javax.swing.*;
import java.awt.*;

public class BmsLogin {
    public BmsLogin(){
        frmGlobal=new JFrame("登录");
        pnGlobal=new JPanel();
        btnLogin=new JButton("登录");
        btnRegister=new JButton(("注册"));
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
        pnButtn.add(Box.createHorizontalStrut(100));
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
