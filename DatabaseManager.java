import java.sql.*;

public class DatabaseManager {
    private Connection conn;
    
    public DatabaseManager() {
        connectDatabase();
        createTables();
    }
    
    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:voting.db");
            System.out.println("Database connected!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        try {
            Statement stmt = conn.createStatement();
            
            stmt.execute("CREATE TABLE IF NOT EXISTS voters (" +
                        "id TEXT PRIMARY KEY, " +
                        "has_voted INTEGER DEFAULT 0)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS votes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "candidate TEXT, " +
                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            
            System.out.println("Tables created!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean registerVoter(String voterId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO voters (id, has_voted) VALUES (?, 0)");
            ps.setString(1, voterId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean hasVoted(String voterId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT has_voted FROM voters WHERE id = ?");
            ps.setString(1, voterId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("has_voted") == 1;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean voterExists(String voterId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM voters WHERE id = ?");
            ps.setString(1, voterId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean castVote(String voterId, String candidate) {
        try {
            PreparedStatement votePs = conn.prepareStatement(
                "INSERT INTO votes (candidate) VALUES (?)");
            votePs.setString(1, candidate);
            votePs.executeUpdate();
            
            PreparedStatement updatePs = conn.prepareStatement(
                "UPDATE voters SET has_voted = 1 WHERE id = ?");
            updatePs.setString(1, voterId);
            updatePs.executeUpdate();
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public ResultSet getResults() {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(
                "SELECT candidate, COUNT(*) as votes " +
                "FROM votes GROUP BY candidate ORDER BY votes DESC");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}