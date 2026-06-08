package ai.analizza.cap.theorem.mysql.cassandra.entity

import jakarta.persistence.*

@Entity
@Table(name = "tipo_transacao")
class TipoTransacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    val idTipo: Int = 0,

    @Column(name = "descricao", nullable = false, unique = true, length = 60)
    var descricao: String = "",

    @Column(name = "sinal", nullable = false)
    var sinal: Int = 1
)
