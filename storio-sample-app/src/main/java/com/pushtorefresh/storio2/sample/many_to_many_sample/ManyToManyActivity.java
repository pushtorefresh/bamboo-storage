package com.pushtorefresh.storio2.sample.many_to_many_sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.pushtorefresh.storio2.sample.R;
import com.pushtorefresh.storio2.sample.SampleApp;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.Car;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.CarStorIOSQLiteDeleteResolver;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.CarTable;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.Person;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.PersonCarRelationTable;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.PersonTable;
import com.pushtorefresh.storio2.sample.ui.activity.BaseActivity;
import com.pushtorefresh.storio2.sqlite.Changes;
import com.pushtorefresh.storio2.sqlite.StorIOSQLite;
import com.pushtorefresh.storio2.sqlite.queries.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static java.util.Arrays.asList;

public class ManyToManyActivity extends BaseActivity implements PersonsAdapter.Callbacks {

    @Inject
    @NonNull
    StorIOSQLite storIOSQLite;

    @Bind(R.id.cars_recycler_view)
    @NonNull
    RecyclerView recyclerView;

    @NonNull
    private PersonsAdapter personsAdapter;

    @NonNull
    private static final List<String> CAR_MARKS = asList(
            "Volvo s60",
            "VW Golf",
            "Tesla Model X",
            "BMW X6",
            "Alfa Romeo 4c");

    @NonNull
    private final Random random = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_many_to_many);

        SampleApp.get(this).appComponent().inject(this);
        ButterKnife.bind(this);

        personsAdapter = new PersonsAdapter(LayoutInflater.from(this), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(personsAdapter);

        unsubscribeOnStop(subscribeToPersonsAndCars());
    }

    @NonNull
    Subscription subscribeToPersonsAndCars() {
        Set<String> tables = new HashSet<String>(3);
        tables.add(PersonTable.NAME);
        tables.add(CarTable.NAME);
        tables.add(PersonCarRelationTable.TABLE);
        return Observable.merge(
                storIOSQLite.observeChangesInTables(tables),
                Observable.just((Changes) null)
        )
                .map(new Func1<Changes, List<Person>>() {
                    @Override
                    public List<Person> call(Changes changes) {
                        return storIOSQLite.get()
                                .listOfObjects(Person.class)
                                .withQuery(Query.builder().table(PersonTable.NAME).build())
                                .prepare()
                                .executeAsBlocking();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Person>>() {
                    @Override
                    public void call(List<Person> persons) {
                        if(persons.isEmpty()) {
                            addPersons();
                        } else {
                            personsAdapter.setPersons(persons);
                        }
                    }
                });
    }

    void addPersons() {
        final List<Person> persons = new ArrayList<Person>();

        persons.add(new Person(null, "artem_zin", asList(nextRandCar(), nextRandCar())));
        persons.add(new Person(null, "elonmusk", asList(nextRandCar(), nextRandCar())));

        storIOSQLite
                .put()
                .objects(persons)
                .prepare()
                .executeAsBlocking();
    }

    @Override
    public void onAddCarClick(@NonNull Person person) {
        final List<Car> cars = person.cars();
        final List<Car> newCarList = new ArrayList<Car>();
        if (cars != null) {
            newCarList.addAll(cars);
        }
        newCarList.add(nextRandCar());
        storIOSQLite.put()
                .object(new Person(person.id(), person.name(), newCarList))
                .prepare()
                .asRxSingle()
                .subscribe();
    }

    @Override
    public void onRemoveCarClick(@NonNull final Person person) {
        final List<Car> cars = person.cars();
        if (cars != null && !cars.isEmpty()) {
            Completable.fromAction(new Action0() {
                @Override
                public void call() {
                    storIOSQLite.lowLevel().beginTransaction();
                    try {
                        final Car carToRemove = cars.get(cars.size() - 1);

                        storIOSQLite.delete()
                                .object(carToRemove)
                                .withDeleteResolver(new CarStorIOSQLiteDeleteResolver())
                                .prepare()
                                .executeAsBlocking();

                        storIOSQLite.put()
                                .object(new Person(person.id(), person.name(), cars.subList(0, cars.size() - 1)))
                                .prepare()
                                .executeAsBlocking();

                        storIOSQLite.lowLevel().setTransactionSuccessful();
                    } finally {
                        storIOSQLite.lowLevel().endTransaction();
                    }
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    @NonNull
    private Car nextRandCar() {
        final String mark = CAR_MARKS.get(random.nextInt(CAR_MARKS.size()));
        return new Car(mark);
    }
}
