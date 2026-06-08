package com.example.demo.oracle.skipped.lock.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Configuration
class TransactionConfig {

    /**
     * Spring Boot auto-configures a [PlatformTransactionManager] (JPA) but not a
     * [TransactionTemplate]. PagamentoProcessingService uses programmatic
     * transactions to bound each batch (100 items) in a single commit, so we
     * expose the template explicitly here.
     */
    @Bean
    fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate =
        TransactionTemplate(transactionManager)
}
