package org.fossasia.openevent.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.fossasia.openevent.data.Track;
import org.fossasia.openevent.dbutils.RealmDataRepository;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.realm.RealmResults;
import timber.log.Timber;

public class TracksFragmentViewModel extends ViewModel {

    private MutableLiveData<List<Track>> tracksList;
    private MutableLiveData<List<Track>> filteredTracksList;

    private RealmDataRepository realmRepo;
    private RealmResults<Track> realmResults;

    private String searchText = "";

    public TracksFragmentViewModel() {
        realmRepo = RealmDataRepository.getDefaultInstance();
    }

    public LiveData<List<Track>> getTracks(String searchText) {
        setSearchText(searchText);
        if (filteredTracksList == null)
            filteredTracksList = new MutableLiveData<>();
        if (tracksList == null) {
            tracksList = new MutableLiveData<>();
            realmResults = realmRepo.getTracks();
            realmResults.addChangeListener((tracks, orderedCollectionChangeSet) -> {
                tracksList.setValue(tracks);
                getFilteredTracks();
            });
        } else getFilteredTracks();

        return filteredTracksList;
    }

    private void getFilteredTracks() {

        final String query = searchText.toLowerCase(Locale.getDefault());

        Observable.fromIterable(tracksList.getValue())
                .filter(track -> track.getName()
                        .toLowerCase(Locale.getDefault())
                        .contains(query))
                .toList().subscribe(trackList -> {
            filteredTracksList.setValue(trackList);

            Timber.d("Filtering done total results %d", trackList.size());

            if (trackList.isEmpty()) {
                Timber.e("No results published. There is an error in query. Check " + getClass().getName() + " filter!");
            }
        });
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    protected void onCleared() {
        realmResults.removeAllChangeListeners();
        super.onCleared();
    }

}
