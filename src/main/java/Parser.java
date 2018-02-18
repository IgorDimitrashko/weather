import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private static String [][] wt;
    private static String date = "";


    private static Document getPage() throws IOException {
//        String url = "https://www.gismeteo.ua/weather-odessa-4982/legacy/";
//        String url = "https://www.gismeteo.ua/weather-lviv-4949/legacy/";
        String url = "https://www.gismeteo.ua/weather-istanbul-3719/legacy/";
        Document page = Jsoup.parse(new URL(url), 3000);
        return page;
    }

    //25.12 Понедельник погода сегодня
    //25.12  регулярные выражения
    //\d{2}\.\d{2}
    private static String getDataFromString(String stringData, String stringPattern) throws Exception {
        Pattern pattern = Pattern.compile(stringPattern);

        Matcher matcher = pattern.matcher(stringData);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new Exception("Can't extract date from string!");
    }

    // date pattern = "[0-9]{1,2} [А-Яа-я]{2,10}, [А-Яа-я]{2,11}";

    private static int printPartValues(Elements values, int index) {
        int iterationCount = 4;
        if (index == 0) {             // по времени суток
            Element valueLn = values.get(3);
            boolean isMorning = valueLn.text().contains("Утро");
            if (isMorning) {
                iterationCount = 3;
            }
        }
        for (int i = 0; i < iterationCount; i++) {
            Element valueLine = values.get(index + i);
            for (Element td : valueLine.select("td")) {
                System.out.print(td.text() + "    ");
            }
            System.out.println();
        }
        return iterationCount;
    }

    public static void main(String[] args) throws Exception {
        wt = new String[7][13];
        Document page = getPage();

        Element tableWth = page.select("table").first();
        Elements dates = tableWth.select("th[colspan=4]"); // даты дней недели для прогноза (их 3)
        Elements rows = tableWth.select("tr");

        // извлекаем даты
        date = "";
        for (Element d : dates)
            date += "\t\t\t" + d.text();

        // извлекаем температуру и темп. по ощущениям
        int i = 0;
        int r = 2;
        Elements temperatures = tableWth.select("span[class=value m_temp c]");
        for (Element t : temperatures)
        {
            wt[r][i++] = t.text();
            if (i > 12)
            {
                r = 6;
                i = 0;
            }
        }

        // извлекаем давление
        i = 0;
        r = 3;
        Elements pressures = tableWth.select("span[class=value m_press torr]");
        for (Element p : pressures)
            wt[r][i++] = p.text();


        // информация об осадках
        Elements perspAll = tableWth.select("tr[class=persp]"); // дни недели, их 5
        Elements persp = perspAll.select("td");
        i = 0;
        for (Element p:persp)
            wt[1][i++] = getDataFromString(p.toString(), "[rs][0-4]\\.gif");

        // информация об Облачности
        Elements cloudAll = tableWth.select("tr[class=cloudness]"); // дни недели, их 5
        Elements clouds = cloudAll.select("td");
        i = 0;
        for (Element c:clouds)
            wt[0][i++] = getDataFromString(c.toString(), "c[0-4]\\.gif");


        // информация о Влажности
        Element trVl = rows.get(6);
        Elements vls = trVl.select("td");
        i = 0;
        for (Element v:vls)
            wt[4][i++] = getDataFromString(v.toString(), "[0-9]{1,3}");


        // извлекаем Ветер
        i = 0;
        r = 5;
        Element trWinds = rows.get(7);
        Elements winds = trWinds.select("dt");
        Elements windPowers = trWinds.select("span[class=value m_wind ms]");
        for (Element w : winds)
            wt[r][i++] = w.text();
        i = 0;
        for (Element wp: windPowers)
            wt[r][i++] += "(" + wp.text() + ")";


        // Печать информации о прогнозе погоды
        printWeather();

    }

    public static void printWeather()
    {
        String [] header = {"Облачность:\t","Осадки:\t\t","Температура:\t","Давление:\t","Влажность:\t","Ветер:\t\t","Ощущение:\t"};
        String s, persp, cloud = "";
        System.out.println(date);
        for (int i = 0; i < 7; i++) {
            System.out.print(header[i]);
            for (int j = 0; j < 13; j++)
            {
                s = wt[i][j];


                // Вывод инф-ции об ооблачности
                if (i==0)
                {
                    switch (s.charAt(1))
                    {
                        case '1' : cloud = "малооблачно"; break;
                        case '2' : cloud = "облачно"; break;
                        case '3' : case '4' : cloud = "сильная облачность"; break;
                        default: cloud = "ясно";
                    }
                    s = cloud;
                }
                // конец вывода информации об осадках
                else
                // Вывод инф-ции об осадках
                if (i==1)
                {
                    if (s.charAt(0)=='r')
                        persp = "Дождь";
                    else
                        persp = "Снег";

                    switch (s.charAt(1))
                    {
                        case '1' : persp += " (слабый)"; break;
                        case '2' : persp += ""; break;
                        case '3' : persp += " (сильный)"; break;
                        case '4' : persp += " (ливневый)"; break;
                        default: persp="";
                    }
                    s = persp;
                }
                // конец вывода информации об осадках

                System.out.print("  " + (s == null?"":s));
            }
            System.out.println();
        }
    }
}
