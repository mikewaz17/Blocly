package io.bloc.android.blocly.api.model.database.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import io.bloc.android.blocly.api.model.RssItem;

/**
 * Created by Mike on 8/21/2015.
 */
public class RssItemTable extends Table {

    public static class Builder implements Table.Builder {

        ContentValues values = new ContentValues();

        public Builder setLink(String link) {
            values.put(COLUMN_LINK, link);
            return this;
        }

        public Builder setTitle(String title) {
            values.put(COLUMN_TITLE, title);
            return this;
        }

        public Builder setDescription(String description) {
            values.put(COLUMN_DESCRIPTION, description);
            return this;
        }

        public Builder setGUID(String guid) {
            values.put(COLUMN_GUID, guid);
            return this;
        }

        public Builder setPubDate(long pubDate) {
            values.put(COLUMN_PUB_DATE, pubDate);
            return this;
        }

        public Builder setEnclosure(String enclosure) {
            values.put(COLUMN_ENCLOSURE, enclosure);
            return this;
        }

        public Builder setMIMEType(String mimeType) {
            values.put(COLUMN_MIME_TYPE, mimeType);
            return this;
        }

        public Builder setRSSFeed(long rssFeed) {
            values.put(COLUMN_RSS_FEED, rssFeed);
            return this;
        }

        @Override
        public long insert(SQLiteDatabase writableDB) {
            return writableDB.insert(RssItemTable.NAME, null, values);
        }
    }

    public static String getLink(Cursor cursor) {
        return getString(cursor, COLUMN_LINK);
    }

    public static String getTitle(Cursor cursor) {
        return getString(cursor, COLUMN_TITLE);
    }

    public static String getDescription(Cursor cursor) {
        return getString(cursor, COLUMN_DESCRIPTION);
    }

    public static String getGUID(Cursor cursor) {
        return getString(cursor, COLUMN_GUID);
    }

    public static long getRssFeedId(Cursor cursor) {
        return getLong(cursor, COLUMN_RSS_FEED);
    }

    public static long getPubDate(Cursor cursor) {
        return getLong(cursor, COLUMN_PUB_DATE);
    }

    public static String getEnclosure(Cursor cursor) {
        return getString(cursor, COLUMN_ENCLOSURE);
    }

    public static boolean getFavorite(Cursor cursor) {
        return getBoolean(cursor, COLUMN_FAVORITE);
    }

    public static boolean getArchived(Cursor cursor) {
        return getBoolean(cursor, COLUMN_ARCHIVED);
    }
    //#55 Added the methods to recover the data inside to RssItemTable

    private static final String NAME = "rss_items";

    private static final String COLUMN_LINK = "link";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_GUID = "guid";
    private static final String COLUMN_PUB_DATE = "pub_date";
    private static final String COLUMN_ENCLOSURE = "enclosure";
    private static final String COLUMN_MIME_TYPE = "mime_type";
    private static final String COLUMN_RSS_FEED = "rss_feed";
    private static final String COLUMN_FAVORITE = "is_favorite";
    private static final String COLUMN_ARCHIVED = "is_archived";

    @Override
    public String getName() {
        return "rss_items";
    }

    @Override
    public String getCreateStatement() {
        return "CREATE TABLE " + getName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_LINK + " TEXT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_GUID + " TEXT,"
                + COLUMN_PUB_DATE + " INTEGER,"
                + COLUMN_ENCLOSURE + " TEXT,"
                + COLUMN_MIME_TYPE + " TEXT,"
                + COLUMN_RSS_FEED + " INTEGER,"
                + COLUMN_FAVORITE + " INTEGER DEFAULT 0,"
                + COLUMN_ARCHIVED + " INTEGER DEFAULT 0)";
    }
    //#52 Established the RssItemTable along with it's return types.
    /*#54 Added the Builder class into RssItemTable while setting the title, description, link,
     *guid, pub date, enclosure, mime type and RSS feed.
     */

