import javax.swing.*;
import java.rmi.UnexpectedException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SqlQuery {
    public String m_jdbcUrl="jdbc:mysql://localhost:3306/bms_db";
    public String m_dbUsername="root";
    public String m_dbPassword="2028915986hcc";
    public Connection m_conn=null;
    //建立数据库连接
    public Connection mysqlConnect(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            m_conn=DriverManager.getConnection(m_jdbcUrl,m_dbUsername,m_dbPassword);
        }catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return m_conn;
    }
    public void mysqlDisconnect() throws SQLException {
        m_conn.close();
    }

    public ResultSet selectQuery(int num,String[] str) throws SQLException {
        //传进来的第一个参数一定是sql语句，而后跟着的是sql语句中的参数
        PreparedStatement prestm=m_conn.prepareStatement(str[0]);
        System.out.println("enter select query");
        if(num>1){
            for(int i=1;i<num;i++){
                prestm.setString(i,str[i]);
            }
        }
        ResultSet result=prestm.executeQuery();
        System.out.println("sql:"+prestm.toString());

        //关闭资源

        //prestm.close();
        return result;
    }
    public void returnTransaction(String id,String retDate) throws SQLException {
        try{
            //开启事务
            m_conn.setAutoCommit(false);
            //检查记录状态
            //先到borrow_relation中得到bid
            String sql="select bid,end_date,is_ret from borrow_relation where id=?";
            PreparedStatement pstmt1=m_conn.prepareStatement(sql);
            pstmt1.setString(1,id);
            ResultSet rset=pstmt1.executeQuery();
            if(!rset.next()){
                rset.close();
                pstmt1.close();
                throw new SQLException("未找到该条记录");
            }
            int bookId=rset.getInt("bid");
            Date endDate=rset.getDate("end_date");
            int is_ret=rset.getInt("is_ret");
            rset.close();
            pstmt1.close();
            //然后拿bid去bookinfo中找到available的值再进行下一步的状态检查
            sql="select bname,author from bookinfo where bid=?";
            PreparedStatement pstmt2=m_conn.prepareStatement(sql);
            pstmt2.setInt(1,bookId);
            ResultSet rset2=pstmt2.executeQuery();
            if(!rset2.next()){
                rset2.close();
                pstmt2.close();
                throw new SQLException("未找到该条记录");
            }
            //int status=rset2.getInt("available");
            String bookName=rset2.getString("bname");
            String bauthor=rset2.getString("author");
            rset2.close();
            pstmt2.close();
            //计算罚金
            if(is_ret==1){
                throw new SQLException("该书已归还");
            }
            LocalDate endLocalDate=endDate.toLocalDate();
            LocalDate currentLocalDate=LocalDate.now();
            long overdueDays = ChronoUnit.DAYS.between(endLocalDate, currentLocalDate);
            long fineValue=overdueDays>0?overdueDays:0;
            //更新借阅记录
            sql="update borrow_relation set fine=?,practical_date=?,is_ret=1 where id=?";
            PreparedStatement pstmt3=m_conn.prepareStatement(sql);
            pstmt3.setLong(1,fineValue);
            pstmt3.setString(2,retDate);
            pstmt3.setString(3,id);
            int affectRows=pstmt3.executeUpdate();
            if(affectRows<=0){
                pstmt3.close();
                throw new SQLException("更新失败");
            }
            pstmt3.close();

            //更新图书库存
            sql="update book_count set availd=availd+1 where bname=? and author=?";
            PreparedStatement pstmt5=m_conn.prepareStatement(sql);
            pstmt5.setString(1,bookName);
            pstmt5.setString(2,bauthor);
            affectRows=pstmt5.executeUpdate();
            if(affectRows<=0){
                pstmt5.close();
                throw new SQLException("book_count更新失败");
            }
            pstmt5.close();
            //更新bookinfo记录
            //先检查book_count中的availd数是否为0
            sql="select availd from book_count where bname=? and author=?";
            PreparedStatement pstmt7=m_conn.prepareStatement(sql);
            pstmt7.setString(1,bookName);
            pstmt7.setString(2,bauthor);
            ResultSet rset7=pstmt7.executeQuery();
            int num=-1;
            if(rset7.next()){
                num=rset7.getInt("availd");
            }else{
                rset7.close();
                pstmt7.close();
                throw new SQLException("availd failed to search");
            }
            rset7.close();
            pstmt7.close();


            if(num>0){
                sql="update bookinfo set available=1 where bname=? and author=?";
                PreparedStatement pstmt8=m_conn.prepareStatement(sql);
                pstmt8.setString(1,bookName);
                pstmt8.setString(2,bauthor);
                int affRows=pstmt8.executeUpdate();
                if(affRows<=0){
                    pstmt8.close();
                    throw new SQLException("failed to update bookinfo available");
                }
                pstmt8.close();
            }


            m_conn.commit();
        }catch (SQLException e){
            m_conn.rollback();
            throw e;
        }finally {
            m_conn.setAutoCommit(true);
        }

    }



    public void borrowManagerTransaction(String bid,String uid,String borrowDate,String dueDate) throws SQLException {
        //这里的uid是指用户账号
        //第一步禁用自动提交
        m_conn.setAutoCommit(false);

        try {
            //第二步检查图书是否可借
            String sql = "select available from bookinfo where bid=?";
            PreparedStatement pstmt1 = m_conn.prepareStatement(sql);
            pstmt1.setString(1, bid);
            ResultSet rset1=pstmt1.executeQuery();
            System.out.println("enter transation");
            //rset1.next();
            //System.out.println("transation:"+rset1.getInt("available"));
            if(!rset1.next()||rset1.getInt("available")<1){
                rset1.close();
                pstmt1.close();
                throw new SQLException("图书不存在或者不可借");
            }
            rset1.close();
            pstmt1.close();
            // 第三步检查用户是否存在
            sql="select uid from user where account=?";
            PreparedStatement pstmt2 = m_conn.prepareStatement(sql);
            pstmt2.setString(1,uid);
            ResultSet rset2=pstmt2.executeQuery();
            if(!rset2.next()){
                rset2.close();
                pstmt2.close();
                throw new SQLException("用户不存在");
            }
            rset2.close();
            pstmt2.close();
            //第四步插入借阅记录
            sql="insert into borrow_relation (uid,bid,start_date,end_date) value (?,?,?,?)";
            PreparedStatement pstmt3 = m_conn.prepareStatement(sql);
            pstmt3.setString(1,uid);
            pstmt3.setString(2,bid);
            pstmt3.setString(3,borrowDate);
            pstmt3.setString(4,dueDate);
            int affectRows=pstmt3.executeUpdate();
            if(affectRows<=0){
                pstmt3.close();
                throw new SQLException("插入失败");
            }
            pstmt3.close();
            //第五步更新图书库存
            //在更新之前先根据bid找到bname和author
            sql="select bname,author from bookinfo where bid=?";
            PreparedStatement pstmt4 = m_conn.prepareStatement(sql);
            pstmt4.setString(1,bid);
            ResultSet rset4=pstmt4.executeQuery();
            if(!rset4.next()){
                rset4.close();
                pstmt4.close();
                throw new SQLException("找不到id对应的信息");
            }
            String author=rset4.getString("author");
            String bookName=rset4.getString("bname");
            rset4.close();
            pstmt4.close();

            sql="update book_count set availd=availd-1 where bname=? and author=?";
            PreparedStatement pstmt5 = m_conn.prepareStatement(sql);
            pstmt5.setString(1,bookName);
            pstmt5.setString(2,author);
            affectRows=pstmt5.executeUpdate();
            if(affectRows<=0){
                pstmt5.close();
                throw new SQLException("更新失败");
            }
            pstmt5.close();
            //在这之前还要查询book_count数据库查找这本书的可借数是否为0，如果为0则更新bookinfo表的available为0
            sql="select availd from book_count where bname=? and author=?";
            PreparedStatement pstmt7=m_conn.prepareStatement(sql);
            pstmt7.setString(1,bookName);
            pstmt7.setString(2,author);
            ResultSet rset7=pstmt7.executeQuery();
            int num=-1;
            if(rset7.next()){
                num=rset7.getInt("availd");
            }else{
                rset7.close();
                pstmt7.close();
                throw new SQLException("availd failed to search");
            }
            rset7.close();
            pstmt7.close();

            System.out.println("num:"+num);
            if(num==0){
                sql="update bookinfo set available=0 where bname=? and author=?";
                PreparedStatement pstmt8=m_conn.prepareStatement(sql);
                pstmt8.setString(1,bookName);
                pstmt8.setString(2,author);
                int affRows=pstmt8.executeUpdate();
                if(affRows<=0){
                    pstmt8.close();
                    throw new SQLException("failed to update bookinfo available");
                }
                pstmt8.close();
            }
            //第六步将bookinfo中的书本对ref_cnt进行++操作
            sql="update bookinfo set ref_cnt=ref_cnt+1 where bid=?";
            PreparedStatement pstmt6=m_conn.prepareStatement(sql);
            pstmt6.setString(1,bid);
            affectRows=pstmt6.executeUpdate();
            if(affectRows<=0){
                pstmt6.close();;
                throw new SQLException("更新状态失败");
            }
            pstmt6.close();

        }catch (SQLException e){
            m_conn.rollback();
            System.out.println("transation failed："+e.getMessage());
            throw e;
        }finally {
            //最后记得设置可自动提交
            m_conn.setAutoCommit(true);
        }
    }

    public int updateQuery(int num,String[] str) throws SQLException {
        PreparedStatement prestm=m_conn.prepareStatement(str[0]);
        if(num>1){
            for(int i=1;i<num;i++){
                prestm.setString(i,str[i]);
            }
        }
        System.out.println("sql:"+prestm.toString());
        int affectRows=prestm.executeUpdate();
        prestm.close();
        return affectRows;
    }
    public void bookManagerDeleteBook(int bid) throws SQLException {
        //先利用bid去bookinfo中查找出bname和author
        String sql="select bname,author from bookinfo where bid=?";
        PreparedStatement stmt1=m_conn.prepareStatement(sql);
        stmt1.setInt(1,bid);
        ResultSet rs1=stmt1.executeQuery();
        if(!rs1.next()){
            rs1.close();
            stmt1.close();
            throw new SQLException("can not find author and bname");
        }
        String bname=rs1.getString("bname");
        String author=rs1.getString("author");
        rs1.close();;
        stmt1.close();

        //然后开始到reservation表中删除与bid相关的记录
        sql="delete from reservation where bid=?";
        PreparedStatement stmt2=m_conn.prepareStatement(sql);
        stmt2.setInt(1,bid);
        int affectRows=stmt2.executeUpdate();

        stmt2.close();

        //然后删除book_count中的与bid相关的记录
        sql="delete from book_count where bname=? and author =?";
        PreparedStatement stmt3=m_conn.prepareStatement(sql);
        stmt3.setString(1,bname);
        stmt3.setString(2,author);
        affectRows=stmt3.executeUpdate();

        stmt3.close();

        //然后到borrow_relation中删除
        sql="delete from borrow_relation where bid=?";
        PreparedStatement stmt4=m_conn.prepareStatement(sql);
        stmt4.setInt(1,bid);
        affectRows=stmt4.executeUpdate();

        stmt4.close();

        //最后到bookinfo中删除
        sql="delete from bookinfo where bid=?";
        PreparedStatement stmt5=m_conn.prepareStatement(sql);
        stmt5.setInt(1,bid);
        affectRows=stmt5.executeUpdate();

        stmt5.close();


    }



}
