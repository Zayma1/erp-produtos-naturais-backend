package com.vilaverde.erp.repository;
import com.vilaverde.erp.entity.AjusteEstoque;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface AjusteEstoqueRepository extends JpaRepository<AjusteEstoque,UUID>, JpaSpecificationExecutor<AjusteEstoque> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from AjusteEstoque a where a.id=:id") Optional<AjusteEstoque> lockById(@Param("id") UUID id);
}
