package entities;

import annotations.IdColumn;
import annotations.JoinMany;
import annotations.OurEntity;
import annotations.UpdateColumnName;
import services.QueryBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class HibernateLike {
    private String packageWithEntitites;

    private final QueryBuilder queryBuilder;
    private final Connection connection;

    public HibernateLike(String packageWithEntitites) throws SQLException {
        this.packageWithEntitites = packageWithEntitites;

        this.queryBuilder = new QueryBuilder();

        Properties connectionProps = new Properties();
        connectionProps.put("user","root");
        connectionProps.put("password","666246+");

        this.connection= DriverManager.getConnection(
                "jdbc:p6spy:mysql://localhost:3306/myhibernate",
                connectionProps);


    }

    public void initDb() throws SQLException {
        Set<Class> allClasses = findAllClasses(this.packageWithEntitites);
        for (Class aClass : allClasses) {
            Annotation annotation = aClass.getAnnotation(OurEntity.class);
            if (annotation == null) {
                continue;
            }

            String createQuery = queryBuilder.buildCreateTableQuery(aClass);
            connection
                    .createStatement()
                    .execute(createQuery);
        }
    }

    public void persist(Object object) throws IllegalAccessException, SQLException {
        String insertQuery = queryBuilder.buildInsertQuery(object);
        connection
                .createStatement()
                .execute(insertQuery);
    }

    public <T> T getById(Class<T> clazz, Serializable id) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String query = queryBuilder.buildSelectByIdQuery(clazz);

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setObject(1, id);

        ResultSet resultSet = preparedStatement.executeQuery();
        boolean hasNext = resultSet.next();
        if (!hasNext) {
            throw new IllegalArgumentException("No record found");
        }

        Object newInstance = clazz.getDeclaredConstructor().newInstance();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();

            UpdateColumnName declaredFieldAnnotation = field.getAnnotation(UpdateColumnName.class);
            if (declaredFieldAnnotation != null) {
                fieldName = declaredFieldAnnotation.name();
            }

            Object object = resultSet.getObject(fieldName);
            boolean isPrivate = field.trySetAccessible();
            field.setAccessible(true);
            field.set(newInstance, object);
            field.setAccessible(isPrivate);
        }

        return (T) newInstance;
    }

    private Set<Class> findAllClasses(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

}