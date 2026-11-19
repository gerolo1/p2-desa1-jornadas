package ar.jornadas.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import retrofit2.http.GET
import kotlinx.coroutines.flow.Flow

data class FichaRemota(val id: String, val title: String, val track: String, val day: Int,
    val time: String, val room: String, val speaker: String, val summary: String)

interface ProgramaApi {
    @GET("programa") suspend fun consultar(): List<FichaRemota>
}

@Entity(tableName = "charlas")
data class Charla(@PrimaryKey val id: String, val titulo: String, val tema: String, val dia: Int,
    val hora: String, val sala: String, val ponente: String, val resumen: String,
    val favorita: Boolean = false)

data class Contenido(val id: String, val titulo: String, val tema: String, val dia: Int,
    val hora: String, val sala: String, val ponente: String, val resumen: String)

@Entity(tableName = "edicion")
data class Edicion(@PrimaryKey val id: Int = 0)

@Dao
abstract class CarteleraDao {
    @Query("SELECT * FROM charlas ORDER BY dia, hora, sala")
    abstract fun observar(): Flow<List<Charla>>

    @Query("SELECT EXISTS(SELECT 1 FROM edicion)")
    abstract fun hayEdicion(): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun guardarNuevas(fichas: List<Charla>)

    @Update(entity = Charla::class)
    abstract suspend fun actualizar(contenidos: List<Contenido>)

    @Query("DELETE FROM charlas WHERE id NOT IN (:ids)")
    abstract suspend fun retirarAusentes(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun registrar(edicion: Edicion)

    @Transaction
    open suspend fun reemplazar(fichas: List<Charla>) {
        retirarAusentes(fichas.map { it.id })
        guardarNuevas(fichas)
        actualizar(fichas.map { Contenido(it.id, it.titulo, it.tema, it.dia, it.hora, it.sala, it.ponente, it.resumen) })
        registrar(Edicion())
    }

    @Query("UPDATE charlas SET favorita = NOT favorita WHERE id = :id")
    abstract suspend fun alternar(id: String)
}

@Database(entities = [Charla::class, Edicion::class], version = 2, exportSchema = false)
abstract class Archivo : RoomDatabase() {
    abstract fun cartelera(): CarteleraDao
}

val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `edicion` (`id` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("INSERT INTO edicion (id) SELECT 0 WHERE EXISTS (SELECT 1 FROM charlas)")
    }
}
