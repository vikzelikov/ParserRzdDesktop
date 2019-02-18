import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.*;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
		String fromCity = null; 
        String toCity = null; 
        String date = null;
		int maxPrice = 0;
	

        //������� ���������
		try{
			fromCity = args[0]; //������
			toCity = args[1]; //����
			date = args[2]; //����
		}catch(Exception ex){
			System.out.println("������� ��� ������������ ���������: ������, ����, ����!");
		}
        

        try {
            if(Integer.valueOf(args[3]) > 0)
                maxPrice = Integer.valueOf(args[3]);
        }catch (Exception ex){
            maxPrice = 999999;
        }

        Main main = new Main();

        String codeCityFrom = main.codeCity(fromCity);
        String codeCityTo = main.codeCity(toCity);
        main.start(fromCity, codeCityFrom, toCity, codeCityTo, date, maxPrice);
    }







    private void start(String fromCity, String codeCityFrom, String toCity, String codeCityTo, String date, int maxPrice){
        //��������� ��� �������
        fromCity = fromCity.toUpperCase();
        toCity = toCity.toUpperCase();


        //������ ������, �������� rid
        Connection.Response res = null;
        Document documentFirst = null;
        String jsonStringDocumentFirst = null;
        String linkFirst = "http://pass.rzd.ru/timetable/public/ru?STRUCTURE_ID=735&layer_id=5371&dir=0&tfl=3&checkSeats=0&st0=" + fromCity + "&code0=" + codeCityFrom + "&st1=" + toCity + "&code1=" + codeCityTo + "&dt0=" + date + "";
        try {
            res = Jsoup.connect(linkFirst).method(Connection.Method.GET).execute();
            documentFirst = res.parse();
            jsonStringDocumentFirst = documentFirst.body().text();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //������ �� ������� ������� RID � COOKIE
        Object obj = null;
        String rid = null;
        String sessionId = null;
        try {
            obj = new JSONParser().parse(jsonStringDocumentFirst);
            JSONObject ridJSON = (JSONObject) obj;
            rid = String.valueOf(ridJSON.get("rid")); //���������� ���� ������� REQUEST_ID
            if (res != null) {
                sessionId = res.cookie("JSESSIONID"); //������� cookie
            }
            Thread.sleep(2000);

        } catch (ParseException | InterruptedException e) {
            e.printStackTrace();
        }

        //������ ������, �������� ������
        String jsonStringDocumentSecond = null;
        String linkSecond = "https://pass.rzd.ru/timetable/public/ru?layer_id=5371&rid=" + rid + "";
        Document documentSecond = null;
        try {
            documentSecond = Jsoup.connect(linkSecond).cookie("JSESSIONID", sessionId).get();
            jsonStringDocumentSecond = documentSecond.body().text();
        } catch (IOException e) {
            e.printStackTrace();
        }






        //������� �� ������ ������� ������� ������ ������(������, ����� ����������� � ��)
        Object objectJsonSecondDocument = null;
        try {
            objectJsonSecondDocument = new JSONParser().parse(jsonStringDocumentSecond);
            JSONObject getObjectJson = (JSONObject) objectJsonSecondDocument;
            JSONArray tp = (JSONArray) getObjectJson.get("tp");
            Iterator tpIter = tp.iterator();

            while (tpIter.hasNext()) {
                JSONObject tpObj = (JSONObject) tpIter.next();
                JSONArray list = (JSONArray) tpObj.get("list");
                Iterator listIter = list.iterator();

                int countTrains = 1;
                while (listIter.hasNext()) {

                    JSONObject listObj = (JSONObject) listIter.next();
                    JSONArray cars = (JSONArray) listObj.get("cars");
                    Iterator carsIter = cars.iterator();

                    boolean start = false;

                    while (carsIter.hasNext()) {

                        JSONObject carsObj = (JSONObject) carsIter.next();
                        if (carsObj.get("typeLoc").equals("�������� ����")) continue;


                        if (Integer.parseInt((String)carsObj.get("tariff")) <= maxPrice) {
                            if (!start) {
                                System.out.println("����� " + countTrains + ":");
                                System.out.println("�����������: " + listObj.get("time0") + " " + listObj.get("date0"));
                                System.out.println("��������: " + listObj.get("time1") + " " + listObj.get("date1"));
                                System.out.println(listObj.get("station0") + " - " + listObj.get("station1"));
                                System.out.println("�������� ��������� ����: ");
                                countTrains++;
                                start = true;
                            }
                            System.out.println(carsObj.get("typeLoc") + ": " + carsObj.get("freeSeats") + " (�� " + carsObj.get("tariff") + " ���.)");
                        }

                    }
                    if (start)
                        System.out.println("\n\n");

                }
            }
        } catch (Exception e) {
            System.out.println("��� �������!");
        }

    }








    private String codeCity(String city) {
        String code = null;


        String cityRu = city.toUpperCase();
        String cityUnicode = null;
        try {
            cityUnicode = URLEncoder.encode(cityRu.toUpperCase(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



        URL url = null;
        HttpURLConnection connection = null;
        String line = null;
        String jsonString = null;
        StringBuilder sb = new StringBuilder();
        int HttpResult = 0;
        Object objectJsonCodeCityDocument = null;
        try {
            //�������� ���� ������� �� �������� ������
            url = new URL("http://www.rzd.ru/suggester?compactMode=y&stationNamePart=" + cityUnicode + "&lang=ru");
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");


            //�������� � ������ JSON
            HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                jsonString = sb.toString();
                br.close();
            }


            //�������� ������ ��� ��� �� ���������
            objectJsonCodeCityDocument = new JSONParser().parse(jsonString);
            JSONArray getObjectJsonCodeCity = (JSONArray) objectJsonCodeCityDocument;
            Iterator codeCityIter = getObjectJsonCodeCity.iterator();
            int count = 0;
            while (codeCityIter.hasNext()) {
                JSONObject cityCodeObj = (JSONObject) codeCityIter.next();
                if(cityCodeObj.get("ss")!=null){
                    try{
                        if((cityCodeObj.get("n").toString().substring(0, cityRu.length()).equals(cityRu))){
                            code =  String.valueOf(cityCodeObj.get("c"));
                            count++;
                        }
                    }catch (StringIndexOutOfBoundsException ex){
                        //ignore
                    }
                }
            }

            if(count==0){
                objectJsonCodeCityDocument = new JSONParser().parse(jsonString);
                JSONArray newJsonArray = (JSONArray) objectJsonCodeCityDocument;
                Iterator newCodeCity = newJsonArray.iterator();
                while(newCodeCity.hasNext()){
                    JSONObject cityCodeObj = (JSONObject) newCodeCity.next();
                    try{
                        if((cityCodeObj.get("n").toString().substring(0, cityRu.length()).equals(cityRu))){
                            code =  String.valueOf(cityCodeObj.get("c"));
                            break;
                        }
                    }catch (StringIndexOutOfBoundsException ex){
                        //ignore
                    }

                }
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return code;
    }



}