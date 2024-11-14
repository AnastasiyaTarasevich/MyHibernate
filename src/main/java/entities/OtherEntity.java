package entities;


import annotations.field.IdColumn;
import annotations.field.NotNullValue;
import annotations.table.OurEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@OurEntity(tableName = "other_entity")
public class OtherEntity {

    @IdColumn
    @NotNullValue
    private Integer id;

    private String field;
}
