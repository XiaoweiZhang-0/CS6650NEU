package com.example;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class DataConnectionPool {
    private static BasicDataSource dataSource = new BasicDataSource();

    static {
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://cs6650-1.cjutmtlyraca.us-west-2.rds.amazonaws.com:3306");
        dataSource.setUsername("admin");
        dataSource.setPassword("12345678");
        dataSource.setMinIdle(2); // Minimum number of idle connections in the pool
        dataSource.setMaxIdle(5); // Maximum number of idle connections in the pool
        dataSource.setMaxTotal(30); // Maximum number of total connections
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}