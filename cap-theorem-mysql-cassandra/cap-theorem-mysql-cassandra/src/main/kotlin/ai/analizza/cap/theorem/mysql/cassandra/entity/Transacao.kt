package ai.analizza.cap.theorem.mysql.cassandra.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transacao")
class Transacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transacao")
    val idTransacao: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta", nullable = false)
    var conta: Conta = Conta(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_produto", nullable = false)
    var produto: Produto = Produto(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo", nullable = false)
    var tipoTransacao: TipoTransacao = TipoTransacao(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contratacao")
    var contratacao: Contratacao? = null,

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    var valor: BigDecimal = BigDecimal.ZERO,

    @Column(name = "data_hora", nullable = false)
    var dataHora: LocalDateTime = LocalDateTime.now(),

    @Column(name = "id_idempotencia", nullable = false, unique = true, length = 36)
    var idIdempotencia: String = ""
)
