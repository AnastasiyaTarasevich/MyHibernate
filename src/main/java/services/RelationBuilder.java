package services;

import annotations.relation.JoinColumn;
import annotations.relation.JoinMany;
import annotations.relation.ManyToOneJoin;
import annotations.table.OurEntity;
import constants.Queries;
import lombok.AllArgsConstructor;
import validators.ValidEntity;
import validators.ValidRelation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class RelationBuilder {

    private static final ValidRelation validRelation = new ValidRelation();
    private static final ValidEntity validEntity=new ValidEntity();

    public List<String> buildManyToOneRelation(Class<?> clazz) {
        List<String> queries = new ArrayList<>();
        String tableName = validEntity.getTableName(clazz);
        boolean hasForeignKeys = false;
        for (Field field : clazz.getDeclaredFields()) {
            ManyToOneJoin manyToOneJoin = field.getAnnotation(ManyToOneJoin.class);
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

            if (manyToOneJoin != null && joinColumn != null) {
                String columnName = joinColumn.name();
                String referencedTable = manyToOneJoin.table();
                String referencedColumnName = joinColumn.referencedColumnName();


                String addColumnQuery = String.format(Queries.ADD_COLUMN_FOR_FOREIGN_KEY, tableName, columnName);
                queries.add(addColumnQuery);

                String addForeignKeyQuery = String.format(Queries.ADD_FOREIGN_KEY,
                        tableName, clazz.getSimpleName(), columnName, columnName, referencedTable, referencedColumnName);
                queries.add(addForeignKeyQuery);

                hasForeignKeys = true;
            }
        }
        if (!hasForeignKeys) {
            return new ArrayList<>();
        }
        return queries;
    }

    public List<String> buildManyToManyRelation(Set<Class> allClasses) {
        List<String> queries = new ArrayList<>();

        for (Class<?> clazz : allClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                JoinMany joinMany = field.getAnnotation(JoinMany.class);
                if (joinMany != null) {
                    String joinTable = joinMany.joinTable();
                    String joinColumn = joinMany.joinColumn();
                    String inverseJoinColumn = joinMany.inverseJoinColumn();

                    // Определяем связанные классы
                    Class<?> relatedClass = validRelation.getGenericType(field);
                    String mainTable = validEntity.getTableName(clazz);
                    String relatedTable = validEntity.getTableName(relatedClass);

                    // Генерация SQL-запроса для создания таблицы связи
                    String createQuery = String.format(
                            Queries.TABLE_FOR_MANY_TO_MANY,
                            joinTable,
                            joinColumn,
                            inverseJoinColumn,
                            joinColumn, inverseJoinColumn,
                            joinColumn, mainTable,
                            inverseJoinColumn, relatedTable
                    );

                    queries.add(createQuery);
                }
            }
        }

        return queries;
    }






}
