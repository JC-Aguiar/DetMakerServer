package br.com.ppw.dma.domain.ambiente;

import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_AMBIENTE")
@Table(name = "PPW_AMBIENTE", uniqueConstraints = {@UniqueConstraint(columnNames = {"NOME", "CLIENTE_ID"})})
@SequenceGenerator(name = "SEQ_AMBIENTE_ID", sequenceName = "RCVRY.SEQ_AMBIENTE_ID", allocationSize = 1)
public class Ambiente implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AMBIENTE_ID")
    // Identificador numérico do Ambiente
    Long id;

    @Column(name = "NOME", length = 50, unique = true)
    // Nome do Ambiente
    String nome;

    @Column(name = "CONEXAO_SFTP", length = 25)
    // Conexão para acessar ao sftp do Ambiente, no padrão ${IP}:${POSTA}
    String conexaoSftp;

    @Column(name = "USUARIO_SFTP", length = 100)
    // Usuário para login de conexão sftp do Ambiente
    String usuarioSftp;

    @ToString.Exclude
    @Column(name = "SENHA_SFTP", length = 200)
    // Senha para login de conexão sftp do Ambiente
    String senhaSftp;

    @Column(name = "CONEXAO_BANCO", length = 75)
    // Conexão para acessar o banco do Ambiente, no padrão ${IP}:${POSTA}:${SID}
    // Deverá ser usado para concatenar com 'jdbc:oracle:thin:@' + ${conexaoBanco}
    String conexaoBanco;

    @Column(name = "USUARIO_BANCO", length = 100)
    // Usuário para login de conexão ao banco do Ambiente
    String usuarioBanco;

    @ToString.Exclude
    @Column(name = "SENHA_BANCO", length = 200)
    // Senha para login de conexão ao banco do Ambiente
    String senhaBanco;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CLIENTE_ID", referencedColumnName = "ID") )
    // Cliente associado a este ambiente
    Cliente cliente; //TODO: precisa ser não-nulo


    public Ambiente(@NonNull AmbienteInfoDTO dto, @NonNull Cliente cliente) {
        this.nome = dto.getNome();
        this.conexaoSftp = dto.getFtp().getConexao();
        this.usuarioSftp = dto.getFtp().getUsuario();
        this.senhaSftp = dto.getFtp().getSenha();
        this.conexaoBanco = dto.getBanco().getConexao();
        this.usuarioBanco = dto.getBanco().getUsuario();
        this.senhaBanco = dto.getBanco().getSenha();
        this.cliente = cliente;
    }

    public AmbienteAcessoDTO acessoBanco() {
        return AmbienteAcessoDTO.banco(this);
    }

    public AmbienteAcessoDTO acessoFtp() {
        return AmbienteAcessoDTO.ftp(this);
    }

}
