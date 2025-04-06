package com.android.editdateview;

import static java.lang.Integer.parseInt;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import java.util.Locale;

public class EditDateView extends AppCompatEditText {

    public static final int POSITION4 = 4;
    public static final int POSITION7 = 7;
    public static final int POSITION2 = 2;
    public static final int POSITION5 = 5;
    public static final String SEPARATOR_FORMAT_LOG_MESSAGE = "format must be \".\", \"/\" or \"-\". Use constants from DefaultSettings";
    public static final int MAX_LENGTH_FOR_FILTER = 11;
    private int day;
    private int month;
    private int year;
    private final char[] chars = new char[10];
    private boolean isUpdatingText = false;
    private boolean isPasting = false;
    private OnDateChange onDateChangeListener;

    private static final char CHAR_PLACEHOLDER = ' ';

    private int dateViewFormat = DefaultSettings.DATE_VIEW_FORMAT_DMY;
    private char dateSeparator = DefaultSettings.DATE_SEPARATOR_DOT;
    private char dayPlaceholder = DefaultSettings.DAY_PLACEHOLDER_EN;
    private char monthPlaceholder = DefaultSettings.MONTH_PLACEHOLDER_EN;
    private char yearPlaceholder = DefaultSettings.YEAR_PLACEHOLDER_EN;

    private static final String TAG = "EditDate";
    private static final String MESSAGE_FOR_LOG = "Placeholder must be [a-zA-Zа-яА-Я]";

    public interface OnDateChange {
        public void onChange(int day, int month, int year);
    }

    public EditDateView(@NonNull Context context) {

        super(context);

        init();
    }

    public EditDateView(@NonNull Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        Log.i(TAG, "attrs: " );
        init();
    }

