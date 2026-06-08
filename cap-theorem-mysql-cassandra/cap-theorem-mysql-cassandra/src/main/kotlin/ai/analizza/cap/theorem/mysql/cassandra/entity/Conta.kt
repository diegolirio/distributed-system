package ai.analizza.cap.theorem.mysql.cassandra.entity

import jakarta.persistence.*
import java.math.BigDecimal

enum class TipoConta { PF, PJ }
enum class StatusConta { ATIVA, BLOQUEADA, ENCERRADA }

@Entity
@Table(name = "conta")
class Conta(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta")
    val idConta: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    var cliente: Cliente = Cliente(),

    @Column(name = "numero", nullable = false, unique = true, length = 20)
    var numero: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false)
    var tipoConta: TipoConta = TipoConta.PF,

    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    var saldo: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: StatusConta = StatusConta.ATIVA
)
