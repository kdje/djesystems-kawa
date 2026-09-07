package com.djesystems.kawa.wallet.infrastructure.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEventEntity, String> {

    @Query(
        value = """
            SELECT *
            FROM outbox_event
            WHERE
                (
                    status = 'PENDING'
                    OR
                    (
                        status = 'PROCESSING'
                        AND processing_started_at < :staleBefore
                    )
                )
                AND attempt_count < :maxAttempts
            ORDER BY created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    List<OutboxEventEntity> findBatchForProcessing(
        @Param("staleBefore") Instant staleBefore,
        @Param("maxAttempts") int maxAttempts,
        @Param("batchSize") int batchSize
    );
}