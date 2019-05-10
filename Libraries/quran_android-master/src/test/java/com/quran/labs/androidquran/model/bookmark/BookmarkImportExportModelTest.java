package com.quran.labs.androidquran.model.bookmark;

import android.content.Context;

import com.quran.labs.androidquran.dao.bookmark.BookmarkData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import io.reactivex.observers.TestObserver;
import okio.Buffer;

public class BookmarkImportExportModelTest {
  private static final String TAGS_JSON =
      "{\"bookmarks\":[],\"tags\":[{\"id\":1,\"name\":\"First\"}," +
          "{\"id\":2,\"name\":\"Second\"},{\"id\":3,\"name\":\"Third\"}]}";

  @Mock Context context;
  @Mock BookmarkModel bookmarkModel;
  private BookmarkImportExportModel bookmarkImportExportModel;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(BookmarkImportExportModelTest.this);
    bookmarkImportExportModel = new BookmarkImportExportModel(
        context, new BookmarkJsonModel(), bookmarkModel);
  }

  @Test
  public void testReadBookmarks() {
    Buffer buffer = new Buffer().writeUtf8(TAGS_JSON);
    TestObserver<BookmarkData> testObserver = new TestObserver<>();
    bookmarkImportExportModel.readBookmarks(buffer)
        .subscribe(testObserver);
    testObserver.awaitTerminalEvent();
    testObserver.assertValueCount(1);
    testObserver.assertNoErrors();
  }

  @Test
  public void testReadInvalidBookmarks() {
    TestObserver<BookmarkData> testObserver = new TestObserver<>();

    Buffer source = new Buffer();
    source.writeUtf8(")");

    bookmarkImportExportModel.readBookmarks(source)
        .subscribe(testObserver);
    testObserver.awaitTerminalEvent();
    testObserver.assertValueCount(0);
    testObserver.assertError(IOException.class);
  }
}
