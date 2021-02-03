package jdbcTest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAOTest8 {
    public static void main(String[] args) {
        UserDao udao = new UserDao();
        List<User> list = udao.selectAllUsers();
        System.out.println(list);
    }
}

class Dao{
    Connection conn;
    String tableName = "";

    Dao(){
        this(null, "");       
    }

    Dao(String tableName){
        this(null, tableName);
    }

    Dao(Connection conn, String tableName){
        this.conn = conn;
        this.tableName = tableName;
    }
    
    // Dao 기본 생성자 생성
    void rollback(Connection conn){
        if(this.conn !=null) {
            try {
                this.conn.rollback();
            } catch(SQLException e){
            }
        }
    }

    // 각각의 UserDao 메소드의 마지막 finally에서 닫은 부분을 공통적으로 묶어서 빼내왔다.
    // 똑같이 rs, conn, pstmt 다 닫는 역할을 한다.
    void close(AutoCloseable... acs) {
        try {
            for(AutoCloseable ac : acs) {
                if(ac!=null) {
                    ac.close();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}



class UserDao extends Dao {
    UserDao() {
        super(null, "user_info");
    }

    UserDao(Connection conn) {
        super(conn, "user_info");
    }

    List<User> selectAllUsers() {
        List<User> list = new ArrayList<User>();
        String query = "SELECT * FROM " + tableName; // 모든 사용자의 정보를 가져온다.
        ResultSet rs = null;
        Statement stmt = null;

        try {
            String url = "jdbc:oracle:thin:@localhost:1521:xe";
            String id = "student";
            String pw = "1234";

            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(url, id, pw);
            rollback(conn);

            stmt = conn.createStatement();             // Statement를 가져온다.
            rs = stmt.executeQuery(query); // SQL문을 실행한다.

            while (rs.next()) {
                User u = new User();
                String user_id = rs.getString("user_id");
                String password = rs.getString("password");
                String name = rs.getString("name");
                String email = rs.getString("email");

                u = new User(user_id, password, name, email);
                list.add(u);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            close(stmt, rs);
        }

        return list;
    }

    User selectUser(User u) {
        String query = "SELECT * FROM "+ tableName
                +" WHERE USER_ID = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, u.getId());
            rs = ps.executeQuery();

            while (rs.next()) {
                u.setId(rs.getString(1));
                u.setName(rs.getString(2));
                u.setPassword(rs.getString(3));
                u.setEmail(rs.getString(4));
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            close(ps, rs);
        }

        return u;
    }

    int deleteUser(User u) {
        int result = 0;
        String query = "DELETE FROM " + tableName
                + " WHERE USER_ID = ?"; // 사용자 정보를 가져온다. ''를 사용하지 않음에 주의
        try (PreparedStatement ps = conn.prepareStatement(query);) {
            conn.setAutoCommit(true);
            // 3.2 쿼리 셋팅 & 실행
            ps.setString(1, u.getId());
            result = ps.executeUpdate(); // ps.executeUpdate(sql);과 같이 하지 않음에 주의
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
    int insertUser(User u) {
        int result = 0;
        String sql = "INSERT INTO " + tableName + " VALUES "
                + " (?,?,?,?)"; // ""안에 ;를 넣지 않아도 된다.
// String sql = "insert into USER_INFO values (?,?,?,?, sysdate, sysdate)"; // ""안에 ;를 넣지 않아도 된다.
        try (PreparedStatement ps = conn.prepareStatement(sql);) {
            conn.setAutoCommit(true);
            // 3.2 쿼리 셋팅 & 실행
            ps.setString(1, u.getId());
            ps.setString(2, u.getName());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getEmail());

            result = ps.executeUpdate(); // ps.executeUpdate(sql);과 같이 하지 않음에 주의
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return result;
    }

    int updateUser(User u) {
        int result = 0;
        String sql = "UPDATE "+ tableName
                + " SET name=?, password=?, email=?"
                + " WHERE user_id = ?"; // ""안에 ;를 넣지 않아도 된다.
        try (PreparedStatement ps = conn.prepareStatement(sql);){
            conn.setAutoCommit(true);
            // 3.2 쿼리 셋팅 & 실행
            ps.setString(1, u.getName());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getEmail());

            result = ps.executeUpdate(); // ps.executeUpdate(sql);과 같이 하지 않음에 주의
        } catch ( Exception e ) {
            // 5. 실패하면, 에러를 보여줘?
            e.printStackTrace();
        }

        return result;
    }
}



class User{
    String id;
    String name;
    String password;
    String email;

    public User(){}

    public User(String id, String name, String password, String email) {
        super();
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", password=" + password + ", email=" + email + "]";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}