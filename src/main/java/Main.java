import entities.HibernateLike;
import entities.OtherEntity;
import entities.SecondSimpleEntity;
import entities.SimpleEntity;
import services.QueryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws IllegalAccessException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        QueryBuilder queryBuilder=new QueryBuilder();
//        System.out.println(queryBuilder.buildCreateTableQuery(SimpleEntity.class));
//        System.out.println(queryBuilder.buildSelectQuery(SimpleEntity.class));
//        System.out.println(queryBuilder.buildSelectByIdQuery(SimpleEntity.class));
        SimpleEntity simpleEntity= new SimpleEntity();
        simpleEntity.setId(1);
        simpleEntity.setMyField("kkgkg");
        simpleEntity.setMyOtherField("kddldl");

        OtherEntity relatedEntity = new OtherEntity();
        relatedEntity.setId(2);
        relatedEntity.setField("jdjdj");
        simpleEntity.setOtherEntity(relatedEntity);

        SecondSimpleEntity secondEntity1 = new SecondSimpleEntity();
        secondEntity1.setId(3);
        secondEntity1.setField("kekkekekekek");
        SecondSimpleEntity secondEntity2 = new SecondSimpleEntity();
        secondEntity2.setId(4);
        secondEntity2.setField("");

        //simpleEntity.setEntities(List.of(secondEntity1, secondEntity2));


        HibernateLike hibernateLike=HibernateLike.getInstance("entities");
//        hibernateLike.initDb();

        hibernateLike.persist(simpleEntity);
//        SimpleEntity simpleEntity1=hibernateLike.getById(SimpleEntity.class,1);
//        System.out.println(simpleEntity1);

    }
}
