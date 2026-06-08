package ai.analizza.cap.theorem.mysql.cassandra

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class TransacaoControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    private val mapper = ObjectMapper()
    private var contaId: Int = 0
    private var produtoId: Int = 0
    private var tipoId: Int = 0

    @BeforeEach
    fun setup() {
        val c = mockMvc.post("/api/clientes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"cpf":"${(10000000000..99999999999).random()}","cnpj":null,"nome":"Transator","email":"transator${System.nanoTime()}@test.com","segmento":"FISIOTERAPEUTA"}"""
        }.andReturn().response.contentAsString
        val clienteId = mapper.readTree(c).get("idCliente").asInt()

        val p = mockMvc.post("/api/produtos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nome":"ProdTrans ${System.nanoTime()}","categoria":"CREDITO","taxaJuros":1.0,"ativo":true}"""
        }.andReturn().response.contentAsString
        produtoId = mapper.readTree(p).get("idProduto").asInt()

        val ct = mockMvc.post("/api/contas") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":$clienteId,"numero":"T${System.nanoTime() % 100000000000L}","tipoConta":"PF","saldo":1000.0,"status":"ATIVA"}"""
        }.andReturn().response.contentAsString
        contaId = mapper.readTree(ct).get("idConta").asInt()

        val t = mockMvc.post("/api/tipos-transacao") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"descricao":"Tipo Trans ${System.nanoTime()}","sinal":1}"""
        }.andReturn().response.contentAsString
        tipoId = mapper.readTree(t).get("idTipo").asInt()
    }

    private fun transacaoJson(valor: String = "250.00", idem: String = UUID.randomUUID().toString()) =
        """{"idConta":$contaId,"idProduto":$produtoId,"idTipo":$tipoId,"idContratacao":null,"valor":$valor,"idIdempotencia":"$idem"}"""

    @Test
    fun `create transacao returns 201`() {
        mockMvc.post("/api/transacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = transacaoJson()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.idTransacao") { isNumber() }
        }
    }

    @Test
    fun `valor zero returns 400`() {
        mockMvc.post("/api/transacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = transacaoJson(valor = "0")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `negative valor returns 400`() {
        mockMvc.post("/api/transacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = transacaoJson(valor = "-10.00")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `duplicate idempotencia returns 409`() {
        val idem = UUID.randomUUID().toString()
        mockMvc.post("/api/transacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = transacaoJson(idem = idem)
        }.andExpect { status { isCreated() } }

        mockMvc.post("/api/transacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = transacaoJson(idem = idem)
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `list transacoes returns 200`() {
        mockMvc.get("/api/transacoes").andExpect { status { isOk() } }
    }

    @Test
    fun `full crud transacao`() {
        val body = mockMvc.post("/api/transacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = transacaoJson()
        }.andReturn().response.contentAsString

        val id = mapper.readTree(body).get("idTransacao").asLong()
        val idem = UUID.randomUUID().toString()

        mockMvc.get("/api/transacoes/$id").andExpect { status { isOk() } }

        mockMvc.put("/api/transacoes/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = transacaoJson(valor = "999.99", idem = idem)
        }.andExpect {
            status { isOk() }
            jsonPath("$.valor") { value(999.99) }
        }

        mockMvc.delete("/api/transacoes/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/transacoes/$id").andExpect { status { isNotFound() } }
    }
}
