package br.com.jcaguiar.cinephiles.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterProcessPage<OBJ> extends MasterProcess<OBJ>  {

    final List<Object> body = new ArrayList<>();

    public MasterProcessPage(@NotNull ProcessLine process) {
        super(process);
        this.body.add(
            process.isObjectPresent() ? process.getObject() : Optional.empty()
        );
    }

    public MasterProcessPage(@NotNull List<ProcessLine> processes) {
        super(processes);
        this.body.addAll(getProcessContent(processes));
    }

}
