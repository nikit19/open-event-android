package org.fossasia.openevent.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import org.fossasia.openevent.data.Track;
import org.fossasia.openevent.dbutils.LiveRealmData;
import org.fossasia.openevent.dbutils.RealmDataRepository;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import timber.log.Timber;

public class TracksFragmentViewModel extends ViewModel {

    private LiveData<List<Track>> tracksList;
    private LiveData<List<Track>> filteredTracksList;

    private RealmDataRepository realmRepo;

    private String searchText = "";

    public TracksFragmentViewModel() {
        realmRepo = RealmDataRepository.getDefaultInstance();
        tracksList = new MutableLiveData<>();
        filteredTracksList = new MutableLiveData<>();
        subscribeToLocations();

    }

    public LiveData<List<Track>> getTracks(String searchText) {
        setSearchText(searchText);
        getFilteredTracks();

        return filteredTracksList;
    }

    private void subscribeToLocations() {
        LiveRealmData<Track> tracksLiveRealmData = RealmDataRepository.asLiveData(realmRepo.getTracks());
        tracksList = Transformations.map(tracksLiveRealmData, input -> input);
    }

    private void getFilteredTracks() {

        final String query = searchText.toLowerCase(Locale.getDefault());

        if (tracksList.getValue() != null) {
            Observable.fromIterable(tracksList.getValue())
                    .filter(track -> track.getName()
                            .toLowerCase(Locale.getDefault())
                            .contains(query))
                    .toList().subscribe(trackList -> {
                filteredTracksList = Transformations.map(tracksList, input -> input);

                Timber.d("Filtering done total results %d", trackList.size());

                if (trackList.isEmpty()) {
                    Timber.e("No results published. There is an error in query. Check " + getClass().getName() + " filter!");
                }
            });
        }
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

}
