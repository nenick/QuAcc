package de.nenick.quacc.view.accounting_edit;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.Date;

import de.nenick.quacc.R;
import de.nenick.quacc.core.accounting.update.UpdateOnceOnlyAccountingFunction;
import de.nenick.quacc.database.accounting.AccountingDb;
import de.nenick.quacc.database.provider.accounting.AccountingCursor;
import de.nenick.quacc.database.provider.category.CategoryCursor;
import de.nenick.quacc.valueparser.ParseValueFromIntegerFunction;
import de.nenick.quacc.view.common.adapter.IntervalAdapter;
import de.nenick.quacc.core.accounting.creation.CreateAccountingFunction;
import de.nenick.quacc.core.accounting.creation.CreateIntervalFunction;
import de.nenick.quacc.core.accounting.interval.GetAccountingIntervalsFunction;
import de.nenick.quacc.core.accounting.type.GetAccountingTypesFunction;
import de.nenick.quacc.core.account.GetAccountsFunction;
import de.nenick.quacc.valueparser.ParseValueFromStringFunction;
import de.nenick.quacc.view.accounting_edit.speechrecognition.SpeechRecognitionFeature;
import de.nenick.quacc.view.mvp.BasePresenterFragment;
import de.nenick.quacc.view.mvp.BaseView;
import de.nenick.quacc.core.common.util.QuAccDateUtil;
import de.nenick.quacc.core.accounting.interval.AccountingInterval;
import de.nenick.quacc.core.accounting.type.AccountingType;
import de.nenick.quacc.view.accounting_edit.adapter.CategoryAdapter;
import de.nenick.quacc.view.common.adapter.TypeAdapter;

import static de.nenick.quacc.valueparser.ParseValueFromStringFunction.ParseResult.Successful;
import static java.lang.Enum.valueOf;

@EFragment(R.layout.fragment_create_accounting)
@OptionsMenu(R.menu.menu_create_account)
public class EditAccountingFragment extends BasePresenterFragment {

    @FragmentArg
    long accountingId;
    String accountingIntervalInitial;

    @Bean
    EditAccountingView view;

    @Bean
    GetAccountingIntervalsFunction getAccountingIntervalsFunction;

    @Bean
    GetAccountingTypesFunction getAccountingTypesFunction;

    @Bean
    GetAccountsFunction getAccountsFunction;

    @Bean
    CreateAccountingFunction createAccountingFunction;

    @Bean
    CreateIntervalFunction createIntervalFunction;

    UpdateOnceOnlyAccountingFunction updateOnceOnlyAccountingFunction;

    @Bean
    ParseValueFromStringFunction parseValueFromStringFunction;

    @Bean
    ParseValueFromIntegerFunction parseValueFromIntegerFunction;

    @Bean
    SpeechRecognitionFeature speechRecognitionFeature;

    @Bean
    IntervalAdapter intervalAdapter;

    @Bean
    TypeAdapter typeAdapter;

    @Bean
    CategoryAdapter categoryAdapter;

    @Bean
    AccountingDb accountingDb;

    @Override
    protected BaseView getBaseView() {
        return view;
    }

    @Override
    protected void onViewStart() {
        view.setDate(QuAccDateUtil.currentDate());
        view.setEndDate(QuAccDateUtil.currentDate());
        view.hideEndDate();

        view.setAccounts(getAccountsFunction.apply());
        speechRecognitionFeature.setView(view);

        intervalAdapter.addAll(getAccountingIntervalsFunction.apply());
        view.setAccountingIntervals(intervalAdapter);
        view.setAccountingInterval(AccountingInterval.once.name());

        typeAdapter.addAll(getAccountingTypesFunction.apply());
        view.setAccountingTypes(typeAdapter);
        view.setAccountingType(AccountingType.outgoing.name());

        view.setAccountingCategories(categoryAdapter);
        reloadCategories();


        if(accountingId != 0) {
            loadAccountingProperties();
        }
    }

    private void loadAccountingProperties() {
        AccountingCursor accountingCursor = accountingDb.getById(accountingId);
        accountingCursor.moveToNext();
        view.setAccount(accountingCursor.getAccountName());
        String accountingIntervalInitial = accountingCursor.getInterval();
        view.setAccountingInterval(accountingIntervalInitial);
        view.setAccountingType(accountingCursor.getType());
        view.setDate(QuAccDateUtil.toString(accountingCursor.getDate()));
        view.setValue(parseValueFromIntegerFunction.apply(accountingCursor.getValue()));
        view.setComment(accountingCursor.getComment());
        accountingCursor.close();
    }

