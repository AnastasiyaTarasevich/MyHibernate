package entities;

import annotations.field.IdColumn;
import annotations.field.NotNullValue;
import annotations.relation.JoinMany;
import annotations.table.OurEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@OurEntity(tableName = "second_table")
public class SecondSimpleEntity {
    @IdColumn
    @NotNullValue
    private Integer id;

    @NotNullValue
    private String field;

    @JoinMany(joinTable = "simple_second", joinColumn = "second_id",inverseJoinColumn = "simple_id")
    private List<SimpleEntity> entityList;
}
