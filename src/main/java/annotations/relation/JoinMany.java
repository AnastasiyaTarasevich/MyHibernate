package annotations.relation;

import constants.Cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinMany {
    Cascade[] cascade() default Cascade.ALL;
    String joinTable();
    String joinColumn();
    String inverseJoinColumn();
}
