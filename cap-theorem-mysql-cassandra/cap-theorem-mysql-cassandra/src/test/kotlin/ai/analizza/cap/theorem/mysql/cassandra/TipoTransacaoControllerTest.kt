package ai.analizza.cap.theorem.mysql.cassandra

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
class TipoTransacaoControllerTest {

    @Autowired lateinit var mockMvc: MockMvc

    @Test
    fun `create tipo transacao returns 201`() {
        mockMvc.post("/api/tipos-transacao") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"descricao":"Aquisicao","sinal":1}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.idTipo") { isNumber() }
            jsonPath("$.sinal") { value(1) }
        }
    }

    @Test
    fun `invalid sinal returns 400`() {
        mockMvc.post("/api/tipos-transacao") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"descricao":"Invalido","sinal":0}"""
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `list tipos transacao returns 200`() {
        mockMvc.get("/api/tipos-transacao").andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `full crud tipo transacao`() {
        val created = mockMvc.post("/api/tipos-transacao") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"descricao":"Resgate","sinal":-1}"""
        }.andExpect { status { isCreated() } }
            .andReturn().response.contentAsString

        val id = com.fasterxml.jackson.databind.ObjectMapper().readTree(created).get("idTipo").asInt()

        mockMvc.get("/api/tipos-transacao/$id").andExpect { status { isOk() } }

        mockMvc.put("/api/tipos-transacao/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"descricao":"Resgate Updated","sinal":-1}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.descricao") { value("Resgate Updated") }
        }

        mockMvc.delete("/api/tipos-transacao/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/tipos-transacao/$id").andExpect { status { isNotFound() } }
    }
}
