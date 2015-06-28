package de.nenick.quacc.database.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import de.nenick.quacc.database.BuildConfig;
import de.nenick.quacc.database.provider.account.AccountColumns;
import de.nenick.quacc.database.provider.accounting.AccountingColumns;
import de.nenick.quacc.database.provider.accountingtemplate.AccountingTemplateColumns;
import de.nenick.quacc.database.provider.category.CategoryColumns;
import de.nenick.quacc.database.provider.interval.IntervalColumns;
import de.nenick.quacc.database.provider.intervalaccounting.IntervalAccountingColumns;
import de.nenick.quacc.database.provider.templatematching.TemplateMatchingColumns;

public class QuAccSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = QuAccSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "quacc.db";
    private static final int DATABASE_VERSION = 1;
    private static QuAccSQLiteOpenHelper sInstance;
    private final Context mContext;
    private final QuAccSQLiteOpenHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_ACCOUNT = "CREATE TABLE IF NOT EXISTS "
            + AccountColumns.TABLE_NAME + " ( "
            + AccountColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AccountColumns.NAME + " TEXT NOT NULL, "
            + AccountColumns.INITIALVALUE + " INTEGER NOT NULL "
            + ", CONSTRAINT unique_name UNIQUE (account__name) ON CONFLICT FAIL"
            + " );";

    public static final String SQL_CREATE_TABLE_ACCOUNTING = "CREATE TABLE IF NOT EXISTS "
            + AccountingColumns.TABLE_NAME + " ( "
            + AccountingColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AccountingColumns.ACCOUNT_ID + " INTEGER NOT NULL, "
            + AccountingColumns.CATEGORY_ID + " INTEGER NOT NULL, "
            + AccountingColumns.COMMENT + " TEXT, "
            + AccountingColumns.INTERVAL + " TEXT NOT NULL, "
            + AccountingColumns.DATE + " INTEGER NOT NULL, "
            + AccountingColumns.TYPE + " TEXT NOT NULL, "
            + AccountingColumns.VALUE + " INTEGER NOT NULL "
            + ", CONSTRAINT fk_account_id FOREIGN KEY (" + AccountingColumns.ACCOUNT_ID + ") REFERENCES account (_id) ON DELETE CASCADE"
            + ", CONSTRAINT fk_category_id FOREIGN KEY (" + AccountingColumns.CATEGORY_ID + ") REFERENCES category (_id) ON DELETE CASCADE"
            + ", CONSTRAINT empty_type CHECK(accounting__type <> '') ON CONFLICT FAIL"
            + ", CONSTRAINT empty_interval CHECK(accounting__interval <> '') ON CONFLICT FAIL"
            + ", CONSTRAINT given_value CHECK(accounting__value > 0) ON CONFLICT FAIL"
            + " );";

    public static final String SQL_CREATE_TABLE_ACCOUNTING_TEMPLATE = "CREATE TABLE IF NOT EXISTS "
            + AccountingTemplateColumns.TABLE_NAME + " ( "
            + AccountingTemplateColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AccountingTemplateColumns.ACCOUNT_ID + " INTEGER NOT NULL, "
            + AccountingTemplateColumns.CATEGORY_ID + " INTEGER NOT NULL, "
            + AccountingTemplateColumns.COMMENT + " TEXT NOT NULL, "
            + AccountingTemplateColumns.INTERVAL + " TEXT NOT NULL, "
            + AccountingTemplateColumns.TYPE + " TEXT NOT NULL "
            + ", CONSTRAINT fk_account_id FOREIGN KEY (" + AccountingTemplateColumns.ACCOUNT_ID + ") REFERENCES account (_id) ON DELETE CASCADE"
            + ", CONSTRAINT fk_category_id FOREIGN KEY (" + AccountingTemplateColumns.CATEGORY_ID + ") REFERENCES category (_id) ON DELETE CASCADE"
            + ", CONSTRAINT empty_type CHECK(accounting_template__type <> '') ON CONFLICT FAIL"
            + ", CONSTRAINT empty_interval CHECK(accounting_template__interval <> '') ON CONFLICT FAIL"
            + " );";

    public static final String SQL_CREATE_TABLE_CATEGORY = "CREATE TABLE IF NOT EXISTS "
            + CategoryColumns.TABLE_NAME + " ( "
            + CategoryColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CategoryColumns.NAME + " TEXT NOT NULL, "
            + CategoryColumns.SECTION + " TEXT NOT NULL, "
            + CategoryColumns.INTERVAL + " TEXT NOT NULL, "
            + CategoryColumns.TYPE + " TEXT NOT NULL, "
            + CategoryColumns.LEVEL + " INTEGER NOT NULL "
            + " );";

    public static final String SQL_CREATE_TABLE_INTERVAL = "CREATE TABLE IF NOT EXISTS "
            + IntervalColumns.TABLE_NAME + " ( "
            + IntervalColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IntervalColumns.ACCOUNT_ID + " INTEGER NOT NULL, "
            + IntervalColumns.CATEGORY_ID + " INTEGER NOT NULL, "
            + IntervalColumns.COMMENT + " TEXT, "
            + IntervalColumns.INTERVAL + " TEXT NOT NULL, "
            + IntervalColumns.DATE_START + " INTEGER NOT NULL, "
            + IntervalColumns.DATE_END + " INTEGER NOT NULL, "
            + IntervalColumns.DATE_LAST + " INTEGER NOT NULL, "
            + IntervalColumns.DATE_UPDATED_UNTIL + " INTEGER NOT NULL, "
            + IntervalColumns.TYPE + " TEXT NOT NULL, "
            + IntervalColumns.VALUE + " INTEGER NOT NULL "
            + ", CONSTRAINT fk_account_id FOREIGN KEY (" + IntervalColumns.ACCOUNT_ID + ") REFERENCES account (_id) ON DELETE CASCADE"
            + ", CONSTRAINT fk_category_id FOREIGN KEY (" + IntervalColumns.CATEGORY_ID + ") REFERENCES category (_id) ON DELETE CASCADE"
            + " );";

    public static final String SQL_CREATE_TABLE_INTERVAL_ACCOUNTING = "CREATE TABLE IF NOT EXISTS "
            + IntervalAccountingColumns.TABLE_NAME + " ( "
            + IntervalAccountingColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IntervalAccountingColumns.INTERVAL_ID + " INTEGER NOT NULL, "
            + IntervalAccountingColumns.ACCOUNTING_ID + " INTEGER NOT NULL "
            + ", CONSTRAINT fk_interval_id FOREIGN KEY (" + IntervalAccountingColumns.INTERVAL_ID + ") REFERENCES interval (_id) ON DELETE CASCADE"
            + ", CONSTRAINT fk_accounting_id FOREIGN KEY (" + IntervalAccountingColumns.ACCOUNTING_ID + ") REFERENCES accounting (_id) ON DELETE CASCADE"
            + " );";

    public static final String SQL_CREATE_TABLE_TEMPLATE_MATCHING = "CREATE TABLE IF NOT EXISTS "
            + TemplateMatchingColumns.TABLE_NAME + " ( "
            + TemplateMatchingColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TemplateMatchingColumns.TEXT + " TEXT NOT NULL, "
            + TemplateMatchingColumns.ACCOUNTING_TEMPLATE_ID + " INTEGER NOT NULL "
            + ", CONSTRAINT fk_accounting_template_id FOREIGN KEY (" + TemplateMatchingColumns.ACCOUNTING_TEMPLATE_ID + ") REFERENCES accounting_template (_id) ON DELETE CASCADE"
            + ", CONSTRAINT empty_text CHECK(text <> '') ON CONFLICT FAIL"
            + " );";

    // @formatter:on

    public static QuAccSQLiteOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static QuAccSQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static QuAccSQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new QuAccSQLiteOpenHelper(context);
    }

    private QuAccSQLiteOpenHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new QuAccSQLiteOpenHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static QuAccSQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new QuAccSQLiteOpenHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private QuAccSQLiteOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new QuAccSQLiteOpenHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_ACCOUNT);
        db.execSQL(SQL_CREATE_TABLE_ACCOUNTING);
        db.execSQL(SQL_CREATE_TABLE_ACCOUNTING_TEMPLATE);
        db.execSQL(SQL_CREATE_TABLE_CATEGORY);
        db.execSQL(SQL_CREATE_TABLE_INTERVAL);
        db.execSQL(SQL_CREATE_TABLE_INTERVAL_ACCOUNTING);
        db.execSQL(SQL_CREATE_TABLE_TEMPLATE_MATCHING);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
