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
class ContaControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    private val mapper = ObjectMapper()
    private var clienteId: Int = 0

    @BeforeEach
    fun setup() {
        val body = mockMvc.post("/api/clientes") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"cpf":"${(10000000000..99999999999).random()}","cnpj":null,"nome":"Titular","email":"titular${System.nanoTime()}@test.com","segmento":"MEDICO"}"""
        }.andReturn().response.contentAsString
        clienteId = mapper.readTree(body).get("idCliente").asInt()
    }

    @Test
    fun `create conta returns 201`() {
        mockMvc.post("/api/contas") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":$clienteId,"numero":"CT${System.nanoTime() % 100000000000L}","tipoConta":"PF","saldo":100.0,"status":"ATIVA"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.idConta") { isNumber() }
        }
    }

    @Test
    fun `non existent cliente returns 404`() {
        mockMvc.post("/api/contas") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":999999,"numero":"CONTA-INVALID","tipoConta":"PF","saldo":0.0,"status":"ATIVA"}"""
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `list contas returns 200`() {
        mockMvc.get("/api/contas").andExpect { status { isOk() } }
    }

    @Test
    fun `full crud conta`() {
        val numero = "CT${System.nanoTime() % 100000000000L}"
        val body = mockMvc.post("/api/contas") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":$clienteId,"numero":"${numero}","tipoConta":"PF","saldo":500.0,"status":"ATIVA"}"""
        }.andReturn().response.contentAsString

        val id = mapper.readTree(body).get("idConta").asInt()

        mockMvc.get("/api/contas/$id").andExpect { status { isOk() } }

        mockMvc.put("/api/contas/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idCliente":$clienteId,"numero":"$numero","tipoConta":"PF","saldo":500.0,"status":"BLOQUEADA"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("BLOQUEADA") }
        }

        mockMvc.delete("/api/contas/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/contas/$id").andExpect { status { isNotFound() } }
    }
}