    public ArrayList<RssItem> getAllArchivedRssItems(SQLiteDatabase readableDatabase) {

        // You should change the name of this ArrayList ************************
        ArrayList<RssItem> archivedRssItems = new ArrayList<RssItem>();

        // You NEED to change this SQLite Query ******************************************

        String sqliteQuery = "SELECT * FROM " + getName() + " WHERE " + COLUMN_ARCHIVED + " = 1;";
        Log.d("MikeDB", "All archived items query: " + sqliteQuery);

        Cursor cursor = readableDatabase.rawQuery(sqliteQuery, null);

        if (cursor.moveToFirst()) {

            do {
                String guid = cursor.getString(cursor.getColumnIndex(COLUMN_GUID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndex(COLUMN_LINK));

                Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                Long datePublished = cursor.getLong(cursor.getColumnIndex(COLUMN_PUB_DATE));

                int isFavoriteInt = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE));
                boolean isFavoriteBool = false;

                if (isFavoriteInt == 1) {
                    isFavoriteBool = true;
                }

                int isArchivedInt = cursor.getInt(cursor.getColumnIndex(COLUMN_ARCHIVED));
                boolean isArchivedBool = false;

                if (isArchivedInt == 1) {
                    isArchivedBool = true;
                }

                RssItem rssItem = new RssItem(guid, title, desc,
                        url, url, id, datePublished,
                        isFavoriteBool, isArchivedBool);

                archivedRssItems.add(rssItem);

            } while(cursor.moveToNext());
        }
        cursor.close();

