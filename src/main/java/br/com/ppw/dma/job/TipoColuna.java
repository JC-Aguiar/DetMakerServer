package br.com.ppw.dma.job;

import lombok.val;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;

public enum TipoColuna {
    NUMBER(BigDecimal.class),
    DECIMAL(BigDecimal.class),
    LONG(Long.class),
    FLOAT(Float.class),
    BINARY_FLOAT(Float.class),
    BINARY_DOUBLE(Double.class),
    CHAR(String.class),
    VARCHAR(String.class),
    VARCHAR2(String.class),
    TIMESTAMP(OffsetDateTime.class),
    DATE(OffsetDateTime.class),
    NCHAR(String.class),
    NVARCHAR(String.class),
    NVARCHAR2(String.class);
    //ROWID(String.class),
    //UROWID(String.class),
    //INTERVAL_YEAR_TO_MONTH(String.class),
    //INTERVAL_DAY_TO_SECOND(String.class),
    //BFILE(BFILE.class),
    //XMLTYPE(String.class);
    
    public final Class<?> javaClass;
    
    TipoColuna(Class<?> javaClass) {
        this.javaClass = javaClass;
    }
    
    public static Optional<TipoColuna> getTipoColunaPeloNome(String nome) {
        return Arrays.stream(TipoColuna.values())
            .filter(tipo -> {
                val tipoString = tipo.name().replace("_", " ");
                return nome.toUpperCase().contains(tipoString);
            }).findFirst();
    }
    
    public static Optional<TipoColuna> getTipoColunaPelaClasse(Class<?> javaClass) {
        return Arrays.stream(TipoColuna.values())
            .filter(tipo -> tipo.javaClass.equals(javaClass))
            .findFirst();
    }
    
    public static boolean campoData(TipoColuna tipo) {
        switch(tipo) {
            case DATE:
            case TIMESTAMP:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean possuiTamanho(TipoColuna tipo) {
        switch(tipo) {
            case CHAR:
            case NCHAR:
            case VARCHAR:
            case VARCHAR2:
            case NVARCHAR2:
            case LONG:
                return true;
            default:
                return false;
        }
    }
    
    public static boolean possuiDigitos(TipoColuna tipo) {
        switch(tipo) {
            case NUMBER:
            case DECIMAL:
            case FLOAT:
            case BINARY_FLOAT:
            case BINARY_DOUBLE:
                return true;
            default:
                return false;
        }
    }
    
}
