import entities.HibernateLike;
import entities.SimpleEntity;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args) throws IllegalAccessException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException {
//        QueryBuilder queryBuilder=new QueryBuilder();
//        System.out.println(queryBuilder.buildCreateTableQuery(SimpleEntity.class));
//        System.out.println(queryBuilder.buildSelectQuery(SimpleEntity.class));
//        System.out.println(queryBuilder.buildSelectByIdQuery(SimpleEntity.class));
        SimpleEntity simpleEntity= new SimpleEntity();
        simpleEntity.setId(1);
        simpleEntity.setMyField("kkgkg");
        simpleEntity.setMyOtherField("kddldl");
//        System.out.println(queryBuilder.buildInsertQuery(simpleEntity));

        HibernateLike hibernateLike=new HibernateLike("entities");
        hibernateLike.initDb();
//        hibernateLike.persist(simpleEntity);
        SimpleEntity simpleEntity1=hibernateLike.getById(SimpleEntity.class,1);
        System.out.println(simpleEntity1);
    }
}