    public EditDateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        defStyleAttr=
                Log.i(TAG, "attrs: " + defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);

    }

    public void addOnDateChangeListener(OnDateChange listener) {
        onDateChangeListener = listener;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text.length() != 10 || !isValidDateFormat(text.toString())) {
            return;
        }
        setDateStr(text.toString());
    }

    public boolean setDateStr(@NonNull String date) {
        if (date.length() != 10 || !isValidDateFormat(date)) {
            return false;
        }
        int yearInput = parseInt(date.substring(0, 4));
        int monthInput = parseInt(date.substring(5, 7));
        int dayInput = parseInt(date.substring(8, 10));

        boolean dateChanged = updateDate(dayInput, monthInput, yearInput);

        setTextCustom(new String(chars));

        if (dateChanged) {
            notifyDateChangeListener();
        }
        return true;
    }

    public void resetDate() {
        day = 0;
        month = 0;
        year = 0;
        resetChars();
        setTextCustom("");
    }

    public void setDateSeparator(char separator) {
        if (!DefaultSettings.validateSeparator(separator)) {
            Log.i(TAG, SEPARATOR_FORMAT_LOG_MESSAGE);
            return;
        }
        dateSeparator = separator;
        updateTextInField();
    }

    public void setDateViewFormat(int format) {
        if (!DefaultSettings.validateDateViewFormat(format)) {
            Log.i(TAG, SEPARATOR_FORMAT_LOG_MESSAGE);
            return;
        }
        dateViewFormat = format;
        updateTextInField();
    }

    public void setPlaceholders(Character dayPlaceholder, Character monthPlaceholder, Character yearPlaceholder) {
        if (dayPlaceholder != null) {
            setDayPlaceholder(dayPlaceholder);
        }
        if (monthPlaceholder != null) {
            setMonthPlaceholder(monthPlaceholder);
        }
        if (yearPlaceholder != null) {
            setYearPlaceholder(yearPlaceholder);
        }
    }

    public void setDayPlaceholder(char placeholder) {
        if (!DefaultSettings.validatePlaceholderChar(placeholder)) {
            Log.i(TAG, MESSAGE_FOR_LOG);
            return;
        }
        dayPlaceholder = placeholder;
        setHintCustom();
    }

    public void setMonthPlaceholder(char placeholder) {
        if (!DefaultSettings.validatePlaceholderChar(placeholder)) {
            Log.i(TAG, MESSAGE_FOR_LOG);
            return;
        }
        monthPlaceholder = placeholder;
        setHintCustom();
    }

    public void setYearPlaceholder(char placeholder) {
        if (!DefaultSettings.validatePlaceholderChar(placeholder)) {
            Log.i(TAG, MESSAGE_FOR_LOG);
            return;
        }
        yearPlaceholder = placeholder;
        setHintCustom();
    }

    /**
     * Sets the value of the day of the month and the year
     *
     * @param dd   day
     * @param mm   month
     * @param yyyy year
     */
    public boolean setDateInt(int dd, int mm, int yyyy) {
        if (dd < 1 || mm < 1 || yyyy < 1) {
            Log.i(TAG, "setDateInt: Invalid date (All the parameters must be more than 0");
            return false;
        }
        if (updateDate(dd, mm, yyyy)) {
            notifyDateChangeListener();
            return true;
        }
        return false;
    }

    @Nullable
    public String getDateAsString() {
        if (day == 0 || year == 0 || month == 0) {
            return null;
        }
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    private void updateTextInField() {
        setHintCustom();
        resetChars();
        if (day != 0 || month != 0 || year != 0) {
            makeYearChars();
            makeMonth();
            makeDayChars();
            setTextCustom(new String(chars));
        }
    }

    private void notifyDateChangeListener() {
        if (onDateChangeListener != null) {
            onDateChangeListener.onChange(day, month, year);
        }
    }

    private void init() {
        setHintCustom();
        resetChars();
        setEDFilters();
        setListeners();
    }

    private void setHintCustom() {
        String hint = "";
        switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD:
                hint = "" + yearPlaceholder + yearPlaceholder + yearPlaceholder + yearPlaceholder + dateSeparator + monthPlaceholder + monthPlaceholder + dateSeparator + dayPlaceholder + dayPlaceholder;
                break;
            case DefaultSettings.DATE_VIEW_FORMAT_MDY:
                hint = "" + monthPlaceholder + monthPlaceholder + dateSeparator + dayPlaceholder + dayPlaceholder + dateSeparator + yearPlaceholder + yearPlaceholder + yearPlaceholder + yearPlaceholder;
                break;
            case DefaultSettings.DATE_VIEW_FORMAT_DMY:
                hint = "" + dayPlaceholder + dayPlaceholder + dateSeparator + monthPlaceholder + monthPlaceholder + dateSeparator + yearPlaceholder + yearPlaceholder + yearPlaceholder + yearPlaceholder;
                break;
        }
        setHint(hint);
    }

    private void resetChars() {
        int[] separatorPosition = new int[2];
        separatorPosition[0] = POSITION2;
        separatorPosition[1] = POSITION5;
        if (dateViewFormat == DefaultSettings.DATE_VIEW_FORMAT_YMD) {
            separatorPosition[0] = POSITION4;
            separatorPosition[1] = POSITION7;
        }
        for (int i = 0; i < 10; i++) {
            chars[i] = (i == separatorPosition[0] || i == separatorPosition[1]) ? dateSeparator : CHAR_PLACEHOLDER;
        }
    }

    private void setTextCustom(CharSequence text) {
        isUpdatingText = true;
        super.setText(text, BufferType.NORMAL);
        isUpdatingText = false;
    }

    private boolean isValidDateFormat(String date) {
        if (date == null) {
            return false;
        }
        return date.matches("\\d{4}-\\d{2}-\\d{2}"); // ISO 8601
    }

    private boolean updateDate(int dayInput, int monthInput, int yearInput) {
        boolean changed = false;
        if (year != yearInput) {
            year = validateYear(yearInput);
            changed = true;
        }
        if (month != monthInput) {
            month = validateMonth(monthInput);
            changed = true;
        }
        if (day != dayInput) {
            day = validateDay(dayInput);
            changed = true;
        }
        updateChars();
        setTextCustom(new String(chars));
        return changed;
    }

    private void updateChars() {
        resetChars();
        System.arraycopy(formatDate(), 0, chars, 0, 10);
    }

    private char[] formatDate() {
        String format;
        return switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD -> {
                format = "%04d" + dateSeparator + "%02d" + dateSeparator + "%02d";
                yield String.format(Locale.US, format, year, month, day).toCharArray();
            }
            case DefaultSettings.DATE_VIEW_FORMAT_MDY -> {
                format = "%02d" + dateSeparator + "%02d" + dateSeparator + "%04d";
                yield String.format(Locale.US, format, month, day, year).toCharArray();
            }
            default -> {
                format = "%02d" + dateSeparator + "%02d" + dateSeparator + "%04d";
                yield String.format(Locale.US, format, day, month, year).toCharArray();
            }
        };
    }

    private void setEDFilters() {
        setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_LENGTH_FOR_FILTER),
                new InputFilterMain()
        });
    }

    private void setListeners() {
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public synchronized void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ... is useless in this Context
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingText) {
                    return;
                }
                handleDeletion(start, before);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This Methode is useless in the context
            }
        });

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    charsCheck();
                }
            }
        });
    }

    private void charsCheck() {

        if (checkAllYearCharsIsEmpty(getYearPosition()) || checkDayOrMonthCharsIsEmpty(getDayPosition()) || checkDayOrMonthCharsIsEmpty(getMonthPosition())) {
            resetDate();
        } else {
            boolean isNotChanged = true;
            if (day == 0) {
                isNotChanged = false;
                makeDay();
            }
            if (month == 0) {
                isNotChanged = false;
                makeMonth();
            }
            if (year == 0) {
                isNotChanged = false;
                makeYear();
            }
            if (isNotChanged) {
                makeDayChars();
                makeMonthChars();
                makeYearChars();
            }
            setTextCustom(new String(chars));
        }
    }

    private boolean checkDayOrMonthCharsIsEmpty(int position) {
        return chars[position] == CHAR_PLACEHOLDER && chars[position + 1] == CHAR_PLACEHOLDER;
    }

    private boolean checkAllYearCharsIsEmpty(int position) {
        int count = 0;
        for (int i = position; i < position + 4; i++) {
            if (chars[i] == CHAR_PLACEHOLDER) {
                count++;
            }
        }
        if (count > 3) {
            return true;
        }
        return false;
    }

    private boolean checkCharsIsVoid() {
        int count = 0;
        for (int i = 0; i < 10; i++) {
            if (chars[i] == CHAR_PLACEHOLDER) {
                count++;
            }
        }
        if (count > 7) {   //count>=8 (all chars are void)
            resetDate();
            return true;
        }
        return false;
    }

    private void handleDeletion(int position, int before) {
        if (isPasting) {
            isPasting = false;
            return;
        }
        if (before < 1) {
            return;
        }
        if (before > 10) {
            before = 10;
        }
        int sep1 = 2;
        int sep2 = 5;
        if (dateViewFormat == DefaultSettings.DATE_VIEW_FORMAT_YMD) {
            sep1 = 4;
            sep2 = 7;
        }

        for (int i = position; i < before + position; i++) {
            if (i == sep1 || i == sep2) {
                continue;
            }
            chars[i] = CHAR_PLACEHOLDER;
        }
        if (checkCharsIsVoid()) {
            return;
        }
        setTextCustom(new String(chars));
        setSelection(position);
    }

    private int makeDigits(int startChar, int count) {
        if (startChar + count > 10) {
            return 0;
        }
        int summ = 0;
        int multiplier = 1;
        for (int i = count + startChar - 1; i >= startChar; i--) {
            if (chars[i] != CHAR_PLACEHOLDER && Character.isDigit(chars[i])) {
                summ = summ + (chars[i] - 48) * multiplier;
            }
            multiplier = multiplier * 10;
        }
        return summ;
    }

    private void makeDay() {
        int startPosition = switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD -> 8;
            case DefaultSettings.DATE_VIEW_FORMAT_MDY -> 3;
            default -> 0;
        };
        int dayInput = makeDigits(startPosition, 2);
        day = validateDay(dayInput);
        makeDayChars();
        notifyDateChangeListener();
    }

    private void makeMonth() {
        int startPosition = switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_MDY -> 0;
            case DefaultSettings.DATE_VIEW_FORMAT_YMD -> 5;
            default -> 3;
        };
        int monthInput = makeDigits(startPosition, 2);
        month = validateMonth(monthInput);
        if (day > 0) {
            day = validateDay(day);
            makeDayChars();
        }
        makeMonthChars();
        notifyDateChangeListener();
    }

    private void makeYear() {
        int startPosition = switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD -> 0;
            default -> 6;
        };

        int yearInput = makeDigits(startPosition, 4);
        year = validateYear(yearInput);
        if (day > 0) {
            day = validateDay(day);
            makeDayChars();
        }
        makeYearChars();
        notifyDateChangeListener();
    }

    private int getDayPosition() {
        switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD:
                return 8;
            case DefaultSettings.DATE_VIEW_FORMAT_MDY:
                return 3;
            default:
                return 0;
        }
    }

    private int getMonthPosition() {
        switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD:
                return 5;
            case DefaultSettings.DATE_VIEW_FORMAT_MDY:
                return 0;
            default:
                return 3;
        }
    }

    private int getYearPosition() {
        if (dateViewFormat == DefaultSettings.DATE_VIEW_FORMAT_YMD) {
            return 0;
        }
        return 6;
    }

    private int validateMonth(int month) {
        return Math.max(1, Math.min(month, 12));
    }

    private int validateYear(int year) {
        return Math.max(1, Math.min(year, 2100));
    }

    private int validateDay(int day) {
        if (day < 1) return 1;
        int maxDays = switch (month) {
            case 4, 6, 9, 11 -> 30;
            case 2 -> isLeapYear(year) ? 29 : 28;
            default -> 31;
        };
        return Math.min(day, maxDays);
    }

    private void makeMonthChars() {
        int a0 = month / 10;
        int a1 = month % 10;
        switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_MDY:
                chars[0] = (char) (a0 + 48);
                chars[1] = (char) ('0' + a1);
                break;
            case DefaultSettings.DATE_VIEW_FORMAT_YMD:
                chars[5] = (char) (a0 + 48);
                chars[6] = (char) ('0' + a1);
                break;
            default:
                chars[3] = (char) (a0 + 48);
                chars[4] = (char) ('0' + a1);
                break;
        }
    }

    private void makeDayChars() {
        int a0 = day / 10;
        int a1 = day % 10;
        switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD:
                chars[8] = (char) (a0 + 48);
                chars[9] = (char) ('0' + a1);
                break;
            case DefaultSettings.DATE_VIEW_FORMAT_MDY:
                chars[3] = (char) (a0 + 48);
                chars[4] = (char) ('0' + a1);
                break;
            default:
                chars[0] = (char) (a0 + 48);
                chars[1] = (char) ('0' + a1);
                break;
        }
    }

    private void makeYearChars() {
        int[] a = new int[4];
        a[0] = year / 1000;
        int rest = year % 1000;
        a[1] = rest / 100;
        rest = rest % 100;
        a[2] = rest / 10;
        a[3] = rest % 10;
        int startPosition = switch (dateViewFormat) {
            case DefaultSettings.DATE_VIEW_FORMAT_YMD -> 0;
            default -> 6;
        };

        for (int i = startPosition; i < 4 + startPosition; i++) {
            chars[i] = (char) (a[i - startPosition] + 48);
        }
    }

    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private class InputFilterMain implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (isUpdatingText) return null;
            if (start != 0) return "";
            if (source.length() == 0) return null;
            if (source.length() == 1) {
                if (source.charAt(0) == DefaultSettings.DATE_SEPARATOR_DOT || source.charAt(0) == DefaultSettings.DATE_SEPARATOR_DASH || source.charAt(0) == DefaultSettings.DATE_SEPARATOR_SLASH) {
                    handleSeparatorsInput(dstart);
                }
                if (Character.isDigit(source.charAt(0))) {
                    handleAddition(dstart, source);
                }
            } else if (source.length() == 10) {
                if (handlePasteDate(source)) {
                    isPasting = true;
                    return new String(chars);
                }
            }
            return "";
        }

        private boolean handlePasteDate(CharSequence source) {
            if (source == null) {
                return false;
            }
            String input = source.toString();
            String separators = "" + DefaultSettings.DATE_SEPARATOR_DOT + DefaultSettings.DATE_SEPARATOR_SLASH + DefaultSettings.DATE_SEPARATOR_DASH;
            String regex = "\\d{2}[" + separators + "]\\d{2}[" + separators + "]\\d{4}";
            int sep1Pos = POSITION2;
            int sep2Pos = POSITION5;

            if (dateViewFormat == DefaultSettings.DATE_VIEW_FORMAT_YMD) {
                regex = "\\d{4}[" + separators + "]\\d{2}[" + separators + "]\\d{2}";
                sep1Pos = POSITION4;
                sep2Pos = POSITION7;
            }

            if (!input.matches(regex)) {
                return false;
            }
            for (int i = 0; i < 10; i++) {
                if (i == sep1Pos || i == sep2Pos) {
                    continue;
                }
                chars[i] = input.charAt(i);
            }
//            makeDay();
//            makeMonth();
//            makeYear();
            return true;
        }

        private void handleSeparatorsInput(int position) {
            int newPos = 3;
            switch (dateViewFormat) {
                case DefaultSettings.DATE_VIEW_FORMAT_YMD:
                    newPos = 5;
                    if (position <= 4) {
                        makeYear();
                    } else if (position <= 7) {
                        makeMonth();
                        newPos = 8;
                    } else {
                        makeDay();
                        newPos = 10;
                    }
                    break;
                case DefaultSettings.DATE_VIEW_FORMAT_MDY:
                    if (position <= 2) {
                        makeMonth();
                    } else if (position <= 5) {
                        makeDay();
                        newPos = 6;
                    } else {
                        makeYear();
                        newPos = 10;
                    }
                    break;
                default:
                    if (position <= 2) {
                        makeDay();
                    } else if (position <= 5) {
                        makeMonth();
                        newPos = 6;
                    } else {
                        makeYear();
                        newPos = 10;
                    }
                    break;
            }
            setTextCustom(new String(chars));
            setSelection(newPos);
        }

        private void handleAddition(int position, CharSequence ch) {
            position = switch (dateViewFormat) {
                case DefaultSettings.DATE_VIEW_FORMAT_YMD -> additionYMD(position, ch);
                case DefaultSettings.DATE_VIEW_FORMAT_MDY -> additionMDY(position, ch);
                default -> additionDMY(position, ch);
            };

            String str = new String(chars);
            setTextCustom(str);
            setSelection(position);
        }

        private int additionYMD(int position, CharSequence ch) {
            if (position != 4 && position != 7 && position < 10) {
                chars[position] = ch.charAt(0);
                position++;
            }
            if (position == 4) {
                makeYear();
                position++;
            }
            if (position == 7) {
                makeMonth();
                position++;
            }
            if (position >= 10) {
                makeDay();
            }
            return position;
        }

        private int additionMDY(int position, CharSequence ch) {
            if (position != 2 && position != 5 && position < 10) {
                chars[position] = ch.charAt(0);
                position++;
            }
            if (position == 2) {
                makeMonth();
                position++;
            }
            if (position == 5) {
                makeDay();
                position++;
            }
            if (position >= 10) {
                makeYear();
            }
            return position;
        }

        private int additionDMY(int position, CharSequence ch) {
            if (position != 2 && position != 5 && position < 10) {
                chars[position] = ch.charAt(0);
                position++;
            }
            if (position == 2) {
                makeDay();
                position++;
            }
            if (position == 5) {
                makeMonth();
                position++;
            }
            if (position >= 10) {
                makeYear();
            }
            return position;
        }
    }

    public static class DefaultSettings {

        public static final int DATE_VIEW_FORMAT_DMY = 0;
        public static final int DATE_VIEW_FORMAT_YMD = 1;
        public static final int DATE_VIEW_FORMAT_MDY = 2;

        public static final char DATE_SEPARATOR_DOT = '.';
        public static final char DATE_SEPARATOR_SLASH = '/';
        public static final char DATE_SEPARATOR_DASH = '-';

        public static final char DAY_PLACEHOLDER_EN = 'd';
        public static final char DAY_PLACEHOLDER_RU = 'д';
        public static final char DAY_PLACEHOLDER_DE = 't';
        public static final char MONTH_PLACEHOLDER_EN = 'm';
        public static final char MONTH_PLACEHOLDER_RU = 'м';
        public static final char MONTH_PLACEHOLDER_DE = 'm';
        public static final char YEAR_PLACEHOLDER_EN = 'y';
        public static final char YEAR_PLACEHOLDER_RU = 'г';
        public static final char YEAR_PLACEHOLDER_DE = 'j';
        public static final String ONLY_LETTERS_A_Z_A_Z_ARE_ALLOWED_FOR_A_PLACEHOLDER = "only letters (a-zA-Z) are allowed for a placeholder";

        private static char dayPlaceholder;
        private static char monthPlaceholder;
        private static char yearPlaceholder;
        private static char dateSeparator;
        private static int dateViewFormat;

        static {
            Locale locale = Locale.getDefault();
            switch (locale.getCountry()) {
                case "RU":
                    dayPlaceholder = DAY_PLACEHOLDER_RU;
                    monthPlaceholder = MONTH_PLACEHOLDER_RU;
                    yearPlaceholder = YEAR_PLACEHOLDER_RU;
                    dateViewFormat = DATE_VIEW_FORMAT_DMY;
                    dateSeparator = DATE_SEPARATOR_DOT;
                    break;

                case "DE":
                    dayPlaceholder = DAY_PLACEHOLDER_DE;
                    monthPlaceholder = MONTH_PLACEHOLDER_DE;
                    yearPlaceholder = YEAR_PLACEHOLDER_DE;
                    dateViewFormat = DATE_VIEW_FORMAT_DMY;
                    dateSeparator = DATE_SEPARATOR_DOT;
                    break;

                default:
                    dayPlaceholder = DAY_PLACEHOLDER_EN;
                    monthPlaceholder = MONTH_PLACEHOLDER_EN;
                    yearPlaceholder = YEAR_PLACEHOLDER_EN;
                    dateViewFormat = DATE_VIEW_FORMAT_YMD;
                    dateSeparator = DATE_SEPARATOR_DOT;
                    break;
            }
        }

        public static char getDateSeparator() {
            return dateSeparator;
        }

        public static int getDateViewFormat() {
            return dateViewFormat;
        }

        public static char getDayPlaceholder() {
            return dayPlaceholder;
        }

        public static char getMonthPlaceholder() {
            return monthPlaceholder;
        }

        public static char getYearPlaceholder() {
            return yearPlaceholder;
        }

        public static void setDefaultDateSeparator(char dateSeparator) {
            if (validateSeparator(dateSeparator)) {
                DefaultSettings.dateSeparator = dateSeparator;
            } else {
                Log.i(TAG, "only '.' or '/' or '-' are allowed. use constants: DATE_SEPARATOR_DOT, DATE_SEPARATOR_SLASH, DATE_SEPARATOR_DASH");
            }
        }

        public static void setDefaultDateViewFormat(int dateViewFormat) {
            if (validateDateViewFormat(dateViewFormat)) {
                DefaultSettings.dateViewFormat = dateViewFormat;
            } else {
                Log.i(TAG, "only 0, 1 or 2 are allowed. use constants: DATE_VIEW_FORMAT_MDY, DATE_VIEW_FORMAT_DMY, DATE_VIEW_FORMAT_YMD");
            }
        }

        public static void setDefaultDayPlaceholder(char dayPlaceholder) {
            if (validatePlaceholderChar(dayPlaceholder)) {
                DefaultSettings.dayPlaceholder = dayPlaceholder;
            } else {
                Log.i(TAG, ONLY_LETTERS_A_Z_A_Z_ARE_ALLOWED_FOR_A_PLACEHOLDER);
            }
        }

        public static void setDefaultMonthPlaceholder(char monthPlaceholder) {
            if (validatePlaceholderChar(monthPlaceholder)) {
                DefaultSettings.monthPlaceholder = monthPlaceholder;
            } else {
                Log.i(TAG, ONLY_LETTERS_A_Z_A_Z_ARE_ALLOWED_FOR_A_PLACEHOLDER);
            }
        }

        public static void setDefaultYearPlaceholder(char yearPlaceholder) {
            if (validatePlaceholderChar(yearPlaceholder)) {
                DefaultSettings.yearPlaceholder = yearPlaceholder;
            } else {
                Log.i(TAG, ONLY_LETTERS_A_Z_A_Z_ARE_ALLOWED_FOR_A_PLACEHOLDER);
            }
        }

        public static void setDefaultDatePlaceholder(Character dayPlaceholder, Character
                monthPlaceholder, Character yearPlaceholder) {
            if (dayPlaceholder != null) {
                DefaultSettings.dayPlaceholder = dayPlaceholder;
            }
            if (monthPlaceholder != null) {
                DefaultSettings.monthPlaceholder = monthPlaceholder;
            }
            if (yearPlaceholder != null) {
                DefaultSettings.yearPlaceholder = yearPlaceholder;
            }
        }

        public static boolean validateSeparator(char separator) {
            return separator == DATE_SEPARATOR_DOT || separator == DATE_SEPARATOR_SLASH || separator == DATE_SEPARATOR_DASH;
        }

        public static boolean validatePlaceholderChar(Character character) {
            return character.toString().matches("[a-zA-Zа-яА-Я]");
        }

        public static boolean validateDateViewFormat(int dateViewFormat) {
            return dateViewFormat == DATE_VIEW_FORMAT_DMY || dateViewFormat == DATE_VIEW_FORMAT_YMD || dateViewFormat == DATE_VIEW_FORMAT_MDY;
        }
    }
}


