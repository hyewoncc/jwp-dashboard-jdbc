package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final ObjectMapper<T> objectMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = statement.executeQuery()) {
                log.debug("query : {}", sql);

                List<T> results = new ArrayList<>();
                if (resultSet.next()) {
                    T t = objectMapper.map(resultSet);
                    results.add(t);
                }
                return results;
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T query(final String sql, final Object parameter, final ObjectMapper<T> objectMapper) {
        validateSql(sql, parameter);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                log.debug("query : {}", sql);

                if (resultSet.next()) {
                    return objectMapper.map(resultSet);
                }
                throw new DataAccessException("조회 결과가 존재하지 않습니다.");
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(final String sql, final Object... parameters) {
        validateSql(sql, parameters);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            statement.executeUpdate();
            log.debug("query : {}", sql);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void validateSql(final String sql, final Object... parameters) {
        int required = sql.length() - sql.replaceAll("\\?", "").length();
        if (required != parameters.length) {
            throw new DataAccessException("sql문 내의 매개변수와 주어진 매개변수의 수가 다릅니다.");
        }
    }
}
