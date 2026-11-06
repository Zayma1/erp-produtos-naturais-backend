package com.vilaverde.erp.repository;
import com.vilaverde.erp.entity.DevolucaoCliente;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface DevolucaoClienteRepository extends JpaRepository<DevolucaoCliente,UUID>, JpaSpecificationExecutor<DevolucaoCliente> {
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("select r from DevolucaoCliente r where r.id=:id") Optional<DevolucaoCliente> lockById(UUID id);
}
