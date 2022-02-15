package br.com.jcaguiar.cinephiles.master;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public interface NameableEntity {

    public String getFullName();

    public String getInitialsName();

    public static String findInitials(@NotBlank String name) {
        return Arrays.stream(name.split(" "))
                .map(n -> n.charAt(0)).toString();
    };

    public static String findInitials(@NotNull List<String> names) {
        return names.stream().map(NameableEntity::findInitials).toString();
    };

    public static String findInitialsInSoloName(@NotBlank String name) {
        final int letters = name.length();
        if (letters <= 1) { return name; }
        final String initials = String.valueOf(name.charAt(0) + name.charAt(1));
        if (letters <= 2) { return initials; }
        return initials + name.chars().skip(2).toString().charAt(0);
    }

}
