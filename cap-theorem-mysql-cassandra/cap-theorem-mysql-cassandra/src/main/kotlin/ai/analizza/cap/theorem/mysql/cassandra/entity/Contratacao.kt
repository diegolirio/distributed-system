package ai.analizza.cap.theorem.mysql.cassandra.entity

import jakarta.persistence.*
import java.time.LocalDate

enum class StatusContratacao { ATIVA, SUSPENSA, LIQUIDADA, CANCELADA }

@Entity
@Table(name = "contratacao")
class Contratacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contratacao")
    val idContratacao: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    var cliente: Cliente = Cliente(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    var produto: Produto = Produto(),

    @Column(name = "data_contratacao", nullable = false)
    var dataContratacao: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: StatusContratacao = StatusContratacao.ATIVA
)
