package services;

import annotations.relation.JoinColumn;
import annotations.relation.JoinMany;
import annotations.relation.ManyToOneJoin;
import annotations.table.OurEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RelationBuilder {


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

    public List<String> buildManyToMany(Class<?> clazz)
    {
        List<String> queries=new ArrayList<>();
        OurEntity entityAnnotation = clazz.getAnnotation(OurEntity.class);
        if (entityAnnotation == null) {
            throw new IllegalArgumentException("Class is not annotated with @OurEntity");
        }
        String tableName = entityAnnotation.tableName();
        boolean hasForeignKeys = false;
        for (Field field : clazz.getDeclaredFields()) {
            JoinMany manyToManyJoin = field.getAnnotation(JoinMany.class);
    }
}
