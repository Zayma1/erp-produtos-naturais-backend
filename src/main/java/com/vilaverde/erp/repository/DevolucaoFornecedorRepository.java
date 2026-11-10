package com.vilaverde.erp.repository;
import com.vilaverde.erp.entity.DevolucaoFornecedor;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface DevolucaoFornecedorRepository extends JpaRepository<DevolucaoFornecedor,UUID>, JpaSpecificationExecutor<DevolucaoFornecedor> {
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("select r from DevolucaoFornecedor r where r.id=:id") Optional<DevolucaoFornecedor> lockById(UUID id);
}
