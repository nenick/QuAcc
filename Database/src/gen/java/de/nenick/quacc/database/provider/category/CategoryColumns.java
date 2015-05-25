package de.nenick.quacc.database.provider.category;

import android.net.Uri;
import android.provider.BaseColumns;

import de.nenick.quacc.database.provider.QuAccProvider;
import de.nenick.quacc.database.provider.account.AccountColumns;
import de.nenick.quacc.database.provider.accounting.AccountingColumns;
import de.nenick.quacc.database.provider.category.CategoryColumns;

/**
 * Columns for the {@code category} table.
 */
public class CategoryColumns implements BaseColumns {
    public static final String TABLE_NAME = "category";
    public static final Uri CONTENT_URI = Uri.parse(QuAccProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    public static final String NAME = "category__name";

    public static final String SECTION = "section";

    public static final String INTERVAL = "category__interval";

    public static final String TYPE = "category__type";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            NAME,
            SECTION,
            INTERVAL,
            TYPE
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
            if (c.equals(SECTION) || c.contains("." + SECTION)) return true;
            if (c.equals(INTERVAL) || c.contains("." + INTERVAL)) return true;
            if (c.equals(TYPE) || c.contains("." + TYPE)) return true;
        }
        return false;
    }

}