package services;

import annotations.relation.JoinColumn;
import annotations.relation.JoinMany;
import annotations.relation.ManyToOneJoin;
import annotations.table.OurEntity;
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
        OurEntity entityAnnotation = clazz.getAnnotation(OurEntity.class);
        if (entityAnnotation == null) {
            throw new IllegalArgumentException("Class is not annotated with @OurEntity");
        }
        String tableName = entityAnnotation.tableName();
        boolean hasForeignKeys = false;
        for (Field field : clazz.getDeclaredFields()) {
            ManyToOneJoin manyToOneJoin = field.getAnnotation(ManyToOneJoin.class);
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

            if (manyToOneJoin != null && joinColumn != null) {
                String columnName = joinColumn.name();
                String referencedTable = manyToOneJoin.table();
                String referencedColumnName = joinColumn.referencedColumnName();


                String addColumnQuery = "ALTER TABLE " + tableName + " ADD " + columnName + " INT";
                queries.add(addColumnQuery);

                String addForeignKeyQuery = "ALTER TABLE " + tableName + " ADD CONSTRAINT fk_"
                        + clazz.getSimpleName() + "_" + columnName
                        + " FOREIGN KEY (" + columnName + ") REFERENCES "
                        + referencedTable + " (" + referencedColumnName + ")";
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
                    // Получаем параметры аннотации
                    String joinTable = joinMany.joinTable();
                    String joinColumn = joinMany.joinColumn();
                    String inverseJoinColumn = joinMany.inverseJoinColumn();

                    // Определяем связанные классы
                    Class<?> relatedClass = validRelation.getGenericType(field);
                    String mainTable = validEntity.getTableName(clazz);
                    String relatedTable = validEntity.getTableName(relatedClass);

                    // Генерация SQL-запроса для создания таблицы связи
                    String createQuery = String.format(
                            "CREATE TABLE IF NOT EXISTS %s (" +
                                    "    %s INT NOT NULL, " +
                                    "    %s INT NOT NULL, " +
                                    "    PRIMARY KEY (%s, %s), " +
                                    "    FOREIGN KEY (%s) REFERENCES %s(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                                    "    FOREIGN KEY (%s) REFERENCES %s(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                                    ")",
                            joinTable,              // Имя таблицы связи
                            joinColumn,             // Поле для внешнего ключа основного класса
                            inverseJoinColumn,      // Поле для внешнего ключа связанного класса
                            joinColumn, inverseJoinColumn, // Первичный ключ - два столбца
                            joinColumn, mainTable,        // Внешний ключ на основной класс
                            inverseJoinColumn, relatedTable // Внешний ключ на связанный класс
                    );

                    queries.add(createQuery);
                }
            }
        }

        return queries;
    }






}
