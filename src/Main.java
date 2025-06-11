import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        //LibrarySystemMainUI mainUi = null;
        //测试案例
        //首先将登录界面显示出来
        BmsLogin login=new BmsLogin();
        SqlQuery query=new SqlQuery();
        //点击登录触发登录事件
        login.btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SqlQuery query=new SqlQuery();
                query.mysqlConnect();
                String sql="select account,chara from user where account=? and password=?";
                String account=login.edtAccount.getText();
                String password=login.edtPassword.getText();
                ResultSet set;
                try {
                    set=query.selectQuery(3,new String[]{sql,account,password});
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                String ret[]=new String[2];
                try {
                    if(set.next()){
                        ret[0]=set.getString("account");
                        ret[1]=set.getString("chara");
                    if(ret==null){
                        JOptionPane.showMessageDialog(login.frmGlobal,"登录失败：账号或密码错误",null,JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        LibrarySystemMainUI mainUi=new LibrarySystemMainUI(ret[0],ret[1],query);
                        login.frmGlobal.setVisible(false);
                        mainUi.setVisible(true);
                    }
                }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }});
        login.btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistrationForm registerUi=new RegistrationForm(login);
                login.frmGlobal.setVisible(false);
                registerUi.setVisible(true);
            }
        });


    }
}


