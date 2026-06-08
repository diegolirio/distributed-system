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

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ContratacaoControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    private val mapper = ObjectMapper()
    private var clienteId: Int = 0
    private var produtoId: Int = 0

    @BeforeEach
    fun setup() {
        val c = mockMvc.post("/api/clientes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"cpf":"${(10000000000..99999999999).random()}","cnpj":null,"nome":"Contratante","email":"contratante${System.nanoTime()}@test.com","segmento":"DENTISTA"}"""
        }.andReturn().response.contentAsString
        clienteId = mapper.readTree(c).get("idCliente").asInt()

        val p = mockMvc.post("/api/produtos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nome":"Produto Contr ${System.nanoTime()}","categoria":"INVESTIMENTO","taxaJuros":0.5,"ativo":true}"""
        }.andReturn().response.contentAsString
        produtoId = mapper.readTree(p).get("idProduto").asInt()
    }

    @Test
    fun `create contratacao returns 201`() {
        mockMvc.post("/api/contratacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":$clienteId,"idProduto":$produtoId,"dataContratacao":"2024-01-15","status":"ATIVA"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.idContratacao") { isNumber() }
        }
    }

    @Test
    fun `duplicate contratacao returns 409`() {
        val body = """{"idCliente":$clienteId,"idProduto":$produtoId,"dataContratacao":"2024-06-01","status":"ATIVA"}"""
        mockMvc.post("/api/contratacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isCreated() } }

        mockMvc.post("/api/contratacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `list contratacoes returns 200`() {
        mockMvc.get("/api/contratacoes").andExpect { status { isOk() } }
    }

    @Test
    fun `full crud contratacao`() {
        val body = mockMvc.post("/api/contratacoes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":$clienteId,"idProduto":$produtoId,"dataContratacao":"2024-03-10","status":"ATIVA"}"""
        }.andReturn().response.contentAsString

        val id = mapper.readTree(body).get("idContratacao").asInt()

        mockMvc.get("/api/contratacoes/$id").andExpect { status { isOk() } }

        mockMvc.put("/api/contratacoes/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":$clienteId,"idProduto":$produtoId,"dataContratacao":"2024-03-10","status":"LIQUIDADA"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("LIQUIDADA") }
        }

        mockMvc.delete("/api/contratacoes/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/contratacoes/$id").andExpect { status { isNotFound() } }
    }
}
