package br.com.jcaguiar.cinephiles.movie;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class PostersModel {

    @URL(message = "insert a valid url")
    String url;

    //Lob (ODL BUG)
    @Type(type="org.hibernate.type.BinaryType")
    @Column(columnDefinition = "bytea")
    @NotNull(message = "image file cant be empty/null")
    byte[] image;

}
