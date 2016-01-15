package com.pushtorefresh.storio.contentresolver.design;

import android.net.Uri;

import com.pushtorefresh.storio.contentresolver.operations.delete.DeleteResult;
import com.pushtorefresh.storio.contentresolver.operations.delete.DeleteResults;
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Single;

import static org.mockito.Mockito.mock;

public class DeleteOperationDesignTest extends OperationDesignTest {

    @Test
    public void deleteByQueryBlocking() {
        final DeleteQuery deleteQuery = DeleteQuery.builder()
                .uri(mock(Uri.class))
                .where("some_field = ?")
                .whereArgs("someValue")
                .build();

        DeleteResult deleteResult = storIOContentResolver()
                .delete()
                .byQuery(deleteQuery)
                .prepare()
                .executeAsBlocking();
    }

    @Test
    public void deleteByQueryObservable() {
        final DeleteQuery deleteQuery = DeleteQuery.builder()
                .uri(mock(Uri.class))
                .where("some_field = ?")
                .whereArgs("someValue")
                .build();

        Observable<DeleteResult> deleteResultObservable = storIOContentResolver()
                .delete()
                .byQuery(deleteQuery)
                .prepare()
                .createObservable();
    }

    @Test
    public void deleteByQuerySingle() {
        final DeleteQuery deleteQuery = DeleteQuery.builder()
                .uri(mock(Uri.class))
                .where("some_field = ?")
                .whereArgs("someValue")
                .build();

        Single<DeleteResult> deleteResultSingle = storIOContentResolver()
                .delete()
                .byQuery(deleteQuery)
                .prepare()
                .asRxSingle();
    }

    @Test
    public void deleteObjectsBlocking() {
        final List<Article> articles = new ArrayList<Article>();

        DeleteResults<Article> deleteResults = storIOContentResolver()
                .delete()
                .objects(articles)
                .withDeleteResolver(ArticleMeta.DELETE_RESOLVER)
                .prepare()
                .executeAsBlocking();
    }

    @Test
    public void deleteObjectsObservable() {
        final List<Article> articles = new ArrayList<Article>();

        Observable<DeleteResults<Article>> deleteResultsObservable = storIOContentResolver()
                .delete()
                .objects(articles)
                .withDeleteResolver(ArticleMeta.DELETE_RESOLVER)
                .prepare()
                .createObservable();
    }

    @Test
    public void deleteObjectsSingle() {
        final List<Article> articles = new ArrayList<Article>();

        Single<DeleteResults<Article>> deleteResultsSingle = storIOContentResolver()
                .delete()
                .objects(articles)
                .withDeleteResolver(ArticleMeta.DELETE_RESOLVER)
                .prepare()
                .asRxSingle();
    }


    @Test
    public void deleteObjectBlocking() {
        Article article = mock(Article.class);

        DeleteResult deleteResult = storIOContentResolver()
                .delete()
                .object(article)
                .withDeleteResolver(ArticleMeta.DELETE_RESOLVER)
                .prepare()
                .executeAsBlocking();
    }

    @Test
    public void deleteObjectObservable() {
        Article article = mock(Article.class);

        Observable<DeleteResult> deleteResultObservable = storIOContentResolver()
                .delete()
                .object(article)
                .withDeleteResolver(ArticleMeta.DELETE_RESOLVER)
                .prepare()
                .createObservable();
    }

    @Test
    public void deleteObjectSingle() {
        Article article = mock(Article.class);

        Single<DeleteResult> deleteResultSingle = storIOContentResolver()
                .delete()
                .object(article)
                .withDeleteResolver(ArticleMeta.DELETE_RESOLVER)
                .prepare()
                .asRxSingle();
    }
}
