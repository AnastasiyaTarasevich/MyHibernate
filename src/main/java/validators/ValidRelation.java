package validators;

import annotations.relation.JoinMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class ValidRelation {

    public boolean hasManyToManyRelation(Class<?> clazz1, Class<?> clazz2) {
        JoinMany relation1 = null;
        JoinMany relation2 = null;

        for (Field field : clazz1.getDeclaredFields()) {
            relation1 = field.getAnnotation(JoinMany.class);
            if (relation1 != null) {
                break;
            }
        }

        for (Field field : clazz2.getDeclaredFields()) {
            relation2 = field.getAnnotation(JoinMany.class);
            if (relation2 != null) {
                break;
            }
        }

        if (relation1 == null || relation2 == null) {
            return false;
        }

        return validateRelationParameters(relation1, relation2);
    }

    private boolean validateRelationParameters(JoinMany relation1, JoinMany relation2) {
        if (!relation1.joinTable().equals(relation2.joinTable())) {
            return false;
        }


        if (!relation1.joinColumn().equals(relation2.inverseJoinColumn()) ||
                !relation2.joinColumn().equals(relation1.inverseJoinColumn())) {
            return false;
        }

        return true;
    }
    public Class<?> getGenericType(Field field) {
        if (!Collection.class.isAssignableFrom(field.getType())) {
            return null;
        }

        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Type[] actualTypeArguments = genericType.getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return null;
        }

        return (Class<?>) actualTypeArguments[0];
    }
}
