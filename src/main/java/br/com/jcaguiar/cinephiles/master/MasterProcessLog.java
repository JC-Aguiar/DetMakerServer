package br.com.jcaguiar.cinephiles.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

//TODO: Deletar!

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterProcessLog<OBJ> extends MasterProcessManager<OBJ> {

//    final String message;
//
//    public MasterProcessLog(@NotNull List<MasterServiceResult> processes, @NotBlank String message) {
////        super(processes);
//        switch(getStatus()) {
//            case PARCIAL -> this.message = "Process complete, with some exceptions. Consult the log for more details.";
//            case FAIL -> this.message = "An error ocorrer during the process. Consult the log for more details.";
//            case EMPTY -> this.message = "No result. The requested action found 0 records in the database.";
//            default -> this.message = message;
//        }
//    }

}