        // This needs to match the name of the array list you created at the beginning of the method
        return archivedRssItems;
    }

    public ArrayList<RssItem> getAllArchivedRssItemsForFeed(SQLiteDatabase readableDatabase) {
        ArrayList<RssItem> archivedRssItemsForFeed = new ArrayList<RssItem>();
        String sqliteQuery = "SELECT * FROM " + getName() + " WHERE " + COLUMN_RSS_FEED + " = 1;";
        Log.d("MikeDB", "All archived items for feed query: " + sqliteQuery);

        Cursor cursor = readableDatabase.rawQuery(sqliteQuery, null);

        if (cursor.moveToFirst()) {

            do {
                String guid = cursor.getString(cursor.getColumnIndex(COLUMN_GUID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndex(COLUMN_LINK));

                Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                Long datePublished = cursor.getLong(cursor.getColumnIndex(COLUMN_PUB_DATE));

                int isFavoriteInt = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE));
                boolean isFavoriteBool = false;

                if (isFavoriteInt == 1) {
                    isFavoriteBool = true;
                }

                int isArchivedInt = cursor.getInt(cursor.getColumnIndex(COLUMN_ARCHIVED));
                boolean isArchivedBool = false;

                if (isArchivedInt == 1) {
                    isArchivedBool = true;
                }

                RssItem rssItem = new RssItem(guid, title, desc,
                        url, url, id, datePublished,
                        isFavoriteBool, isArchivedBool);

                archivedRssItemsForFeed.add(rssItem);

            } while(cursor.moveToNext());
        }
        cursor.close();

        return archivedRssItemsForFeed;


    }

    public ArrayList<RssItem> getAllFavoritedRssItems(SQLiteDatabase readableDatabase) {
        ArrayList<RssItem> favoritedRssItems = new ArrayList<RssItem>();
        String sqliteQuery = "SELECT * FROM " + getName() + " WHERE " + COLUMN_FAVORITE + " = 1;";
        Log.d("MikeDB", "All favorited items query: " + sqliteQuery);

        Cursor cursor = readableDatabase.rawQuery(sqliteQuery, null);

        if (cursor.moveToFirst()) {

            do {
                String guid = cursor.getString(cursor.getColumnIndex(COLUMN_GUID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndex(COLUMN_LINK));

                Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                Long datePublished = cursor.getLong(cursor.getColumnIndex(COLUMN_PUB_DATE));

                int isFavoriteInt = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE));
                boolean isFavoriteBool = false;

                if (isFavoriteInt == 1) {
                    isFavoriteBool = true;
                }

                int isArchivedInt = cursor.getInt(cursor.getColumnIndex(COLUMN_ARCHIVED));
                boolean isArchivedBool = false;

                if (isArchivedInt == 1) {
                    isArchivedBool = true;
                }

                RssItem rssItem = new RssItem(guid, title, desc,
                        url, url, id, datePublished,
                        isFavoriteBool, isArchivedBool);

                favoritedRssItems.add(rssItem);

            } while(cursor.moveToNext());
        }
        cursor.close();

        return favoritedRssItems;

    }

    public ArrayList<RssItem> getAllFavoritedRssItemsForFeed(SQLiteDatabase readableDatabase) {
        ArrayList<RssItem> favoritedRssItemsForFeed = new ArrayList<RssItem>();
        String sqliteQuery = "SELECT * FROM " + getName() + " WHERE " + COLUMN_RSS_FEED + " = 1;";
        Log.d("MikeDB", "All favorited items for feed query: " + sqliteQuery);

        Cursor cursor = readableDatabase.rawQuery(sqliteQuery, null);

        if (cursor.moveToFirst()) {

            do {
                String guid = cursor.getString(cursor.getColumnIndex(COLUMN_GUID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndex(COLUMN_LINK));

                Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                Long datePublished = cursor.getLong(cursor.getColumnIndex(COLUMN_PUB_DATE));

                int isFavoriteInt = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE));
                boolean isFavoriteBool = false;

                if (isFavoriteInt == 1) {
                    isFavoriteBool = true;
                }

                int isArchivedInt = cursor.getInt(cursor.getColumnIndex(COLUMN_ARCHIVED));
                boolean isArchivedBool = false;

                if (isArchivedInt == 1) {
                    isArchivedBool = true;
                }

                RssItem rssItem = new RssItem(guid, title, desc,
                        url, url, id, datePublished,
                        isFavoriteBool, isArchivedBool);

                favoritedRssItemsForFeed.add(rssItem);

            } while(cursor.moveToNext());
        }
        cursor.close();

        return favoritedRssItemsForFeed;

    }

    public ArrayList<RssItem> getAllRssItemsForFeed(SQLiteDatabase readableDatabase) {
        ArrayList<RssItem> RssItemsForFeed = new ArrayList<RssItem>();
        String sqliteQuery = "SELECT * FROM " + getName() + " WHERE " + COLUMN_RSS_FEED + " = 1;";
        Log.d("MikeDB", "All items for query: " + sqliteQuery);

        Cursor cursor = readableDatabase.rawQuery(sqliteQuery, null);

        if (cursor.moveToFirst()) {

            do {
                String guid = cursor.getString(cursor.getColumnIndex(COLUMN_GUID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                String desc = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                String url = cursor.getString(cursor.getColumnIndex(COLUMN_LINK));

                Long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                Long datePublished = cursor.getLong(cursor.getColumnIndex(COLUMN_PUB_DATE));

                int isFavoriteInt = cursor.getInt(cursor.getColumnIndex(COLUMN_FAVORITE));
                boolean isFavoriteBool = false;

                if (isFavoriteInt == 1) {
                    isFavoriteBool = true;
                }

                int isArchivedInt = cursor.getInt(cursor.getColumnIndex(COLUMN_ARCHIVED));
                boolean isArchivedBool = false;

                if (isArchivedInt == 1) {
                    isArchivedBool = true;
                }

                RssItem rssItem = new RssItem(guid, title, desc,
                        url, url, id, datePublished,
                        isFavoriteBool, isArchivedBool);

                RssItemsForFeed.add(rssItem);

            } while(cursor.moveToNext());
        }
        cursor.close();

        return RssItemsForFeed;

    }
}
