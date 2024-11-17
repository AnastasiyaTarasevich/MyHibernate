package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PreparedStatementBuilder {

    public static PreparedStatement createPreparedStatement(Connection connection, QueryWithParameters queryWithParameters) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(queryWithParameters.getQuery());
        List<Object> parameters = queryWithParameters.getParameters();

        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            if (value instanceof Integer) {
                preparedStatement.setInt(i + 1, (Integer) value);
            } else if (value instanceof String) {
                preparedStatement.setString(i + 1, (String) value);
            } else {
                preparedStatement.setObject(i + 1, value);
            }
        }

        return preparedStatement;
    }
}
