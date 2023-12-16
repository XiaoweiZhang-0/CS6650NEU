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
        dataSource.setMinIdle(5); // Minimum number of idle connections in the pool
        dataSource.setMaxIdle(10); // Maximum number of idle connections in the pool
        dataSource.setMaxTotal(25); // Maximum number of total connections
        // Additional configuration as needed
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}