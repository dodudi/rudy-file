package kr.it.rudy.file.common.persistence;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @CreatedDate
    private Instant createdDt;

    @LastModifiedDate
    private Instant updatedDt;

    public void setCreatedDt(Instant createdDt) {
        this.createdDt = createdDt;
    }
}
