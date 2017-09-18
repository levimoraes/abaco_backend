package br.com.basis.abaco.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Alr.
 */
@Entity
@Table(name = "alr")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "alr")
public class Alr implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne
    private FuncaoTransacao funcaoTransacao;

    @OneToMany(mappedBy = "alr")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<FuncaoDados> funcaoDados = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FuncaoTransacao getFuncaoTransacao() {
        return funcaoTransacao;
    }

    public Alr funcaoTransacao(FuncaoTransacao funcaoTransacao) {
        this.funcaoTransacao = funcaoTransacao;
        return this;
    }

    public void setFuncaoTransacao(FuncaoTransacao funcaoTransacao) {
        this.funcaoTransacao = funcaoTransacao;
    }

    public Set<FuncaoDados> getFuncaoDados() {
        return funcaoDados;
    }

    public Alr funcaoDados(Set<FuncaoDados> funcaoDados) {
        this.funcaoDados = funcaoDados;
        return this;
    }

    public Alr addFuncaoDados(FuncaoDados funcaoDados) {
        this.funcaoDados.add(funcaoDados);
        funcaoDados.setAlr(this);
        return this;
    }

    public Alr removeFuncaoDados(FuncaoDados funcaoDados) {
        this.funcaoDados.remove(funcaoDados);
        funcaoDados.setAlr(null);
        return this;
    }

    public void setFuncaoDados(Set<FuncaoDados> funcaoDados) {
        this.funcaoDados = funcaoDados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Alr alr = (Alr) o;
        if (alr.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, alr.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Alr{" +
            "id=" + id +
            '}';
    }
}
