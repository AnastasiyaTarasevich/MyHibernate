package services;


import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@AllArgsConstructor
@Getter
public class QueryWithParameters {

    private final String query;
    private final List<Object> parameters;


}
