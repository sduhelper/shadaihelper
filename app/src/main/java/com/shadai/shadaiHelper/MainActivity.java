package com.shadai.shadaiHelper;

import static android.content.ContentValues.TAG;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    private static final int syear = 2023;
    private static final int smonth = 2;
    private static final int sday = 13;
    private static final int maxWeekNum = 18;
    private Uri xlsUri;
    ActivityResultLauncher<Intent> fileReturn = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                xlsUri = data.getData();
            }
        }
    });
    public static int countStrs(@NonNull String text, @NonNull String sub) {
        int index = 0, count = 0, length = sub.length();
        while ((index = text.indexOf(sub, index)) != -1) {
            index += length;
            count++;
        }
        return count;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button choose = findViewById(R.id.choose);
        choose.setOnClickListener(new ChooseButton());
        Button parse = findViewById(R.id.parse);
        parse.setOnClickListener(new ParseButton());
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button save = findViewById(R.id.save_button);
        save.setOnClickListener(new SaveButton());
    }

    public String getCell(@NonNull HSSFSheet sheet, int column, int row) {
        return sheet.getRow(row).getCell(column).getStringCellValue();
    }

    public List<List<List<Map<String, String>>>> getCalFromSheet(HSSFSheet sheet) {
        List<List<List<Map<String, String>>>> class_calender = new ArrayList<>();
        for (int w = 1; w < 8; w++) {
            List<List<Map<String, String>>> day = new ArrayList<>();
            for (int t = 3; t < 8; t++) {
                String classStr = getCell(sheet, w, t);
                List<String> big_class = multiClassDay(classStr);
                List<Map<String, String>> big_class_formated = new ArrayList<>();
                for (int i = 0; i < big_class.size(); i++) {
                    big_class_formated.add(getSingleClassDetail(big_class.get(i)));
                }
                day.add(big_class_formated);
            }
            class_calender.add(day);
        }
        return class_calender;
    }

    public Map<String, String> getSingleClassDetail(@NonNull String single_class) {
        Map<String, String> singleClassDetail = new HashMap<>();
        List<String> list = List.of("name", "num", "sd", "teacher", "time", "location");
        if (single_class != "noClassNow") {
            String[] strspl = single_class.split("\n");
            for (int i = 0; i < 6 && i < strspl.length - 1; i++) {
                singleClassDetail.put(list.get(i), strspl[i + 1]);
            }
        } else {
            singleClassDetail.put("noClassNow", "noClassNow");
        }
        return singleClassDetail;
    }

    public List<String> multiClassDay(String dayClasses) {
        int classCount = countStrs(dayClasses, "([周])");
        List<String> classes = new ArrayList<>();
        if (classCount > 1) {
            String[] strspl = dayClasses.split("\n");
            List<Integer> time_split_index = new ArrayList<>();
            for (int i = 0; i < strspl.length; i++) {
                if (strspl[i].contains("([周])")) {
                    if (i < strspl.length - 1) {
                        if (strspl[i + 1].contains("d")) {
                            time_split_index.add(i + 2);
                        } else {
                            time_split_index.add(i + 1);
                        }
                    }
                }
            }
            for (int i = 0; i < classCount; i++) {
                StringBuilder str_final = new StringBuilder();
                if (i == 0) {
                    for (int e = 0; e < time_split_index.get(i); e++) {
                        str_final.append(strspl[e]).append("\n");
                    }
                } else {
                    if (i < time_split_index.size()) {
                        for (int e = time_split_index.get(i - 1); e < time_split_index.get(i); e++) {
                            str_final.append(strspl[e]).append("\n");
                        }
                    } else {
                        for (int e = time_split_index.get(i - 1); e < strspl.length; e++) {
                            str_final.append(strspl[e]).append("\n");
                        }
                    }
                }
                classes.add(str_final.toString());
            }
        } else if (classCount == 1) {
            classes.add(dayClasses);
        } else {
            classes.add("noClassNow");
        }
        return classes;
    }

    @Nullable
    private List<List<Map>> singleWeekTable(int weekNum, List<List<List<Map<String, String>>>> cls) {
        List<List<List<List<Integer>>>> lowl = listOfWeekList(cls);
        List<List<Map>> swt = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            List<Map> sdt = new ArrayList<>();
            for (int b = 0; b < 5; b++) {
                int weekClassIndex = 0;
                Boolean hasClass = false;
                for (int i = 0; i < lowl.get(d).get(b).size(); i++) {
                    if (lowl.get(d).get(b).get(i).contains(weekNum)) {
                        weekClassIndex = i;
                        hasClass = true;
                        break;
                    }
                }
                if (hasClass) {
                    sdt.add(b, cls.get(d).get(b).get(weekClassIndex));
                } else {
                    Map<String, String> thisWeekNoClass = new HashMap<>();
                    thisWeekNoClass.put("noClassNow", "noClassNow");
                    sdt.add(b, thisWeekNoClass);
                }
            }
            swt.add(sdt);
        }
        return swt;
    }

    @NonNull
    private List<VEvent> generateSingleWeekEvent(int weekNum, List<List<Map>> swt) {
        List<VEvent> swe = new ArrayList<>();
        for (int d = 0; d < 7; d++) {
            for (int b = 0; b < 5; b++) {
                if (!swt.get(d).get(b).containsKey("noClassNow")) {
                    VEvent a = cls2event(weekNum, d, b, swt.get(d).get(b));
                    swe.add(a);
                }
            }
        }
        return swe;
    }

    @NonNull
    private List<VEvent> allEvents(List<List<List<Map<String, String>>>> cls) {
        List<VEvent> list_ = new ArrayList<>();
        for (int i = 1; i < maxWeekNum + 1; i++) {
            list_.addAll(generateSingleWeekEvent(i - 1, singleWeekTable(i, cls)));
        }
        return list_;
    }

    @NonNull
    private net.fortuna.ical4j.model.Calendar icsCalendar(@NonNull List<VEvent> list_) {
        net.fortuna.ical4j.model.Calendar icsc = new net.fortuna.ical4j.model.Calendar();
        icsc.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//CN"));
        icsc.getProperties().add(Version.VERSION_2_0);
        icsc.getProperties().add(CalScale.GREGORIAN);
        for (int i = 0; i < list_.size(); i++) {
            icsc.getComponents().add(list_.get(i));
        }
        return icsc;
    }
    private static final int CREATE_FILE = 1;
    private Intent createFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/calendar");
        intent.putExtra(Intent.EXTRA_TITLE, "a");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        return intent;
    }
    private void writeFile(net.fortuna.ical4j.model.Calendar cld) throws IOException {
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(xlsUri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            CalendarOutputter out = new CalendarOutputter();
            out.output(cld, fileOutputStream);
            fileOutputStream.close();
            pfd.close();
            Toast.makeText(MainActivity.this, "Save Success! Find file at: "+xlsUri, Toast.LENGTH_SHORT).show();
        } catch (java.lang.SecurityException e) {
            Toast.makeText(MainActivity.this, "Please select path again: "+ e, Toast.LENGTH_SHORT).show();
        } catch (java.lang.NullPointerException err) {
            Toast.makeText(MainActivity.this, "Please click CHOOSE to choose file again: "+ err, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private List<List<List<List<Integer>>>> listOfWeekList(List<List<List<Map<String, String>>>> calmap) {
        List<List<List<List<Integer>>>> lowl = new ArrayList<>();
        for (int dcoint = 0; dcoint < 7; dcoint++) {
            List<List<List<Integer>>> day = new ArrayList<>();
            for (int bcount = 0; bcount < 5; bcount++) {
                List<List<Integer>> big_classes = new ArrayList<>();
                for (int ccount = 0; ccount < calmap.get(dcoint).get(bcount).size(); ccount++) {
                    List<Integer> list_;
                    if (!calmap.get(dcoint).get(bcount).get(0).containsKey("noClassNow")) {
                        list_ = weekList(calmap.get(dcoint).get(bcount).get(ccount).get("time"));
                    } else {
                        list_ = List.of(-1);
                    }
                    big_classes.add(ccount, list_);
                }
                day.add(bcount, big_classes);
            }
            lowl.add(dcoint, day);
        }
        return lowl;
    }

    @Nullable
    private List<Integer> weekList(@NonNull String weeks) {
        String[] splitedString = weeks.split("\\(\\[周\\]\\)");
        String weeks_str = splitedString[0];
        String[] week_str0;
        if (weeks_str.contains(",") && !weeks_str.contains("-")) {
            week_str0 = weeks_str.split("周,第");
            List<Integer> weeks_ = new ArrayList<>();
            weeks_.add(Integer.valueOf(week_str0[0].replace("第", "")));
            for (int r = 1; r < week_str0.length - 1; r++) {
                weeks_.add(Integer.valueOf(week_str0[r]));
            }
            weeks_.add(Integer.valueOf(week_str0[week_str0.length - 1].replace("周", "")));
            return weeks_;
        } else if (weeks_str.contains("-") && !weeks_str.contains(",")) {
            week_str0 = weeks_str.split("-");
            String s = week_str0[0].replace("第", "");
            String e = week_str0[1].replace("周", "");
            int start_week = Integer.parseInt(s);
            int end_week = Integer.parseInt(e);
            List<Integer> weeks_ = IntStream.range(start_week, end_week + 1).boxed().collect(Collectors.toList());
            return weeks_;
        } else if (weeks_str.contains("-") && weeks_str.contains(",")){
            week_str0 = weeks_str.split("周,第");
            List<String> strGroup = new ArrayList<>();
            List<Integer> weeks_ = new ArrayList<>();
            for (int i = 0; i < week_str0.length; i++) {
                if (i == 0) {
                    strGroup.add(week_str0[i].replace("第", ""));
                } else if (i == week_str0.length - 1) {
                    strGroup.add(week_str0[i].replace("周", ""));
                } else {
                    strGroup.add(week_str0[i]);
                }
            }
            for (int i = 0; i < strGroup.size(); i++) {
                String[] wks = strGroup.get(i).split("-");
                int start_week = Integer.parseInt(wks[0]);
                int end_week = Integer.parseInt(wks[1]);
                List<Integer> duration = IntStream.range(start_week, end_week + 1).boxed().collect(Collectors.toList());
                for (int e = 0; e < duration.size(); e++) {
                    weeks_.add(duration.get(e));
                }
            }
            return weeks_;
        } else {
            List<Integer> weeks_ = new ArrayList<>();
            Integer i = Integer.parseInt(weeks_str.replace("周", "").replace("第", ""));
            weeks_.add(i);
            return weeks_;
        }
    }

    @NonNull
    private List<Integer> weekDay2Date(int weekNum, int weekDay) {
        List<Integer> date = new ArrayList<>();
        LocalDate newDate = LocalDate.of(syear, smonth, sday).plusDays(weekNum * 7 + weekDay);
        date.add(newDate.getYear());
        date.add(newDate.getMonthValue());
        date.add(newDate.getDayOfMonth());
        return date;
    }

    @NonNull
    private VEvent cls2event(int weekNum, int weekDay, int bigClassNum, Map<String, String> clsDetail) {
        int hasPermission = checkSelfPermission(Manifest.permission.INTERNET);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 123);
        }

        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone("Asia/Shanghai");
        VTimeZone tz = timezone.getVTimeZone();
        List<Integer> date = weekDay2Date(weekNum, weekDay);
        int shour, smin, ehour, emin;
        if (bigClassNum == 0) {
            shour = 8;
            smin = 0;
            ehour = 9;
            emin = 50;
        } else if (bigClassNum == 1) {
            shour = 10;
            smin = 10;
            ehour = 12;
            emin = 0;
        } else if (bigClassNum == 2) {
            shour = 14;
            smin = 0;
            ehour = 15;
            emin = 50;
        } else if (bigClassNum == 3) {
            shour = 16;
            smin = 10;
            ehour = 18;
            emin = 0;
        } else {
            shour = 19;
            smin = 0;
            ehour = 20;
            emin = 50;
        }
        java.util.Calendar startDate = new GregorianCalendar();
        startDate.setTimeZone(timezone);
        startDate.set(syear, date.get(1) - 1, date.get(2), shour, smin);

        java.util.Calendar endDate = new GregorianCalendar();
        endDate.setTimeZone(timezone);
        endDate.set(syear, date.get(1) - 1, date.get(2), ehour, emin);

        DateTime start = new DateTime(startDate.getTime());
        DateTime end = new DateTime(endDate.getTime());
        VEvent meeting = new VEvent(start, end, clsDetail.get("name"));
        meeting.getProperties().add(tz.getTimeZoneId());
        meeting.getProperties().add(new Description("class_number: " + clsDetail.get("num") + "; class_sd_number: " + clsDetail.get("sd") + "; teacher: " + clsDetail.get("teacher") + "; time: " + clsDetail.get("time")));
        RandomUidGenerator ug = new RandomUidGenerator();
        Uid uid = ug.generateUid();
        meeting.getProperties().add(uid);
        if (clsDetail.containsKey("location")) {
            meeting.getProperties().add(new Location(clsDetail.get("location")));
        }
        return meeting;
    }

    class SaveButton implements View.OnClickListener {
        @Override
        public void onClick (View v) {
            try {
                writeFile(cal);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class ChooseButton implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.ms-excel");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            fileReturn.launch(intent);
            Toast.makeText(MainActivity.this, "Choose the .xls file downloaded from school website!", Toast.LENGTH_SHORT).show();
        }
    }
    net.fortuna.ical4j.model.Calendar cal;
    class ParseButton implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int hasPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                return;
            }
            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
                return;
            }
            try {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(xlsUri);
                    HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                    HSSFSheet sheet = workbook.getSheetAt(0);
                    List<List<List<Map<String, String>>>> calmap = getCalFromSheet(sheet);
                    Toast.makeText(MainActivity.this, "Please choose file save location", Toast.LENGTH_SHORT).show();
                    Thread thread = new Thread(() -> {
                        try {
                            List<VEvent> evts = allEvents(calmap);
                            cal = icsCalendar(evts);
                            fileReturn.launch(createFile(xlsUri));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    thread.start();
                } catch (java.lang.NullPointerException er) {
                    Toast.makeText(MainActivity.this, "Please CHOOSE again: "+ er, Toast.LENGTH_SHORT).show();
                } catch (java.lang.RuntimeException runtime) {
                    Toast.makeText(MainActivity.this, "Please CHOOSE again: "+ runtime, Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}