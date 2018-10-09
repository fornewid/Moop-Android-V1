package soup.movie.data.source.remote

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import soup.movie.data.model.Movie
import soup.movie.data.model.Theater
import soup.movie.data.model.Timetable
import soup.movie.data.model.Version
import soup.movie.data.model.response.CodeResponse
import soup.movie.data.model.response.MovieListResponse
import soup.movie.data.source.MoopDataSource
import soup.movie.data.util.toAnObservable

class RemoteMoopDataSource(private val moopApiService: MoopApiService) : MoopDataSource {

    override fun getNowList(): Observable<MovieListResponse> =
            moopApiService.getNowMovieList()

    override fun getPlanList(): Observable<MovieListResponse> =
            moopApiService.getPlanMovieList()

    override fun getCodeList(): Observable<CodeResponse> =
            moopApiService.getCodeList()

    override fun getTimetable(theater: Theater, movie: Movie): Observable<Timetable> =
            moopApiService.getTimetable(theater.code, movie.id)
                    .map { it.timetable }

    override fun getVersion(pkgName: String, defaultVersion: String): Observable<Version> =
            Version(defaultVersion)
                    .toAnObservable()
                    .subscribeOn(Schedulers.io())
}