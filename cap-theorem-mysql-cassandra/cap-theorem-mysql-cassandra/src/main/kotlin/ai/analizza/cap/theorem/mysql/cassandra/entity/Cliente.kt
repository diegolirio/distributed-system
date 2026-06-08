package ai.analizza.cap.theorem.mysql.cassandra.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class SegmentoCliente { MEDICO, DENTISTA, FISIOTERAPEUTA, OUTRO }

@Entity
@Table(name = "cliente")
class Cliente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    val idCliente: Int = 0,

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    var cpf: String = "",

    @Column(name = "cnpj", unique = true, length = 14)
    var cnpj: String? = null,

    @Column(name = "nome", nullable = false, length = 150)
    var nome: String = "",

    @Column(name = "email", nullable = false, unique = true, length = 150)
    var email: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "segmento", nullable = false)
    var segmento: SegmentoCliente = SegmentoCliente.OUTRO,

    @Column(name = "criado_em", nullable = false, updatable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()
)
