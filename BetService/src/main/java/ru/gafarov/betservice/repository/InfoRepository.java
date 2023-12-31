package ru.gafarov.betservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gafarov.bet.grpcInterface.Info;
import ru.gafarov.betservice.entity.InfoEntity;

public interface InfoRepository extends JpaRepository<InfoEntity, Info.InfoType> {
}
