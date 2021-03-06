package br.com.basis.abaco.domain.audit;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Embeddable
public class AbacoAudit {

    @CreatedDate
    @Column(name = "created_on", nullable = false)
    @NotNull
    private ZonedDateTime createdOn;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    @NotNull
    private ZonedDateTime updatedOn;
    
    {
        ZonedDateTime now = ZonedDateTime.now();
        createdOn = now;
        updatedOn = now;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

}
