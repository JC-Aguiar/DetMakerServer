package br.com.jcaguiar.cinephiles.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterProcessPage<OBJ> extends MasterProcess<OBJ> {

    final PageImpl<OBJ> body;

    public MasterProcessPage(@NotNull List<ProcessLine> processes) {
        super(processes);
        this.body = new PageImpl(getProcessContent(processes));
    }

    public MasterProcessPage(@NotNull List<ProcessLine> processes, @NotNull Pageable pageable) {
        super(processes);
        this.body = new PageImpl(
            getProcessContent(processes),
            pageable,
            processes.size());
    }

}