    @Override
    protected void onViewPause() {
        speechRecognitionFeature.onViewPause();
    }

    @Override
    protected void onViewFinish() {
        speechRecognitionFeature.onViewFinish();
    }

    @OptionsItem(R.id.confirm)
    protected void onConfirmNewAccounting() {
        view.closeSoftKeyboard();
        String value = view.getValue();
        ParseValueFromStringFunction.Result valueResult = parseValueFromStringFunction.apply(value);
        if (valueResult.report == Successful) {
            if(accountingId == 0) {
                createNewAccounting(valueResult);
            } else {
                updateAccounting(valueResult);
            }
        } else {
            showParsingError(valueResult);
        }
    }

    private void updateAccounting(ParseValueFromStringFunction.Result valueResult) {
        String account = view.getAccount();
        String accountingType = view.getAccountingType();
        String accountingInterval = view.getAccountingInterval();
        CategoryCursor accountingCategory = view.getAccountingCategory();
        String dateString = view.getDate();
        String comment = view.getComment();
        view.finish();
        Date date = QuAccDateUtil.toDate(dateString);
        if(accountingInterval.equals(AccountingInterval.once.name()) && accountingIntervalInitial.equals(AccountingInterval.once.name())) {
           updateOnceOnlyAccountingFunction.apply(accountingId, account, accountingType, accountingCategory.getId(), date, valueResult.value, comment);
        }
    }

    private void createNewAccounting(ParseValueFromStringFunction.Result valueResult) {
        String account = view.getAccount();
        String accountingType = view.getAccountingType();
        String accountingInterval = view.getAccountingInterval();
        CategoryCursor accountingCategory = view.getAccountingCategory();
        String dateString = view.getDate();
        String comment = view.getComment();
        view.finish();
        Date date = QuAccDateUtil.toDate(dateString);
        if(accountingInterval.equals(AccountingInterval.once.name())) {
            createAccountingFunction.apply(account, accountingType, accountingInterval, accountingCategory.getId(), date, valueResult.value, comment);
        } else {
            if(view.isEndDateActive()) {
                String endDateString = view.getEndDate();
                Date endDate = QuAccDateUtil.toDate(endDateString);
                createIntervalFunction.applyWithEndDate(account, accountingType, accountingInterval, accountingCategory.getId(), date, endDate
                        , valueResult.value, comment);
            } else {
                createIntervalFunction.apply(account, accountingType, accountingInterval, accountingCategory.getId(), date, valueResult.value, comment);
            }
        }
    }

    private void showParsingError(ParseValueFromStringFunction.Result valueResult) {
        switch (valueResult.report) {
            case EmptyInput:
                view.showValueParsingError(R.string.parse_error_missing_value);
                break;
            case ZeroValue:
                view.showValueParsingError(R.string.parse_error_missing_value);
                break;
            case InvalidChar:
                view.showValueParsingError(R.string.parse_error_invalid_char);
                break;
            case InvalidFormat:
                view.showValueParsingError(R.string.parse_error_invalid_format);
                break;
            case UnknownError:
            default:
                view.showValueParsingError(R.string.parse_error_unknown);
        }
    }

    @CheckedChange(R.id.endDateCheck)
    public void onToggleEndDate() {
        if(view.isEndDateActive()) {
            view.enableEndDate();
        } else {
            view.disableEndDate();
        }
    }

    @ItemSelect(R.id.interval)
    public void onIntervalSelection(boolean selected, int position) {
        if(view.getAccountingInterval().equals(AccountingInterval.once.name())) {
            view.hideEndDate();
        } else {
            view.showEndDate();
        }
        reloadCategories();
    }

    @ItemSelect(R.id.category)
    public void onCategorySelection(boolean selected, int position) {
        CategoryCursor item = categoryAdapter.getItem(position);
        view.setSection(item.getSection());
    }

    @ItemSelect(R.id.type)
    public void onTypeSelection(boolean selected, int position) {
        reloadCategories();
    }

    private void reloadCategories() {
        String accountingInterval = view.getAccountingInterval();
        String accountingType = view.getAccountingType();
        if (isViewNotFullInitialized(accountingInterval, accountingType)) {
            return;
        }
        categoryAdapter.updateFor(accountingInterval, accountingType);
    }

    private boolean isViewNotFullInitialized(String accountingInterval, String accountingType) {
        return accountingInterval == null || accountingType == null;
    }
}
