package entities;

import annotations.field.IdColumn;
import annotations.field.NotNullValue;
import annotations.field.UniqueValue;
import annotations.field.UpdateColumnName;
import annotations.relation.JoinColumn;
import annotations.relation.ManyToOneJoin;
import annotations.table.OurEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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


    @ManyToOneJoin(table = "other_entity")
    @JoinColumn(name = "other_entity_id",referencedColumnName = "id")
    private OtherEntity otherEntity;


}
