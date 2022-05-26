import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class JDBC {
	private static final String DB_HOST = "localhost";
	private static final String DB_USER = "c3358";
	private static final String DB_PASS = "c3358PASS";
	private static final String DB_NAME = "c3358";
	private Connection conn;
	public JDBC() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		conn = DriverManager.getConnection("jdbc:mysql://"+DB_HOST+
				"/"+DB_NAME+
				"?user="+DB_USER+
				"&password="+DB_PASS);
		System.out.println("Database connection successful.");
	}
}
