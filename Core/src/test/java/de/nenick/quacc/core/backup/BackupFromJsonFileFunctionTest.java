package de.nenick.quacc.core.backup;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import de.nenick.quacc.core.backup.model.AccountJson;
import de.nenick.quacc.core.backup.model.AccountingJson;
import de.nenick.quacc.core.backup.model.BackupJson;
import de.nenick.quacc.core.backup.model.CategoryJson;
import de.nenick.quacc.core.backup.model.IntervalJson;
import de.nenick.quacc.core.backup.model.TemplateJson;
import de.nenick.quacc.core.backup.model.TemplateMatchingJson;
import de.nenick.quacc.database.account.AccountDb;
import de.nenick.quacc.database.accounting.AccountingDb;
import de.nenick.quacc.database.category.CategoryDb;
import de.nenick.quacc.database.interval.IntervalDb;
import de.nenick.quacc.database.template.AccountingTemplateDb;
import de.nenick.quacc.database.template.TemplateMatchingDb;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class BackupFromJsonFileFunctionTest {

    @InjectMocks
    BackupFromJsonFileFunction backupFromJsonFileFunction;

    @Mock
    GetInputStreamForFileFunction getInputStreamForFileFunction;

    @Mock
    AccountDb accountDb;

    @Mock
    CategoryDb categoryDb;

    @Mock
    AccountingDb accountingDb;

    @Mock
    AccountingTemplateDb accountingTemplateDb;

    @Mock
    TemplateMatchingDb templateMatchingDb;

    @Mock
    IntervalDb intervalDb;

    InputStream testStream;
    String backupLocation;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReplaceAccountsWithBackup() throws IOException {
        long accountId = 10;
        String accountName = "name";
        int accountInitialValue = 1;

        BackupJson backupJson = new BackupJson();
        backupJson.accounts.add(new AccountJson(accountId, accountName, accountInitialValue));

        givenBackup(backupJson);
        whenApplyBackup();
        verify(accountDb).deleteAll();
        verify(accountDb).insert(accountName, accountInitialValue);
    }

    @Test
    public void shouldReplaceCategoriesWithBackup() throws IOException {
        long categoryId = 11;
        String categoryName = "name";
        String categorySection = "section";
        String categoryInterval = "interval";
        String categoryType = "type";
        int categoryLevel = 1;

        BackupJson backupJson = new BackupJson();
        backupJson.categories.add(new CategoryJson(categoryId, categoryName, categorySection, categoryInterval, categoryType, categoryLevel));

        givenBackup(backupJson);
        whenApplyBackup();
        verify(categoryDb).deleteAll();
        verify(categoryDb).insert(categorySection, categoryName, categoryInterval, categoryType, categoryLevel);
    }

    @Test
    public void shouldReplaceAccountingWithBackup() throws IOException {
        long accountId = 10;
        String accountName = "name";
        int accountInitialValue = 1;

        long accountIdNew = 100;
        given(accountDb.insert(accountName, accountInitialValue)).willReturn(accountIdNew);

        long categoryId = 11;
        String categoryName = "name";
        String categorySection = "section";
        String categoryInterval = "interval";
        String categoryType = "type";
        int categoryLevel = 1;

        long categoryIdNew = 110;
        given(categoryDb.insert(categorySection, categoryName, categoryInterval, categoryType, categoryLevel)).willReturn(categoryIdNew);

        long accountingId = 12;
        String accountingInterval = "interval";
        String accountingType = "type";
        String accountingComment = "comment";
        Date accountingDate = new Date();
        int accountingValue = 1;

        BackupJson backupJson = new BackupJson();
        backupJson.accounts.add(new AccountJson(accountId, accountName, accountInitialValue));
        backupJson.categories.add(new CategoryJson(categoryId, categoryName, categorySection, categoryInterval, categoryType, categoryLevel));
        backupJson.accounting.add(new AccountingJson(accountingId, accountId, categoryId, accountingComment, accountingInterval, accountingType, accountingDate, accountingValue));

        givenBackup(backupJson);
        whenApplyBackup();
        verify(accountingDb).deleteAll();
        verify(accountingDb).insert(accountIdNew, accountingType, accountingInterval, categoryIdNew, accountingDate, accountingComment, accountingValue);
    }

    @Test
    public void shouldReplaceIntervalsWithBackup() throws IOException {
        long accountId = 10;
        String accountName = "name";
        int accountInitialValue = 1;

        long accountIdNew = 100;
        given(accountDb.insert(accountName, accountInitialValue)).willReturn(accountIdNew);

        long categoryId = 11;
        String categoryName = "name";
        String categorySection = "section";
        String categoryInterval = "interval";
        String categoryType = "type";
        int categoryLevel = 1;

        long categoryIdNew = 110;
        given(categoryDb.insert(categorySection, categoryName, categoryInterval, categoryType, categoryLevel)).willReturn(categoryIdNew);

        long intervalId = 12;
        String intervalInterval = "interval";
        String intervalType = "type";
        String intervalComment = "comment";
        Date intervalDateStart = new Date(new Date().getTime() - 300000);
        Date intervalDateEnd = new Date();
        int intervalValue = 1;

        BackupJson backupJson = new BackupJson();
        backupJson.accounts.add(new AccountJson(accountId, accountName, accountInitialValue));
        backupJson.categories.add(new CategoryJson(categoryId, categoryName, categorySection, categoryInterval, categoryType, categoryLevel));
        backupJson.intervals.add(new IntervalJson(intervalId, accountId, categoryId, intervalComment, intervalInterval, intervalType, intervalDateStart, intervalDateEnd, intervalValue));

        givenBackup(backupJson);
        whenApplyBackup();
        verify(intervalDb).deleteAll();
        verify(intervalDb).insert(accountIdNew, intervalType, intervalInterval, categoryIdNew, intervalDateStart, intervalDateEnd, intervalComment, intervalValue);
    }

    @Test
    public void shouldReplaceTemplatesWithBackup() throws IOException {
        long accountId = 10;
        String accountName = "name";
        int accountInitialValue = 1;

        long accountIdNew = 100;
        given(accountDb.insert(accountName, accountInitialValue)).willReturn(accountIdNew);

        long categoryId = 11;
        String categoryName = "name";
        String categorySection = "section";
        String categoryInterval = "interval";
        String categoryType = "type";
        int categoryLevel = 1;

        long categoryIdNew = 110;
        given(categoryDb.insert(categorySection, categoryName, categoryInterval, categoryType, categoryLevel)).willReturn(categoryIdNew);

        long templateId = 12;
        String templateInterval = "interval";
        String templateType = "type";
        String templateComment = "comment";

        BackupJson backupJson = new BackupJson();
        backupJson.accounts.add(new AccountJson(accountId, accountName, accountInitialValue));
        backupJson.categories.add(new CategoryJson(categoryId, categoryName, categorySection, categoryInterval, categoryType, categoryLevel));
        backupJson.templates.add(new TemplateJson(templateId, accountId, categoryId, templateComment, templateInterval, templateType));

        givenBackup(backupJson);
        whenApplyBackup();
        verify(accountingTemplateDb).deleteAll();
        verify(accountingTemplateDb).insert(accountIdNew, templateType, templateInterval, categoryIdNew, templateComment);
    }

    @Test
    public void shouldReplaceTemplateMatchingWithBackup() throws IOException {
        long accountId = 109;
        String accountName = "name";
        int accountInitialValue = 1;

        long accountIdNew = 100;
        given(accountDb.insert(accountName, accountInitialValue)).willReturn(accountIdNew);

        long categoryId = 11;
        String categoryName = "name";
        String categorySection = "section";
        String categoryInterval = "interval";
        String categoryType = "type";
        int categoryLevel = 1;

        long categoryIdNew = 110;
        given(categoryDb.insert(categorySection, categoryName, categoryInterval, categoryType, categoryLevel)).willReturn(categoryIdNew);

        long templateId = 12;
        String templateInterval = "interval";
        String templateType = "type";
        String templateComment = "comment";

        long templateIdNew = 120;
        given(accountingTemplateDb.insert(accountIdNew, templateType, templateInterval, categoryIdNew, templateComment)).willReturn(templateIdNew);

        String templateMatchText = "text";

        BackupJson backupJson = new BackupJson();
        backupJson.accounts.add(new AccountJson(accountId, accountName, accountInitialValue));
        backupJson.categories.add(new CategoryJson(categoryId, categoryName, categorySection, categoryInterval, categoryType, categoryLevel));
        backupJson.templates.add(new TemplateJson(templateId, accountId, categoryId, templateComment, templateInterval, templateType));
        backupJson.templateMatches.add(new TemplateMatchingJson(templateMatchText, templateId));

        givenBackup(backupJson);
        whenApplyBackup();
        verify(templateMatchingDb).deleteAll();
        verify(templateMatchingDb).insert(templateMatchText, templateIdNew);
    }

    private void whenApplyBackup() {
        backupFromJsonFileFunction.apply(backupLocation);
    }

    private void givenBackup(BackupJson backupJson) throws IOException {
        String input = new ObjectMapper().writeValueAsString(backupJson);
        testStream = IOUtils.toInputStream(input);
        given(getInputStreamForFileFunction.apply(backupLocation)).willReturn(testStream);
    }
}