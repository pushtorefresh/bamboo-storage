package com.pushtorefresh.storio.sample;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sample.db.DbModule;
import com.pushtorefresh.storio.sample.provider.SampleContentProvider;
import com.pushtorefresh.storio.sample.ui.fragment.PersonCarsFragment;
import com.pushtorefresh.storio.sample.ui.fragment.TweetsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                AppModule.class,
                DbModule.class
        }
)
public interface AppComponent {
    void inject(@NonNull TweetsFragment fragment);

    void inject(@NonNull PersonCarsFragment fragment);

    void inject(@NonNull SampleContentProvider sampleContentProvider);
}
