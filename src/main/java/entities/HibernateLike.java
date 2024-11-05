package entities;

import annotations.OurEntity;
import services.QueryBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

//    public <T> T getById(Class<T> clazz, Serializable id) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//        String query = queryBuilder.buildSelectById(clazz);
//
//        PreparedStatement preparedStatement = connection.prepareStatement(query);
//        preparedStatement.setObject(1, id);
//
//        ResultSet resultSet = preparedStatement.executeQuery();
//        List<T> fetchData = fetchData(clazz, resultSet);
//
//        if (fetchData.isEmpty()) {
//            throw new IllegalArgumentException("No record found!");
//        }
//
//        if (fetchData.size() > 1) {
//            throw new IllegalArgumentException("More than 1 record is found!");
//        }
//
//        return fetchData.iterator().next();
//    }
//
//    public <T> List<T> getAll(Class<T> clazz) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
//        return getAll(clazz, "");
//    }
//
//    public <T> List<T> getAll(Class<T> clazz, String whereClause) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
//        String query = queryBuilder.buildSelectQuery(clazz);
//        query += " " + whereClause;
//
//        Statement statement = connection.createStatement();
//
//        ResultSet resultSet = statement.executeQuery(query);
//
//        return fetchData(clazz, resultSet);
//    }
//
//    private <T> List<T> fetchData(Class<T> clazz, ResultSet resultSet) throws SQLException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//        List<T> result = new ArrayList<>();
//
//        while (resultSet.next()) {
//            Object newInstance = clazz.getDeclaredConstructor().newInstance();
//
//            Field[] fields = clazz.getDeclaredFields();
//
//            Object currentObjectIdValue = null;
//            for (Field field : fields) {
//                if (field.isAnnotationPresent(IdColumn.class)) {
//                    currentObjectIdValue = resultSet.getObject(field.getName());
//                }
//            }
//
//            for (Field field : fields) {
//                String fieldName = field.getName();
//
//                if (field.isAnnotationPresent(JoinMany.class)) {
//                    String joinColumnName = field.getAnnotation(JoinMany.class).joinColumn();
//
//                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
//                    Class<?> genericType = (Class<?>) listType.getActualTypeArguments()[0];
//
//                    List list = (List) Proxy.newProxyInstance(
//                            HibernateLike.class.getClassLoader(),
//                            new Class[]{List.class},
//                            new SelectProxyHandler(this, genericType, joinColumnName, currentObjectIdValue)
//                    );
//
//                    boolean isPrivate = field.trySetAccessible();
//                    field.setAccessible(true);
//                    field.set(newInstance, list);
//                    field.setAccessible(isPrivate);
//
//                } else {
//
//                    OurColumnName declaredFieldAnnotation = field.getAnnotation(OurColumnName.class);
//                    if (declaredFieldAnnotation != null) {
//                        fieldName = declaredFieldAnnotation.name();
//                    }
//
//                    Object object = resultSet.getObject(fieldName);
//                    boolean isPrivate = field.trySetAccessible();
//                    field.setAccessible(true);
//                    field.set(newInstance, object);
//                    field.setAccessible(isPrivate);
//                }
//            }
//
//            result.add((T) newInstance);
//        }
//
//        return result;
//    }

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