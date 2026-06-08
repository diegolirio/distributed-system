package com.example.demo.oracle.skipped.lock.web

import com.example.demo.oracle.skipped.lock.service.PagamentoProcessingService
import com.example.demo.oracle.skipped.lock.service.ProcessingResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pagamentos")
class PagamentosController(private val processingService: PagamentoProcessingService) {

    @PostMapping("/processar")
    fun processar(): ResponseEntity<ProcessingResult> {
        val result = processingService.runLoop()
        return ResponseEntity.ok(result)
    }
}
