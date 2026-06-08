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
class ProdutoControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    private val mapper = ObjectMapper()

    @Test
    fun `create produto returns 201`() {
        mockMvc.post("/api/produtos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nome":"Score Hibrido","categoria":"CREDITO","taxaJuros":1.5,"ativo":true}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.idProduto") { isNumber() }
        }
    }

    @Test
    fun `negative taxaJuros returns 400`() {
        mockMvc.post("/api/produtos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nome":"Produto Invalido","categoria":"CREDITO","taxaJuros":-1.0,"ativo":true}"""
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `list produtos returns 200`() {
        mockMvc.get("/api/produtos").andExpect { status { isOk() } }
    }

    @Test
    fun `full crud produto`() {
        val body = mockMvc.post("/api/produtos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nome":"Kit Abertura PJ","categoria":"SAAS","taxaJuros":0.0,"ativo":true}"""
        }.andReturn().response.contentAsString

        val id = mapper.readTree(body).get("idProduto").asInt()

        mockMvc.get("/api/produtos/$id").andExpect { status { isOk() } }

        mockMvc.put("/api/produtos/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nome":"Kit Abertura PJ","categoria":"SAAS","taxaJuros":2.5,"ativo":false}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.ativo") { value(false) }
        }

        mockMvc.delete("/api/produtos/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/produtos/$id").andExpect { status { isNotFound() } }
    }
}
