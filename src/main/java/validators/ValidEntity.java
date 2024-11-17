package validators;

import annotations.table.OurEntity;

public class ValidEntity {


    public  String getTableName(Object obj) {
        Class<?> clazz = obj.getClass();
        OurEntity clazzEntityAnnotation = clazz.getAnnotation(OurEntity.class);
        if (clazzEntityAnnotation == null) {
            throw new IllegalArgumentException("Class is not entity");
        }
        return clazzEntityAnnotation.tableName();
    }
}
