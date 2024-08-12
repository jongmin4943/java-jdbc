package camp.nextstep.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final Object[] EMPTY = new Object[0];

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            log.info("query : {}", sql);

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        ResultSet resultSet = null;
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }

            resultSet = preparedStatement.executeQuery();

            log.info("query : {}", sql);

            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(resultSet);
        }
    }

    private void closeResultSet(final ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, rowMapper, EMPTY);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final List<T> result = query(sql, rowMapper, params);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

}
