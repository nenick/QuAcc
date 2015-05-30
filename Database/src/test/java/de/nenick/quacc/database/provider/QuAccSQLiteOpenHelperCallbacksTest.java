package de.nenick.quacc.database.provider;

import org.junit.Test;

import de.nenick.quacc.database.provider.account.AccountCursor;
import de.nenick.quacc.database.provider.account.AccountSelection;
import de.nenick.quacc.database.provider.category.CategoryCursor;
import de.nenick.quacc.database.provider.category.CategorySelection;
import de.nenick.quacc.database.robolectric.RoboDatabaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class QuAccSQLiteOpenHelperCallbacksTest extends RoboDatabaseTest {

    @Test
    public void shouldAddInitialData() {
        AccountCursor account = new AccountSelection().query(context.getContentResolver());
        assertThat(account.getCount()).isEqualTo(3);

        CategoryCursor accountingCategory = new CategorySelection().query(context.getContentResolver());
        assertThat(accountingCategory.getCount()).isEqualTo(115);
    }
}