package com.vilaverde.erp.repository;
import com.vilaverde.erp.entity.TransferenciaEstoque;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
public interface TransferenciaEstoqueRepository extends JpaRepository<TransferenciaEstoque,UUID>, JpaSpecificationExecutor<TransferenciaEstoque> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select t from TransferenciaEstoque t where t.id=:id")
 Optional<TransferenciaEstoque> lockById(UUID id);
}
