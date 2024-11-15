package validators;

import annotations.table.OurEntity;

public class ValidEntity {

    public boolean hasEntityAnnotation(Class<?> clazz)
    {
        OurEntity entityAnnotation = clazz.getAnnotation(OurEntity.class);
        if (entityAnnotation == null) {
            throw new IllegalArgumentException("Class is not annotated with @OurEntity");
        }
        return true;
    }
    public String getTableName(Class<?> clazz) {

        OurEntity entityAnnotation = clazz.getAnnotation(OurEntity.class);
        if (entityAnnotation != null) {

            String tableName = entityAnnotation.tableName();
            if (!tableName.isEmpty()) {
                return tableName;
            }
        }

       return null;
    }
}
