package soup.movie.data.api.response

import kotlinx.serialization.Serializable

@Serializable
data class MetascoreInfoResponse(
    val star: String? = null
)
