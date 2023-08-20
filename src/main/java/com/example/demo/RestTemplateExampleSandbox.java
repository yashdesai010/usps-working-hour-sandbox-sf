package com.example.demo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import com.force.api.ApiConfig;
import com.force.api.ApiSession;
import com.force.api.ForceApi;

/**
 * RestTemplateExample Sandbox
 */
public class RestTemplateExampleSandbox {

    public static JSONArray fetchUSPSCodes(String firstPart, String secondPart) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String Requestbody = convertCsvToJson(firstPart, secondPart);
        RequestBody body = RequestBody.create(Requestbody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://tools.usps.com/UspsToolsRestServices/rest/POLocator/findLocations")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        int statusCode = response.code();
        String responseBody = response.body().string();
        System.out.println("Response Code: " + statusCode);
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(responseBody);
        JSONArray locationsArray = (JSONArray) json.get("locations");
        for (Object entry : locationsArray) {
            JSONObject locationObject = (JSONObject) entry;
            if (locationObject.containsKey("zip5") && locationObject.containsKey("zip4")) {
                String zip5 = (String) locationObject.get("zip5"); 
                String zip4 = (String) locationObject.get("zip4");
                if (zip5.equals(firstPart) && zip4.equals(secondPart)) {
                    JSONArray locationServiceHoursArray = (JSONArray) locationObject.get("locationServiceHours");
                        return locationServiceHoursArray;
                    
                }
            }
            break;
        }
        return null;
    }

    private static String convertCsvToJson(String firstPart, String secondPart) throws IOException {
        return "{ \"requestZipCode\": " + firstPart + ", \"requestZipPlusFour\":" + secondPart + "}";
    }


public static void salesforcefetch(){    
    String accountid;
        ForceApi api = new ForceApi(new ApiConfig()
                .setForceURL("https://test.salesforce.com/services/oauth2/token")
                .setUsername("msami19971@gmail.com.tirthdev")
                .setPassword("Tirthpatel2")
                .setClientId("3MVG959Nd8JMmavQkzQV5w5FoV07nsXqXQxOq9tuwFWyyAIt4e5HBbCYZ.eavivE29PEVEuBnO.knucmWt1bk")
                .setClientSecret("5D001DDE640EC92CA2C6B7FC54B2E36256A7F786CFB8B8B46F07E0934BF98DD8"));
                
        ApiSession session = api.getSession();
        String accessToken = session.getAccessToken();
        
        System.out.println("ACCESS_TOKEN : " + accessToken);
        List<Map> result = api.query("SELECT Id,Lessor_Zip_Code__c from Account where Lessor_Zip_Code__c!=null").getRecords();
        for (Map<String, Object> record : result) {
            String zipcode = (String) record.get("Lessor_Zip_Code__c");
            accountid = (String) record.get("Id");
            String[] zips = zipcode.split("-");
            String firstPart = null, secondPart = null;
            if (zips.length >= 1) {
                firstPart = zips[0].trim();
            }
            if (zips.length >= 2) {
                secondPart = zips[1].trim();
            }
            if (firstPart != null && secondPart != null) {
                try {
                    JSONArray locationServiceHoursArray = fetchUSPSCodes(firstPart, secondPart);
                    if (locationServiceHoursArray != null && locationServiceHoursArray.size() > 0) {
                        JSONObject locationObject = (JSONObject) locationServiceHoursArray.get(0);


                                JSONArray locationHoursArray1 = (JSONArray) locationObject.get("dailyHoursList");
                                if (locationHoursArray1 != null) {
                                    for (Object locationHourObj : locationHoursArray1) {
                                        JSONObject locationHour1 = (JSONObject) locationHourObj;
                                        String dayOfTheWeek = (String) locationHour1.get("dayOfTheWeek");
                                        JSONArray timesArray = (JSONArray) locationHour1.get("times");

                                        if (dayOfTheWeek != null && timesArray != null && timesArray.size() > 0) {
                                            JSONObject timesObject = (JSONObject) timesArray.get(0);
                                            String openTime = (String) timesObject.get("open");
                                            String closeTime = (String) timesObject.get("close");

                                            Map<String, Object> accountData2 = new HashMap<>();

                                            switch (dayOfTheWeek) {
                                                case "MO":
                                                    accountData2.put("Monday_Working_Hours__c", openTime + "-" + closeTime);
                                                    break;
                                                case "TU":
                                                    accountData2.put("Tuesday_Working_Hours__c", openTime + "-" + closeTime);
                                                    break;
                                                case "WE":
                                                    accountData2.put("Wednesday_Working_Hours__c", openTime + "-" + closeTime);
                                                    break;
                                                case "TH":
                                                    accountData2.put("Thursday_Working_Hours__c", openTime + "-" + closeTime);
                                                    break;
                                                case "FR":
                                                    accountData2.put("Friday_Working_Hours__c", openTime + "-" + closeTime);
                                                    break;
                                                case "SA":
                                                    accountData2.put("Saturday_Working_Hours__c", openTime + "-" + closeTime);
                                                    break;
                                                case "SU":
                                                    break;
                                                default:
                                                    break;
                                            }
                                            api.updateSObject("Account", accountid, accountData2);
                                        }
                                    }
                                }
                            }
                        
                    }
                

                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        salesforcefetch();
    }
}

    

