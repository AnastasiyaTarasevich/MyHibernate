import entities.SimpleEntity;
import services.QueryBuilder;

import java.sql.SQLException;

public class Main
{
    public static void main(String[] args) throws IllegalAccessException, SQLException {
        QueryBuilder queryBuilder=new QueryBuilder();
        System.out.println(queryBuilder.buildCreateTableQuery(SimpleEntity.class));
        System.out.println(queryBuilder.buildSelectQuery(SimpleEntity.class));
        System.out.println(queryBuilder.buildSelectByIdQuery(SimpleEntity.class));
    }
}
