package br.com.basis.abaco.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Der.
 */
@Entity
@Table(name = "der")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "der")
public class Der implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "nome", nullable = false)
    private String nome;

    @ManyToOne
    private Rlr rlr;

    @ManyToOne
    private FuncaoDados funcaoDados;

    @ManyToOne
    private FuncaoTransacao funcaoTransacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public Der nome(String nome) {
        this.nome = nome;
        return this;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Rlr getRlr() {
        return rlr;
    }

    public Der rlr(Rlr rlr) {
        this.rlr = rlr;
        return this;
    }

    public void setRlr(Rlr rlr) {
        this.rlr = rlr;
    }

    public FuncaoDados getFuncaoDados() {
        return funcaoDados;
    }

    public void setFuncaoDados(FuncaoDados funcaoDados) {
        this.funcaoDados = funcaoDados;
    }

    public FuncaoTransacao getFuncaoTransacao() {
        return funcaoTransacao;
    }

    public void setFuncaoTransacao(FuncaoTransacao funcaoTransacao) {
        this.funcaoTransacao = funcaoTransacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Der der = (Der) o;
        if (der.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, der.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Der{" + "id=" + id + ", nome='" + nome + "'" + '}';
    }
}
