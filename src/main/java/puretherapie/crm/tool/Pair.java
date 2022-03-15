package puretherapie.crm.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pair<T, S> {

    private T first;
    private S second;

}
