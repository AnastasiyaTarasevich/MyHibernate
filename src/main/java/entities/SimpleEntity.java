package entities;

import annotations.*;

@OurEntity(tableName = "simple_table")
public class SimpleEntity {

    @IdColumn
    @NotNullValue
    @UniqueValue
    private Integer id;

    @NotNullValue
    @UpdateColumnName(name="other_column")
    private String myField;

    private String myOtherField;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMyField() {
        return myField;
    }

    public void setMyField(String myField) {
        this.myField = myField;
    }

    public String getMyOtherField() {
        return myOtherField;
    }

    public void setMyOtherField(String myOtherField) {
        this.myOtherField = myOtherField;
    }

    @Override
    public String toString() {
        return "SimpleEntity{" +
                "id=" + id +
                ", myField='" + myField + '\'' +
                ", myOtherField='" + myOtherField + '\'' +
                '}';
    }
}
