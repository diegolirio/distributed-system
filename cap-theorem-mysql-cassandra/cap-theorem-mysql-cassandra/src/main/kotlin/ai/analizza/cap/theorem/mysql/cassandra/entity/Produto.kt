package ai.analizza.cap.theorem.mysql.cassandra.entity

import jakarta.persistence.*
import java.math.BigDecimal

enum class CategoriaProduto { CREDITO, INVESTIMENTO, SAAS, SERVICO }

@Entity
@Table(name = "produto")
class Produto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    val idProduto: Int = 0,

    @Column(name = "nome", nullable = false, unique = true, length = 120)
    var nome: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    var categoria: CategoriaProduto = CategoriaProduto.SERVICO,

    @Column(name = "taxa_juros", nullable = false, precision = 7, scale = 4)
    var taxaJuros: BigDecimal = BigDecimal.ZERO,

    @Column(name = "ativo", nullable = false)
    var ativo: Boolean = true
)
