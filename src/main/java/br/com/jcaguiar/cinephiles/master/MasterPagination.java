package br.com.jcaguiar.cinephiles.master;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public final class MasterPagination<T> {

    private static PageImpl<?> pagination;

    public static Page<?> pageResult(Object object) {
        return new PageImpl<Object>((List<Object>) new Object());
    }

}
