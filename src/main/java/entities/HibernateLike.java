package entities;

import annotations.relation.JoinMany;
import annotations.relation.ManyToOneJoin;
import annotations.table.OurEntity;
import annotations.field.UpdateColumnName;
import constants.Cascade;
import lombok.NoArgsConstructor;
import services.PreparedStatementBuilder;
import services.QueryBuilder;
import services.QueryWithParameters;
import services.RelationBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;


public class HibernateLike {
    private String packageWithEntities;

    private final QueryBuilder queryBuilder;
    private final Connection connection;
    private final RelationBuilder relationBuilder;
    private static HibernateLike INSTANCE;

    // Статический метод для получения единственного экземпляра
    public static HibernateLike getInstance(String packageWithEntities) throws SQLException {
        if (INSTANCE == null) {
            synchronized (HibernateLike.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HibernateLike(packageWithEntities);
                }
            }
        }
        return INSTANCE;
    }

    private HibernateLike(String packageWithEntities) throws SQLException {
        this.packageWithEntities = packageWithEntities;

        this.queryBuilder = new QueryBuilder();
        this.relationBuilder=new RelationBuilder();

        Properties connectionProps = new Properties();
        connectionProps.put("user","root");
        connectionProps.put("password","666246+");

        this.connection= DriverManager.getConnection(
                "jdbc:p6spy:mysql://localhost:3306/myhibernate",
                connectionProps);


    }

    public void initDb() throws SQLException {
        Set<Class> allClasses = findAllClasses(this.packageWithEntities);
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

        for (Class aClass : allClasses) {
            Annotation annotation = aClass.getAnnotation(OurEntity.class);
            if (annotation == null) {
                continue;
            }

            RelationBuilder relationBuilder = new RelationBuilder();
            List<String> foreignKeys = relationBuilder.buildManyToOneRelation(aClass);

            if (!foreignKeys.isEmpty()) {
                for (String query : foreignKeys) {
                    connection.createStatement().execute(query);
                }
            }

        }
        List<String> manyToManyQueries = relationBuilder.buildManyToManyRelation( allClasses);

        for (String query : manyToManyQueries) {
            connection.createStatement().execute(query);
        }


    }



    public void persist(Object object) throws IllegalAccessException, SQLException {
        handleCascadeOperations(object, Cascade.INSERT);
        QueryWithParameters queryWithParameters = queryBuilder.buildInsertQuery(object);
        PreparedStatement insertQuery = PreparedStatementBuilder.createPreparedStatement(connection,queryWithParameters);
        insertQuery.executeUpdate();

    }


    private void handleCascadeOperations(Object object, Cascade cascadeType) throws IllegalAccessException, SQLException {
        // Получаем все поля объекта
        Field[] fields = object.getClass().getDeclaredFields();

        // Проходим по каждому полю
        for (Field field : fields) {
            field.setAccessible(true);

            Object relatedObject = field.get(object);
            if (relatedObject != null) {


                Cascade[] cascades = getCascadesForField(field);

                if (cascades != null) {
                    for (Cascade cascade : cascades) {


                        switch (cascade) {
                            case INSERT:
                                if (cascadeType == Cascade.INSERT) {
                                    persist(relatedObject);
                                }
                                break;
                            case UPDATE:
                                if (cascadeType == Cascade.UPDATE) {
                                    // merge(relatedObject); // Здесь нужно будет вызвать метод обновления
                                }
                                break;
                            case DELETE:
                                if (cascadeType == Cascade.DELETE) {
                                    // delete(relatedObject); // Здесь нужно будет вызвать метод удаления
                                }
                                break;
                            case ALL:
                                persist(relatedObject); // В случае ALL выполняем все операции
                                // merge(relatedObject); // Вы можете также добавить обновление и удаление в случае ALL
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    // Метод для получения каскадных операций для поля
    private Cascade[] getCascadesForField(Field field) {
        // Проверяем, есть ли аннотация каскадирования для данного поля

        if (field.isAnnotationPresent(ManyToOneJoin.class)) {
            return field.getAnnotation(ManyToOneJoin.class).cascade();
        }

        if (field.isAnnotationPresent(JoinMany.class)) {
            return field.getAnnotation(JoinMany.class).cascade();
        }
        return null; // Если аннотации каскадирования нет, возвращаем null
    }

//    public void merge(Object object) throws SQLException {
//        String updateQuery=queryBuilder.buildUpdateQuery(object);
//        connection.createStatement().execute(updateQuery);
//    }

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