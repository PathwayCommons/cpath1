package org.mskcc.pathdb.logger;

import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Admin/View the Log Messages.
 *
 * @author Ethan Cerami
 */
public class AdminLogger {
    private String host = "localhost";
    private String db = "log";
    private String user = "logger";
    private String password = "cbio1";
    private ArrayList records;

    /**
     * Gets all log messages.
     * @return ArrayList of LogRecord objects.
     * @throws SQLException Indicates error connecting to database.
     * @throws ClassNotFoundException Indicates error locating jdbc driver.
     */
    public ArrayList getLogRecords() throws SQLException,
            ClassNotFoundException {
        records = new ArrayList();
        Connection con = getConnection();
        getRecords(con);
        return records;
    }

    /**
     * Deletes all existing Log messages.
     * @throws SQLException Indicates error connecting to database.
     * @throws ClassNotFoundException Indicates error locating jdbc driver.
     */
    public void deleteAllLogRecords() throws SQLException,
            ClassNotFoundException {
            Connection con = this.getConnection();
        PreparedStatement pstmt = con.prepareStatement
                ("TRUNCATE TABLE record");
        pstmt.executeUpdate();
    }

    /**
     * Gets all Log Records.
     */
    private void getRecords(Connection con) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement
                ("SELECT * FROM record order by date asc");
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            Date date = rs.getTimestamp("date");
            String logger = rs.getString("logger");
            String priority = rs.getString("priority");
            String message = rs.getString("message");
            LogRecord record = new LogRecord(date, priority, logger, message);
            records.add(record);
        }
    }

    /**
     * Gets Database Connection.
     */
    private Connection getConnection() throws ClassNotFoundException,
            SQLException {
        Connection con = null;
        String url =
                new String("jdbc:mysql://" + host + "/"
                + db + "?user=" + user
                + "&password=" + password);
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection(url);
        return con;
    }
}
