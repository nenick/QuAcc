package de.nenick.quacc.addaccounting;

import android.os.Bundle;
import android.speech.SpeechRecognizer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.nenick.quacc.TestQuAccApplication;
import de.nenick.quacc.speechrecognition.SpeechRecognitionListener;
import de.nenick.quacc.speechrecognition.SpeechRecognitionWrapper;
import de.nenick.robolectric.RoboSup;
import de.nenick.robolectric.RobolectricSupportedTest;
import de.nenick.robolectricpages.components.RoboSpinnerEntry;
import de.nenick.robolectricpages.dialogs.RoboDatePickerDialog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

public class AddAccountingFragmentTest extends RobolectricSupportedTest {

    RoboSup<AddAccountingActivity_, AddAccountingFragment> robo = new RoboSup<>();

    RoboAddAccountingPage addAccountingPage = new RoboAddAccountingPage(robo);
    RoboDatePickerDialog datePickerDialog = new RoboDatePickerDialog();

    List<RoboSpinnerEntry> entries;
    Calendar calendar = Calendar.getInstance(Locale.GERMAN);

    @Mock
    SpeechRecognitionWrapper mockSpeechRecognition;

    @Captor
    ArgumentCaptor<SpeechRecognitionListener> speechRecognitionListenerArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        given(TestQuAccApplication.coreModuleMocks.recognizeAccountingTypeUc.apply(anyString())).willReturn("");
        given(TestQuAccApplication.coreModuleMocks.getAccountingCategoriesUc.apply()).willReturn(new CharSequence[]{"Beruf", "Essen", "Freizeit", "Miete"});
        given(TestQuAccApplication.coreModuleMocks.getAccountingIntervalsUc.apply()).willReturn(new CharSequence[]{"Einmahlig", "Wöchentlich", "Monatlich", "Alle 3 Monate"});
        given(TestQuAccApplication.coreModuleMocks.getAccountingTypesUc.apply()).willReturn(new CharSequence[]{"Ausgabe", "Einnahme"});
        given(TestQuAccApplication.coreModuleMocks.getAccountsUc.apply()).willReturn(new CharSequence[]{"Konto", "Sparkonto", "Bar"});
    }

    @Test
    public void shouldHaveCorrectInitialState() {
        addAccountingPage.startPage();

        entries = addAccountingPage.accountSpinner().entries();
        assertThat(entries).hasSize(3);
        assertThat(entries.get(0).getText()).isEqualTo("Konto");
        assertThat(entries.get(1).getText()).isEqualTo("Sparkonto");
        assertThat(entries.get(2).getText()).isEqualTo("Bar");
        assertThat(addAccountingPage.accountSpinner().selectedEntry().getText()).isEqualTo("Konto");

        entries = addAccountingPage.typeSpinner().entries();
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).getText()).isEqualTo("Ausgabe");
        assertThat(entries.get(1).getText()).isEqualTo("Einnahme");
        assertThat(addAccountingPage.typeSpinner().selectedEntry().getText()).isEqualTo("Ausgabe");

        entries = addAccountingPage.intervalSpinner().entries();
        assertThat(entries).hasSize(4);
        assertThat(entries.get(0).getText()).isEqualTo("Einmahlig");
        assertThat(entries.get(1).getText()).isEqualTo("Wöchentlich");
        assertThat(entries.get(2).getText()).isEqualTo("Monatlich");
        assertThat(entries.get(3).getText()).isEqualTo("Alle 3 Monate");
        assertThat(addAccountingPage.intervalSpinner().selectedEntry().getText()).isEqualTo("Einmahlig");

        entries = addAccountingPage.categorySpinner().entries();
        assertThat(entries).hasSize(4);
        assertThat(entries.get(0).getText()).isEqualTo("Beruf");
        assertThat(entries.get(1).getText()).isEqualTo("Essen");
        assertThat(entries.get(2).getText()).isEqualTo("Freizeit");
        assertThat(entries.get(3).getText()).isEqualTo("Miete");
        assertThat(addAccountingPage.intervalSpinner().selectedEntry().getText()).isEqualTo("Einmahlig");

        assertThat(addAccountingPage.dateField().getText()).isEqualTo(String.format("%s.%s.%s", day(), month(), year()));
    }

    @Test
    public void shouldShowValueFromDatePicker() {
        addAccountingPage.startPage();
        addAccountingPage.dateField().click();
        assertThat(datePickerDialog.isShowing()).isTrue();
        datePickerDialog.pickDate(21, 12, 2012);
        datePickerDialog.clickOk();
        assertThat(addAccountingPage.dateField().getText()).isEqualTo(String.format("%s.%s.%s", day(21), month(12), year(2012)));
    }

    @Test
    public void shouldSaveStateForConfigChanges() {
        addAccountingPage.startPage();

        addAccountingPage.accountSpinner().entries().get(2).select();
        addAccountingPage.typeSpinner().entries().get(1).select();
        addAccountingPage.intervalSpinner().entries().get(2).select();
        addAccountingPage.categorySpinner().entries().get(3).select();

        addAccountingPage.dateField().click();
        datePickerDialog.pickDate(21, 12, 2012);
        datePickerDialog.clickOk();

        robo.activityController.restart();

        assertThat(addAccountingPage.accountSpinner().selectedEntry().getText()).isEqualTo("Bar");
        assertThat(addAccountingPage.typeSpinner().selectedEntry().getText()).isEqualTo("Einnahme");
        assertThat(addAccountingPage.intervalSpinner().selectedEntry().getText()).isEqualTo("Monatlich");
        assertThat(addAccountingPage.categorySpinner().selectedEntry().getText()).isEqualTo("Miete");
        assertThat(addAccountingPage.dateField().getText()).isEqualTo(String.format("%s.%s.%s", day(21), month(12), year(2012)));
    }

    @Test
    public void shouldToggleSpeechRecognition() {
        addAccountingPage.startPageWithMocks(mockSpeechRecognition);
        addAccountingPage.speechButton().click();
        verify(mockSpeechRecognition).toggle();
    }

    @Test
    public void shouldShowSpeechResult() {
        addAccountingPage.startPage();
        addAccountingPage.speechResult("tell me something");
        assertThat(addAccountingPage.speechResultField().getText()).isEqualTo("tell me something");
    }

    @Test
    public void shouldDestroySpeechRecognition() {
        addAccountingPage.startPageWithMocks(mockSpeechRecognition);
        robo.activityController.destroy();
        verify(mockSpeechRecognition).destroy();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailAtUnknownActivityResult() {
        AddAccountingFragment addAccountingFragment = new AddAccountingFragment();
        addAccountingFragment.onActivityResult(123456, 0, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldFailAtUnknownMultipleSpeechResult() {
        addAccountingPage.startPage();
        addAccountingPage.speechResult("tell me something", "more than expected");
    }

    public Bundle speechResultBundle(String text) {
        ArrayList<String> speechResultText = new ArrayList<>();
        speechResultText.add(text);
        Bundle speechResultBundle = new Bundle();
        speechResultBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, speechResultText);
        return speechResultBundle;
    }

    private String year() {
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    private String year(int value) {
        return String.valueOf(value);
    }

    private String month() {
        int value = calendar.get(Calendar.MONTH) + 1;
        return withLeadingZero(value);
    }

    private String month(int value) {
        return withLeadingZero(value);
    }

    private String day() {
        int value = calendar.get(Calendar.DAY_OF_MONTH);
        return withLeadingZero(value);
    }

    private String day(int value) {
        return withLeadingZero(value);
    }

    private String withLeadingZero(int value) {
        String asString = String.valueOf(value);
        if (value >= 10) {
            return asString;
        }
        return "0" + asString;
    }
}
