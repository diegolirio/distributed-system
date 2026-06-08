package ai.analizza.cap.theorem.mysql.cassandra

import com.fasterxml.jackson.databind.ObjectMapper
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
class ClienteControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    private val mapper = ObjectMapper()

    private fun clienteJson(cpf: String, email: String) =
        """{"cpf":"$cpf","cnpj":null,"nome":"Dr. Teste","email":"$email","segmento":"MEDICO"}"""

    @Test
    fun `create cliente returns 201`() {
        mockMvc.post("/api/clientes") {
            contentType = MediaType.APPLICATION_JSON
            content = clienteJson("11122233301", "medico1@test.com")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.idCliente") { isNumber() }
            jsonPath("$.segmento") { value("MEDICO") }
        }
    }

    @Test
    fun `duplicate cpf returns 409`() {
        val cpf = "99988877701"
        mockMvc.post("/api/clientes") {
            contentType = MediaType.APPLICATION_JSON
            content = clienteJson(cpf, "unique1@test.com")
        }.andExpect { status { isCreated() } }

        mockMvc.post("/api/clientes") {
            contentType = MediaType.APPLICATION_JSON
            content = clienteJson(cpf, "unique2@test.com")
        }.andExpect { status { isConflict() } }
    }

    @Test
    fun `list clientes returns 200`() {
        mockMvc.get("/api/clientes").andExpect { status { isOk() } }
    }

    @Test
    fun `get by id not found returns 404`() {
        mockMvc.get("/api/clientes/999999").andExpect { status { isNotFound() } }
    }

    @Test
    fun `full crud cliente`() {
        val body = mockMvc.post("/api/clientes") {
            contentType = MediaType.APPLICATION_JSON
            content = clienteJson("55544433301", "crud@test.com")
        }.andReturn().response.contentAsString

        val id = mapper.readTree(body).get("idCliente").asInt()

        mockMvc.get("/api/clientes/$id").andExpect { status { isOk() } }

        mockMvc.put("/api/clientes/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"cpf":"55544433301","cnpj":null,"nome":"Dr. Updated","email":"crud@test.com","segmento":"DENTISTA"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.segmento") { value("DENTISTA") }
        }

        mockMvc.delete("/api/clientes/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/clientes/$id").andExpect { status { isNotFound() } }
    }
}
