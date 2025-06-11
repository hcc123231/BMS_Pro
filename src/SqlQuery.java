import java.sql.*;

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
    public void mysqlDisconnect(Connection conn) throws SQLException {
        conn.close();
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
    /*public String[] selectQueryLogin(int num,String[] str) throws SQLException {
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
        String[] ret=new String[2];
        if(result.next()){
            ret[0]=result.getString("account");
            ret[1]=result.getString("chara");
            result.close();
            prestm.close();
            return ret;
        }
        //关闭资源
        result.close();
        prestm.close();
        return null;
    }*/
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


}
