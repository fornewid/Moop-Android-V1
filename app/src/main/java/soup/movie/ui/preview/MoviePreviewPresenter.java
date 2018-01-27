package soup.movie.ui.preview;

import io.reactivex.android.schedulers.AndroidSchedulers;
import soup.movie.Injection;
import soup.movie.data.MovieListRequest;

public class MoviePreviewPresenter implements MoviePreviewContract.Presenter {

    private MoviePreviewContract.View mView;
    private Injection mInjection;

    MoviePreviewPresenter(MoviePreviewContract.View view) {
        mView = view;
        mInjection = new Injection();
    }

    @Override
    public void bind() {
    }

    @Override
    public void unbind() {
    }

    @Override
    public void refresh() {
        mView.onClearList();
        loadMovieList();
    }

    @Override
    public void loadItems() {
        loadMovieList();
    }

    private void loadMovieList() {
        mInjection.getMovieRepository()
                .getMovieList(new MovieListRequest())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movieList -> {
                    mView.onRefreshDone();
                    mView.onListUpdated(movieList);
                });
    }
}
