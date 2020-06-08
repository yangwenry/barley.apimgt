package barley.apimgt.usage.billing;

import barley.apimgt.usage.billing.exception.UsageBillingDatabaseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BillingDBUtil {

    private static final Log log = LogFactory.getLog(BillingDBUtil.class);

    private static volatile DataSource dataSource = null;
    private static final  Object lock = new Object();
    private static final String DATA_SOURCE_NAME = "java:/comp/env/jdbc/BARLEY_AM_DB";

    public static void initializeDataSource() throws UsageBillingDatabaseException {
        if (dataSource != null) {
            return;
        }

        try {
            synchronized (lock) {
                if(dataSource == null){
                    Context ctx = new InitialContext();
                    dataSource = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
                }
            }
        } catch (NamingException e) {
            throw new UsageBillingDatabaseException("Error while looking up the data " +
                    "source: " + DATA_SOURCE_NAME, e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }

    /**
     * Utility method to close the connection streams.
     * @param preparedStatement PreparedStatement
     * @param connection Connection
     * @param resultSet ResultSet
     */
    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }

    /**
     * Close Connection
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close database connection. Continuing with " +
                        "others. - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }

    }

    /**
     * Close PreparedStatement
     * @param preparedStatement PreparedStatement
     */
    public static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close PreparedStatement. Continuing with" +
                        " others. - " + e.getMessage(), e);
            }
        }

    }

}
