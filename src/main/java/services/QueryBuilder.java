package services;

import annotations.field.IdColumn;
import annotations.field.NotNullValue;
import annotations.field.UniqueValue;
import annotations.field.UpdateColumnName;
import annotations.relation.JoinColumn;
import annotations.relation.ManyToOneJoin;
import annotations.table.OurEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {
   public String buildSelectQuery(Class <?> clazz)
    {
        OurEntity clazzAnnotation=clazz.getAnnotation(OurEntity.class);
        if(clazzAnnotation==null)
        {
            throw new IllegalArgumentException("Object is not entity");
        }
        String tableName= clazzAnnotation.tableName();
        List<String> fields=new ArrayList<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            UpdateColumnName declaredAnnotation=declaredField.getAnnotation(UpdateColumnName.class);
            if(declaredAnnotation!=null)
            {
                fields.add(declaredAnnotation.name());
            }
            else {
                fields.add(declaredField.getName());
            }
        }
        StringBuilder query =new StringBuilder();
        query.append("SELECT ");
        for (int i = 0; i < fields.size(); i++) {
            query.append(fields.get(i));
            if(i!=fields.size()-1)
            {
                query.append(", ");
            }

        }
        query.append(" FROM "+tableName);

        return query.toString();
    }
    public String buildCreateTableQuery(Class<?> clazz) {
        OurEntity clazzEntityAnnotation = clazz.getAnnotation(OurEntity.class);
        if (clazzEntityAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }

        String tableName = clazzEntityAnnotation.tableName();
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

        boolean firstField = true; // Флаг, который отслеживает, нужно ли ставить запятую

        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            Field declaredField = clazz.getDeclaredFields()[i];

            // Пропускаем поля, помеченные @ManyToOneJoin или @JoinColumn
            if (declaredField.isAnnotationPresent(ManyToOneJoin.class) || declaredField.isAnnotationPresent(JoinColumn.class)) {
                continue; // Эти поля не добавляются как столбцы
            }

            // Если это не первое поле, ставим запятую
            if (!firstField) {
                query.append(", ");
            }
            firstField = false;  // После первого поля запятая будет ставиться

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

            String primaryKey="";
            if(declaredField.isAnnotationPresent(IdColumn.class))
            {
                primaryKey="PRIMARY KEY";
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


    public String buildSelectByIdQuery(Class<?> clazz)
    {
        String idColumn=null;
        for (Field declaredField : clazz.getDeclaredFields()) {
            IdColumn id=declaredField.getAnnotation(IdColumn.class);
            UpdateColumnName columnName=declaredField.getAnnotation(UpdateColumnName.class);
            if(id!=null)
            {
                if(columnName!=null)
                {
                    idColumn=columnName.name();
                }else {
                    idColumn=declaredField.getName();
                }
                break;
            }
        }
        if(idColumn==null)
        {
            throw new IllegalArgumentException("There is no ID in this entity");
        }
        String condition=" WHERE "+idColumn+" = ? ";

        return buildSelectQuery(clazz)+condition;
    }

    public String buildInsertQuery(Object obj) throws IllegalAccessException {
        //TODO rewrite to prepared statement
       Class <?> clazz=obj.getClass();
        OurEntity clazzEntityAnnotation=clazz.getAnnotation(OurEntity.class);
        if(clazzEntityAnnotation ==null)
        {
            throw new IllegalArgumentException("Class is not entity");
        }
        String tableName= clazzEntityAnnotation.tableName();
        StringBuilder query=new StringBuilder();
        query.append("INSERT INTO "+tableName+" (");
        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            Field declaredField=clazz.getDeclaredFields()[i];
            String fieldName= declaredField.getName();
            UpdateColumnName declaredFieldAnnotation=declaredField.getAnnotation(UpdateColumnName.class);
            if(declaredFieldAnnotation!=null)
            {
                query.append(declaredFieldAnnotation.name());
            }else {
                query.append(declaredField.getName());
            }
            if(i!=clazz.getDeclaredFields().length-1)
            {
                query.append(",");
            }
        }
        query.append(") VALUES (");
        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            Field declaredField=clazz.getDeclaredFields()[i];
            boolean isPrivate=declaredField.trySetAccessible();
            declaredField.setAccessible(true);
            Object value=declaredField.get(obj);
            declaredField.setAccessible(isPrivate);
            if(declaredField.getType().equals(Integer.class))
            {
                query.append(value);
            } else if (declaredField.getType().equals(String.class)) {
                query.append("'").append(value).append("'");
            }
            if(i!=clazz.getDeclaredFields().length-1)
            {
                query.append(",");
            }
        }
        query.append(")");
        return query.toString();
    }
}
