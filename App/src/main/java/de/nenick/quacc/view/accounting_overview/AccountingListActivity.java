package de.nenick.quacc.view.accounting_overview;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import de.nenick.quacc.R;
import de.nenick.quacc.backup.WriteDataFunction;
import de.nenick.quacc.view.account.AccountsActivity_;
import de.nenick.quacc.view.category.CategoriesActivity_;

@EActivity(R.layout.activity_accounting_list)
public class AccountingListActivity extends ActionBarActivity
        implements AccountingListDrawer.NavigationDrawerCallbacks {

    public static final String TAG_FRAGMENT = AccountingListFragment.class.getSimpleName();

    private CharSequence mTitle;
    private String mAccount;

    @ViewById(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @FragmentById(R.id.navigation_drawer)
    AccountingListDrawer accountingListDrawer;

    @Bean
    WriteDataFunction writeDataFunction;

    @AfterViews
    protected void onCreateView() {
        mTitle = getTitle();
        initFragment();

        // Set up the drawer.
        accountingListDrawer.setUp(
                R.id.navigation_drawer,
                drawerLayout);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager();
    }

    private void initFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (fragmentManager.findFragmentByTag(TAG_FRAGMENT) == null) {
            onNavigationDrawerItemSelected(0);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        String account = getAccountNameByPosition(position);
        if(account.equals(mAccount)) {
            // reload fragment is not necessary if the current and selected account are same
            return;
        }

        mAccount = account;
        replaceFragmentForCurrentAccount();
    }

    private void replaceFragmentForCurrentAccount() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(mAccount.isEmpty()) {
            throw new IllegalStateException();
        }
        fragmentManager.beginTransaction().replace(R.id.container, AccountingListFragment_.builder().account(mAccount).build(), TAG_FRAGMENT).commit();
    }

    private String getAccountNameByPosition(int position) {
        String account;
        if(position == 0) {
            account = "Girokonto";
        } else if (position == 1) {
            account = "Bar";
        } else {
            account = "Tagesgeldkonto";
        }
        return account;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!accountingListDrawer.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_accounting_list, menu);
            getSupportActionBar().setTitle(mTitle + " (" + mAccount + ")");
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem(R.id.category)
    protected void onCategoriesEditor() {
        CategoriesActivity_.intent(this).start();
    }

    @OptionsItem(R.id.account)
    protected void onAccountsEditor() {
        AccountsActivity_.intent(this).start();
    }

    @OptionsItem(R.id.import_data)
    protected void onImport() {
        writeDataFunction.importDatabase();
    }

    @OptionsItem(R.id.export_data)
    protected void onExport() {
        writeDataFunction.exportDatabase();
    }
}