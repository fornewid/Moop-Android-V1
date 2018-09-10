package soup.movie.data.model.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import soup.movie.data.model.Movie

@Entity(tableName = "cached_movie_list")
data class CachedMovieList(
        @PrimaryKey
        val type: String,
        val lastUpdateTime: Long,
        val list: List<Movie>) {

    fun isUpToDate(): Boolean = System.currentTimeMillis() - lastUpdateTime < STALE_MS

    companion object {

        const val TYPE_NOW = "type_now"
        const val TYPE_PLAN = "type_plan"

        private const val STALE_MS = (60 * 60 * 1000).toLong() // 1 hour
    }
}
