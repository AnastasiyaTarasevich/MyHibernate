package constants;

public final class Queries {

    public static final String TABLE_FOR_MANY_TO_MANY= "CREATE TABLE IF NOT EXISTS %s (" +
            "    %s INT NOT NULL, " +
            "    %s INT NOT NULL, " +
            "    PRIMARY KEY (%s, %s), " +
            "    FOREIGN KEY (%s) REFERENCES %s(id) ON DELETE CASCADE ON UPDATE CASCADE, " +
            "    FOREIGN KEY (%s) REFERENCES %s(id) ON DELETE CASCADE ON UPDATE CASCADE" +
            ")";

    public static final String ADD_COLUMN_FOR_FOREIGN_KEY = "ALTER TABLE %s ADD %s INT";
    public static final String ADD_FOREIGN_KEY = "ALTER TABLE %s ADD CONSTRAINT fk_%s_%s FOREIGN KEY (%s) REFERENCES %s (%s)";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s (%s);";

    public static final String INSERT = "INSERT INTO %s (%s) VALUES (%s)";
}
