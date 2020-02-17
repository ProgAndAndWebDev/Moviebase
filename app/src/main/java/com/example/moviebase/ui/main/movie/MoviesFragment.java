package com.example.moviebase.ui.main.movie;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.moviebase.R;
import com.example.moviebase.databinding.FragmentMoviesBinding;
import com.example.moviebase.ui.base.BaseFragment;
import com.example.moviebase.utils.GridSpacingItemDecorationUtils;
import com.example.moviebase.data.model.Movie;
import com.example.moviebase.utils.RecyclerViewScrollListenerUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MoviesFragment extends BaseFragment<FragmentMoviesBinding,MoviesViewModel> {

    private MoviesViewModel moviesViewModel;
    private String category;
    private FragmentMoviesBinding moviesBinding;
    private GridLayoutManager gridLayoutManager;
    private int totalMoviesPages;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    MoviesAdapter moviesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        category = getArguments().getString("category"); // request API to get all movies in this Category

        moviesAdapter.setOnMovieItemClickListener(moviesViewModel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        moviesBinding = getViewDataBinding();
        moviesBinding.progressBar.setVisibility(View.VISIBLE);

        initMoviesRecyclerView(2 ,25);

        getMoviesDataApiCall(1);
        observeMoviesListData();
        observeTotalMoviesPages();
        setupEndlessRecyclerView();
    }

    @Override
    public MoviesViewModel getViewModel() {
        moviesViewModel = new ViewModelProvider(this , viewModelFactory).get(MoviesViewModel.class);
        return moviesViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_movies;
    }

    private void initMoviesRecyclerView(int spanCount , int spacing)
    {
        gridLayoutManager = new GridLayoutManager(getActivity() , spanCount);
        moviesBinding.moviesRv.setLayoutManager(gridLayoutManager);
        moviesBinding.moviesRv.setHasFixedSize(true);
        // set Animation to all children (items) of this Layout
        int animID = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(context, animID);
        moviesBinding.moviesRv.setLayoutAnimation(animation);
        // equal spaces between grid items
        boolean includeEdge = true;
        moviesBinding.moviesRv.addItemDecoration(new GridSpacingItemDecorationUtils(spanCount, spacing, includeEdge));
        moviesBinding.moviesRv.setAdapter(moviesAdapter);
    }

    private void observeTotalMoviesPages(){
        totalMoviesPages = 1;
        moviesViewModel.getTotalMoviesPages().observe(getViewLifecycleOwner(), new Observer< Integer >() {
            @Override
            public void onChanged(Integer pages) {
                totalMoviesPages = pages;
            }
        });
    }

    private void setupEndlessRecyclerView(){
        RecyclerViewScrollListenerUtils rvScrollListenerUtils = new RecyclerViewScrollListenerUtils(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (page <= totalMoviesPages){
                    moviesViewModel.getMoviesListApiCall(category,page);
                }
            }
        };
        moviesBinding.moviesRv.addOnScrollListener(rvScrollListenerUtils);
    }

    private void observeMoviesListData(){
        moviesViewModel.getMoviesList().observe(getViewLifecycleOwner() , new Observer< List< Movie > >() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (movies != null){
                    updateMoviesList(movies);
                }else{
                    Log.i("Here" , "No Data Changed");
                }
            }
        });
    }

    private void updateMoviesList(List<Movie> movies){
        moviesBinding.progressBar.setVisibility(View.INVISIBLE);
        moviesAdapter.addAll((ArrayList< Movie >) movies);
    }

    private void getMoviesDataApiCall(int page){
        moviesViewModel.getMoviesListApiCall(category,page);
    }

}

