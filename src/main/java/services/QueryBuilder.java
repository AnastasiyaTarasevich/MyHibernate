package services;

import annotations.field.IdColumn;
import annotations.field.NotNullValue;
import annotations.field.UniqueValue;
import annotations.field.UpdateColumnName;
import annotations.relation.JoinColumn;
import annotations.relation.JoinMany;
import annotations.relation.ManyToOneJoin;
import annotations.table.OurEntity;
import constants.Cascade;
import constants.Queries;
import entities.HibernateLike;
import validators.ValidEntity;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// TODO РАЗБИТЬ ЛОГИКУ!!!!!!!!!!
public class QueryBuilder {
    private final ValidEntity validEntity;


    public QueryBuilder() throws SQLException {

        validEntity = new ValidEntity();
    }

    public String buildCreateTableQuery(Class<?> clazz) {
        OurEntity clazzEntityAnnotation = clazz.getAnnotation(OurEntity.class);
        if (clazzEntityAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }

        String tableName = clazzEntityAnnotation.tableName();
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

        boolean firstField = true;

        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            Field declaredField = clazz.getDeclaredFields()[i];


            if (declaredField.isAnnotationPresent(ManyToOneJoin.class) || declaredField.isAnnotationPresent(JoinColumn.class)
                    || declaredField.isAnnotationPresent(JoinMany.class)) {
                continue;
            }

            // Если это не первое поле, ставим запятую
            if (!firstField) {
                query.append(", ");
            }
            firstField = false;

            String fieldName = declaredField.getName();
            UpdateColumnName declaredFieldAnnotation = declaredField.getAnnotation(UpdateColumnName.class);
            if (declaredFieldAnnotation != null) {
                fieldName = declaredFieldAnnotation.name();
            }

            String type = null;
            if (String.class.equals(declaredField.getType())) {
                type = "TEXT";
            } else if (Integer.class.equals(declaredField.getType())) {
                type = "INT";
            }

            String notNullValue = "";
            if (declaredField.isAnnotationPresent(NotNullValue.class)) {
                notNullValue = "NOT NULL";
            }

            String uniqueModifier = "";
            if (declaredField.isAnnotationPresent(UniqueValue.class)) {
                uniqueModifier = "UNIQUE";
            }

            String primaryKey = "";
            if (declaredField.isAnnotationPresent(IdColumn.class)) {
                primaryKey = "PRIMARY KEY";
            }

            query.append(fieldName).append(" ")
                    .append(type).append(" ")
                    .append(uniqueModifier).append(" ")
                    .append(notNullValue).append(" ")
                    .append(primaryKey);

        }

        query.append(");");
        return query.toString();
    }

    public String buildSelectQuery(Class<?> clazz) {
        OurEntity clazzAnnotation = clazz.getAnnotation(OurEntity.class);
        if (clazzAnnotation == null) {
            throw new IllegalArgumentException("Object is not entity");
        }
        String tableName = clazzAnnotation.tableName();
        List<String> fields = new ArrayList<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            UpdateColumnName declaredAnnotation = declaredField.getAnnotation(UpdateColumnName.class);
            if (declaredAnnotation != null) {
                fields.add(declaredAnnotation.name());
            } else {
                fields.add(declaredField.getName());
            }
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        for (int i = 0; i < fields.size(); i++) {
            query.append(fields.get(i));
            if (i != fields.size() - 1) {
                query.append(", ");
            }

        }
        query.append(" FROM " + tableName);

        return query.toString();
    }

    public String buildSelectByIdQuery(Class<?> clazz) {
        String idColumn = null;
        for (Field declaredField : clazz.getDeclaredFields()) {
            IdColumn id = declaredField.getAnnotation(IdColumn.class);
            UpdateColumnName columnName = declaredField.getAnnotation(UpdateColumnName.class);
            if (id != null) {
                if (columnName != null) {
                    idColumn = columnName.name();
                } else {
                    idColumn = declaredField.getName();
                }
                break;
            }
        }
        if (idColumn == null) {
            throw new IllegalArgumentException("There is no ID in this entity");
        }
        String condition = " WHERE " + idColumn + " = ? ";

        return buildSelectQuery(clazz) + condition;
    }



    public QueryWithParameters buildInsertQuery(Object obj) throws IllegalAccessException, SQLException {
        Class<?> clazz = obj.getClass();

        String tableName = validEntity.getTableName(obj);
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        List<Object> parameters = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {

            Field declaredField = fields[i];
            declaredField.setAccessible(true);
            if (declaredField.isAnnotationPresent(JoinMany.class)) {

                continue;
            }
            String columnName = getColumnName(declaredField);

            columns.append(columnName);
            placeholders.append("?");

            // Добавляем параметр, используя отдельный метод
            Object parameterValue = extractFieldValue(declaredField, obj);
            parameters.add(parameterValue);

            if (i != fields.length - 1) {
                columns.append(", ");
                placeholders.append(", ");
            }
        }

        String query = String.format(Queries.INSERT, tableName, columns, placeholders);
        return new QueryWithParameters(query, parameters);
    }

    private static String getColumnName(Field declaredField) {
        String columnName;

        // Проверяем, есть ли аннотация @JoinColumn
        JoinColumn joinColumnAnnotation = declaredField.getAnnotation(JoinColumn.class);
        if (joinColumnAnnotation != null) {
            columnName = joinColumnAnnotation.name(); // Используем имя из аннотации JoinColumn
        } else {
            // Если аннотации JoinColumn нет, используем @UpdateColumnName или имя поля
            UpdateColumnName declaredFieldAnnotation = declaredField.getAnnotation(UpdateColumnName.class);
            columnName = (declaredFieldAnnotation != null) ? declaredFieldAnnotation.name() : declaredField.getName();
        }
        return columnName;
    }

    /**
     * Метод для извлечения значения из поля, включая обработку аннотаций.
     */
    private Object extractFieldValue(Field field, Object obj) throws IllegalAccessException {
        Object value = field.get(obj);

        if (field.isAnnotationPresent(ManyToOneJoin.class)) {
            if (value != null) {
                Field idField = findIdField(value.getClass());
                boolean idFieldWasAccessible = idField.canAccess(value);
                idField.setAccessible(true); // Делаем поле доступным
                Object idValue = idField.get(value); // Получаем значение ID
                idField.setAccessible(idFieldWasAccessible); // Восстанавливаем состояние доступа
                return idValue; // Возвращаем ID
            } else {
                return null; // Если объект не установлен
            }
        }

        if (field.isAnnotationPresent(JoinMany.class)) {
            // Пропускаем обработку для @JoinMany
            return null;
        }

        // Если нет специальных аннотаций, возвращаем значение напрямую
        return value;
    }




    private Field findIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(IdColumn.class)) {
                return field;
            }
        }
        throw new IllegalArgumentException("No ID field found in class " + clazz.getName());
    }
}
