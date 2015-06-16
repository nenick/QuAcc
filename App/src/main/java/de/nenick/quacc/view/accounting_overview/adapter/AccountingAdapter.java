package de.nenick.quacc.view.accounting_overview.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.joda.time.DateTime;

import de.nenick.quacc.accounting.GetAccountingFunction;
import de.nenick.quacc.accounting.creation.UpdateIntervalsFunction;
import de.nenick.quacc.common.valueparser.ParseValueFromIntegerFunction;
import de.nenick.quacc.common.util.QuAccDateUtil;
import de.nenick.quacc.database.AccountingType;
import de.nenick.quacc.database.provider.accounting.AccountingCursor;
import de.nenick.quacc.view.i18n.AccountingIntervalTranslator;
import de.nenick.quacc.view.i18n.AccountingTypeTranslator;

@EBean
public class AccountingAdapter extends CursorAdapter {

    @RootContext
    Context context;

    @Bean
    GetAccountingFunction getAccountingFunction;

    @Bean
    AccountingIntervalTranslator accountingIntervalTranslator;

    @Bean
    AccountingTypeTranslator accountingTypeTranslator;

    @Bean
    ParseValueFromIntegerFunction parseValueFromInteger;

    @Bean
    UpdateIntervalsFunction updateIntervalsFunction;

    private String account;

    public AccountingAdapter() {
        super(null, null, true);
    }

    @AfterInject
    protected void afterInject() {
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return AccountingItemView_.build(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        bindView((AccountingItemView) view, (AccountingCursor) cursor);
    }

    private void bindView(AccountingItemView view, AccountingCursor accountingCursor) {
        AccountingType accountingType = AccountingType.valueOf(accountingCursor.getType());
        switch (accountingType) {
            case incoming:
                view.showAsIncome();
                break;
            case outgoing:
                view.showAsOutgoing();
        }

        view.setDate(QuAccDateUtil.toString(accountingCursor.getDate()));
        view.setInterval(accountingIntervalTranslator.translate(accountingCursor.getInterval()));
        view.setCategory(accountingCursor.getCategoryName());
        view.setComment(accountingCursor.getComment());
        view.setValue(createValueString(accountingCursor));
    }

    private String createValueString(AccountingCursor accountingCursor) {
        int value = accountingCursor.getValue();
        return parseValueFromInteger.apply(value);
    }

    public void changeFor(DateTime startDate, DateTime endDate) {
        updateIntervalsFunction.apply(account, endDate);
        AccountingCursor apply = getAccountingFunction.apply(account, startDate, endDate);
        changeCursor(apply);
    }

    public void setAccount(String account) {
        this.account = account;
    }
}