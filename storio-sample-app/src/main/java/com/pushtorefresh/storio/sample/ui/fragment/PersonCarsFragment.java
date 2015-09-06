package com.pushtorefresh.storio.sample.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pushtorefresh.storio.sample.R;
import com.pushtorefresh.storio.sample.SampleApp;
import com.pushtorefresh.storio.sample.db.entities.Car;
import com.pushtorefresh.storio.sample.db.entities.Person;
import com.pushtorefresh.storio.sample.db.entities.Tweet;
import com.pushtorefresh.storio.sample.db.tables.PersonsTable;
import com.pushtorefresh.storio.sample.ui.DividerItemDecoration;
import com.pushtorefresh.storio.sample.ui.UiStateController;
import com.pushtorefresh.storio.sample.ui.adapter.PersonCarsAdapter;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.put.PutResults;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

import static com.pushtorefresh.storio.sample.ui.Toasts.safeShowShortToast;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class PersonCarsFragment extends BaseFragment {

    // In this sample app we use dependency injection (DI) to keep the code clean
    // Just remember that it's already configured instance of StorIOSQLite from DbModule
    @Inject
    StorIOSQLite storIOSQLite;

    UiStateController uiStateController;

    @InjectView(R.id.person_cars_recycler_view)
    RecyclerView recyclerView;

    PersonCarsAdapter personCarsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SampleApp.get(getActivity()).appComponent().inject(this);
        personCarsAdapter = new PersonCarsAdapter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person_cars, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(personCarsAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        uiStateController = new UiStateController.Builder()
                .withLoadingUi(view.findViewById(R.id.person_cars_loading_ui))
                .withErrorUi(view.findViewById(R.id.person_cars_error_ui))
                .withEmptyUi(view.findViewById(R.id.person_cars_empty_ui))
                .withContentUi(recyclerView)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadData();
    }

    void reloadData() {
        uiStateController.setUiStateLoading();

//        final Subscription subscription = storIOSQLite
//                .get()
//                .listOfObjects(Person.class)
//                .withQuery("select persons.name,")
//                .prepare()
//                .createObservable() // it will be subscribed to changes in tweets table!
//                .delay(1, SECONDS) // for better User Experience :) Actually, StorIO is so fast that we need to delay emissions (it's a joke, or not)
//                .observeOn(mainThread())
//                .subscribe(new Action1<List<Person>>() {
//                    @Override
//                    public void call(List<Person> persons) {
//                        // Remember: subscriber will automatically receive updates
//                        // Of tables from Query (tweets table in our case)
//                        // This makes your code really Reactive and nice!
//
//                        // We guarantee, that list of objects will never be null (also we use @NonNull/@Nullable)
//                        // So you just need to check if it's empty or not
//                        if (persons.isEmpty()) {
//                            uiStateController.setUiStateEmpty();
//                            personCarsAdapter.setPersons(null);
//                        } else {
//                            uiStateController.setUiStateContent();
//                            personCarsAdapter.setPersons(persons);
//                        }
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        // In cases when you are not sure that query will be successful
//                        // You can prevent crash of the application via error handler
//                        Timber.e(throwable, "reloadData()");
//                        uiStateController.setUiStateError();
//                        personCarsAdapter.setPersons(null);
//                    }
//                });
//
//        // Preventing memory leak (other Observables: Put, Delete emit result once so memory leak won't live long)
//        // Because rx.Observable from Get Operation is endless (it watches for changes of tables from query)
//        // You can easily create memory leak (in this case you'll leak the Fragment and all it's fields)
//        // So please, PLEASE manage your subscriptions
//        // We suggest same mechanism via storing all subscriptions that you want to unsubscribe
//        // In something like CompositeSubscription and unsubscribe them in appropriate moment of component lifecycle
//        unsubscribeOnStop(subscription);
    }

    // TODO add examples of person-cars
    @OnClick(R.id.person_cars_empty_ui_add_person_cars_button)
    void addPersonCars() {
        final List<Person> persons = new ArrayList<Person>();

        // TODO: 2 possibilities
        // 1) create person, add new cars to that persons -> save persons
        // 2) create cars and add to existing persons; save persons

        // 1)
        Person person = Person.newPerson("Jennifer");
        person.getCars().add(Car.newCar("BMW X3"));
        person.getCars().add(Car.newCar("Chevrolet Tahoe"));
        persons.add(person);

        person = Person.newPerson("Sam");
        person.getCars().add(Car.newCar("Maserati GranTurismo"));
        persons.add(person);

        // Looks/reads nice, isn't it?
        storIOSQLite
                .put()
                .objects(persons)
                .prepare()
                .createObservable()
                .observeOn(mainThread()) // Remember, all Observables in StorIO already subscribed on Schedulers.io(), you just need to set observeOn()
                .subscribe(new Observer<PutResults<Person>>() {
                    @Override
                    public void onError(Throwable e) {
                        safeShowShortToast(getActivity(), R.string.person_cars_add_error_toast);
                    }

                    @Override
                    public void onNext(PutResults<Person> putResults) {
                        // After successful Put Operation our subscriber in reloadData() will receive update!
                    }

                    @Override
                    public void onCompleted() {
                        // no impl required
                    }
                });
        ////////////////////////////////////////////////////////////////

//        persons.clear();
//
//        ////////////////////////////////////////////////////////////////
//        // 2)
//        persons.add(Person.newPerson("Michael"));
//        persons.add(Person.newPerson("Sam"));
//        persons.add(Person.newPerson("Betty"));
//
//        // a) now save the persons
//        storIOSQLite
//                .put()
//                .objects(persons)
//                .prepare()
//                .createObservable()
//                .observeOn(mainThread()) // Remember, all Observables in StorIO already subscribed on Schedulers.io(), you just need to set observeOn()
//                .subscribe(new Observer<PutResults<Person>>() {
//                    @Override
//                    public void onError(Throwable e) {
//                        safeShowShortToast(getActivity(), R.string.person_cars_add_error_toast);
//                    }
//
//                    @Override
//                    public void onNext(PutResults<Person> putResults) {
//                        // After successful Put Operation our subscriber in reloadData() will receive update!
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        // no impl required
//                    }
//                });
//
//        // b) now create the cars and save them
//        persons.get(0).getCars().add(Car.newCar("Toyota Yaris"));
//        persons.get(0).getCars().add(Car.newCar("VW Golf"));
//
//        persons.get(1).getCars().add(Car.newCar("Honda Accord"));
//        persons.get(1).getCars().add(Car.newCar("Cadillac De Ville Coupe"));
//        persons.get(1).getCars().add(Car.newCar("Austin Healey 3000 BJ8"));
//
//        persons.get(2).getCars().add(Car.newCar("Lotus Elise"));
//
//        tweets.add(Tweet.newTweet("artem_zin", "Checkout StorIO — modern API for SQLiteDatabase & ContentResolver"));
//        tweets.add(Tweet.newTweet("HackerNews", "It's revolution! Dolphins can write news on HackerNews with our new app!"));
//        tweets.add(Tweet.newTweet("AndroidDevReddit", "Awesome library — StorIO"));
//        tweets.add(Tweet.newTweet("Facebook", "Facebook community in Twitter is more popular than Facebook community in Facebook and Instagram!"));
//        tweets.add(Tweet.newTweet("Google", "Android be together not the same: AOSP, AOSP + Google Apps, Samsung Android"));
//        tweets.add(Tweet.newTweet("Reddit", "Now we can send funny gifs directly into your brain via Oculus Rift app!"));
//        tweets.add(Tweet.newTweet("ElonMusk", "Tesla Model S OTA update with Android Auto 5.2, fixes for memory leaks"));
//        tweets.add(Tweet.newTweet("AndroidWeekly", "Special issue #1: StorIO — forget about SQLiteDatabase, ContentResolver APIs, ORMs sucks!"));
//        tweets.add(Tweet.newTweet("Apple", "Yosemite update: fixes for Wifi issues, yosemite-wifi-patch#142"));
//
//        // Looks/reads nice, isn't it?
//        storIOSQLite
//                .put()
//                .objects(persons)
//                .prepare()
//                .createObservable()
//                .observeOn(mainThread()) // Remember, all Observables in StorIO already subscribed on Schedulers.io(), you just need to set observeOn()
//                .subscribe(new Observer<PutResults<Person>>() {
//                    @Override
//                    public void onError(Throwable e) {
//                        safeShowShortToast(getActivity(), R.string.person_cars_add_error_toast);
//                    }
//
//                    @Override
//                    public void onNext(PutResults<Person> putResults) {
//                        // After successful Put Operation our subscriber in reloadData() will receive update!
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        // no impl required
//                    }
//                });
    }
}
