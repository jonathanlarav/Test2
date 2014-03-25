package com.nearsoft.service.impl;

import com.google.gson.JsonParseException;
import com.nearsoft.bean.Flight;
import com.nearsoft.service.APIService;
import com.nearsoft.utils.APIConnection;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by slopez on 3/4/14.
 */
public class DespegarAPIService implements APIService {

    private static String AUTOCOMPLETE_URL = " http://api.despegar.com/autocomplete/";
    private static String FLIGHTS_URL = "http://api.despegar.com/availability/flights/";
    private static String HOTELS_URL = "http://api.despegar.com/availability/hotels/";
    private static String CARS_URL = "http://api.despegar.com/";

    private static final int TOMORROW = 1;
    private static final int NEXT_WEEK = 2;
    private static final int NEXT_MONTH = 3;

    @Override
    /**
     * Auto complete takes a part of a string and retrieves flights, countries, airports, etc that match with the
     * given text. "Despegar API" removes accents and even some symbols
     * @param options <code>options[0]</code> MUST BE a string to search for auto complete. If cannot happen it will return empty string
     */
    public Object autoComplete(Object... options) {
        //example: http://api.despegar.com/autocomplete/mexi means get all the occurrences tha contains the word 'mexi'
        try {
            return APIConnection.callAPI( AUTOCOMPLETE_URL + options[0] );
        } catch (ConnectException e) {
            return "";
        }
    }

    @Override
    /**
     * Get flights that match with the given options
     * @param options <code>options[0]</code> must be the source place in three-letter fashion. Follow international conventions
     * <code>options[1]</code> must be the destiny place in three-letter fashion. Follow international conventions
     * <code>options[2]</code> OPTIONAL must be the departure time as a date object. Default 'today'
     * <code>options[3]</code> OPTIONAL must be the number of adults. Default 1
     * <code>options[4]</code> OPTIONAL must be the number of children. Default 0
     * <code>options[5]</code> OPTIONAL must be the number of infants (babies). Default 0
     * <code>options[6]</code> OPTIONAL must be the type of flight (one way, round trip). Default 'one way'
     */
    public Object getFlights(Object... options) {
        //example: http://api.despegar.com/availability/flights/oneWay/HMO/LON/2014-05-25/1/0/0
        //means a flight between HMO and LON in 2014-05-25, one adult, zero children and zero infants
        List<Flight> results = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String query = FLIGHTS_URL +
                (options[6] == null ? "oneWay" : options[6]) + "/" +
                (options[0].toString().isEmpty() ? "HMO" : options[0].toString()) + "/" +
                (options[1].toString().isEmpty() ? "MEX" : options[1].toString()) + "/" +
                (options[2] == null ? sdf.format(getDate(DespegarAPIService.TOMORROW)) : sdf.format( (Date) options[2]) ) + "/" +
                (options[3] == null ? 1 : options[3]) + "/" +
                (options[4] == null ? 0 : options[4]) + "/" +
                (options[5] == null ? 0 : options[5]);
        try {
            /*ResponseEntity entityResponse = rt.getForEntity("http://api.despegar.com/availability/flights/oneWay/HMO/LON/2014-05-25/1/0/0");
            System.out.println(entityResponse.getBody().toString());*/
//            System.out.println(rt.getForObject("http://api.despegar.com/availability/flights/oneWay/HMO/LON/2014-05-25/1/0/0", Object.class));
//            return processFlightsAPIResponse(APIConnection.callAPI(query));
            ObjectMapper mapper = new ObjectMapper();
            Map apiResponse =  mapper.readValue( APIConnection.callAPI("http://api.despegar.com/availability/flights/oneWay/HMO/LON/2014-05-25/1/0/0?currency=USD").toString(), Map.class);
            System.out.println("*********meta.flights*********");
            List<Map> flights = (List<Map>) apiResponse.get("flights");//X1
            int price = 0;
            String estimatedTimeTravel = "";
            List<Map> outboundRoutes = null;
            List<Map<String, Map>> segments = null;
            String estimatedDate1 = "";
            String estimatedDate2 = "";
            String stops = "";
            Set<String> companies = new HashSet<>();
            Set<String> airports = new HashSet<>();
            Set<String> scales = new HashSet<>();
                System.out.println("number of flights found " + flights.size());
                for(Map flight : flights) {
                    price = (int) ( ( (Map) ( (Map) flight.get("priceInfo") ).get("total") ).get("fare")); //priceinfo X2
                    outboundRoutes = (List<Map>) flight.get("outboundRoutes"); //outboundRoutes X2
                    for(Map route : outboundRoutes) {
                        estimatedTimeTravel = (String) route.get("duration");
                        segments =  (List<Map<String, Map>>) route.get("segments"); //Segments X3
                        estimatedDate1 = (String) segments.get(0).get("departure").get("date");
                        estimatedDate2 = (String) segments.get( segments.size() - 1 ).get("arrival").get("date");
                        stops = segments.size() == 1 ? "non-stop" : segments.size() + " stops";
                        for(Map segment : segments) {
                            companies.add((String) segment.get("marketingCarrierDescription"));
                            airports.add((String) ((Map) segment.get("arrival") ).get("location"));
                            airports.add((String) ((Map) segment.get("departure")).get("location"));
                            scales.add((String) ((Map) segment.get("arrival") ).get("location"));
                        }
                    }
                    System.out.println("$" + price + " " + formatISODate(estimatedDate1) + " " + companies.toString() + " " +
                            estimatedTimeTravel + " " + airports.toString() + " " + scales.toString() + "");
                    results.add(new Flight(0L, price + "", formatISODate(estimatedDate1), formatISODate(estimatedDate2), companies.toString(),
                            estimatedTimeTravel, airports.toString(), stops, scales.size() > 1 ? scales.toString() : ""));
                    companies = new HashSet<>();
                    airports = new HashSet<>();
                    scales = new HashSet<>();
                }
            return results;
        } catch (NullPointerException | IndexOutOfBoundsException | JsonParseException | IOException e) {
            return null;
        }
    }

    @Override
    /**
     * Hotels from api.despegar.com
     * @param options <code>options[0]</code> must be the latitude
     * <code>options[1]</code> must be the longitude
     * <code>options[2]</code> OPTIONAL must be the check in date. Default 'today'
     * <code>options[3]</code> OPTIONAL must be the check out date. Default 'today'
     * <code>options[4]</code> OPTIONAL must be a string that describe the number of adults, children, infants, etc. Default 1 adult
     */
    public Object getHotels(Object... options) {
        //example: http://api.despegar.com/availability/hotels/27.2833333/-108.05000000000001?checkin=2014-05-25&checkout=2014-05-30&distribution=1
        //means looking for hotels in "Cerocahui, Chihuahua, México" (latitude 27.2833333, longitude -108.05000000000001)
        //checking in 2014-05-25
        //checking out 2014-05-30
        //distribution 1   TO-DO change it as soon as possible to return something really useful. Check http://api.despegar.com/docs/page/distribution
        String query = HOTELS_URL +
                options[0] + "/" +
                options[1] + "?" +
                "checkin=" + (options[2] == null ? new Date() : (Date) options[2]) + "&" +
                "checkout=" + (options[3] == null ? new Date() : (Date) options[3]) + "&" +
                "distribution=" + (options[4] == null ? 1 : options[4]);
        try {
            return APIConnection.callAPI( query );
        } catch (ConnectException e) {
            return "";
        }
    }

    @Override
    public Object getCars(Object... options) {
        throw new UnsupportedOperationException();
    }

    private List<Flight> processFlightsAPIResponse(Object apiResponse) {
        //mock objects, get the real ones soon enough
        Flight mockFlight0 = new Flight(0L, "780", "8:45 am", "10:45 am", "Aeromexico/Delta", "11h 00M", "HMO, JFK", "1 stop", "3h 30m in MEX");
        Flight mockFlight1 = new Flight(1L, "880", "10:00 am", "11:00 am", "Aeromexico/Air Canada", "16h 00M", "HMO,PHX,JFK", "2 stops", "2h 50m in MEX");
        Flight mockFlight2 = new Flight(2L, "890", "9:30 am", "12:50 am", "Aeromexico/Delta", "10h 00M", "HMO, JFK", "1 stop", "2h 30m in HMO");
        Flight mockFlight3 = new Flight(3L, "900", "12:50 am", "10:45 am", "Aeromexico/Air Canada", "11h 00M", "HMO,PHX,JFK", "1 stop", "1h 00m in WAS");
        Flight mockFlight4 = new Flight(4L, "1100", "15:10 am", "9:25 am", "Aeromexico/Delta", "9h 00M", "HMO, JFK", "non-stop", "");
        Flight mockFlight5 = new Flight(5L, "1234", "8:30 am", "11:10 am", "Aeromexico/Air Canada", "16h 00M", "HMO,PHX,JFK", "non-stop", "");
        Flight mockFlight6 = new Flight(6L, "1345", "7:20 am", "10:00 am", "Aeromexico/Delta", "12h 00M", "HMO, JFK", "1 stop", "4h 00m in MEX");
        Flight mockFlight7 = new Flight(7L, "1781", "6:45 am", "13:10 am", "Aeromexico/Air Canada", "11h 00M", "HMO,PHX,JFK", "1 stop", "2h 30m in PHX");
        Flight mockFlight8 = new Flight(8L, "2100", "6:50 am", "14:25 am", "Aeromexico/Delta", "10h 00M", "HMO, JFK", "1 stop", "1h 50m in MEX");
        Flight mockFlight9 = new Flight(9L, "2100", "8:45 am", "8:30 am", "Aeromexico/Air Canada", "6h 30M", "HMO,PHX,JFK", "non-stop", "");

        List<Flight> results = new ArrayList<>();
        results.add(mockFlight0);
        results.add(mockFlight1);
        results.add(mockFlight2);
        results.add(mockFlight3);
        results.add(mockFlight4);
        results.add(mockFlight5);
        results.add(mockFlight6);
        results.add(mockFlight7);
        results.add(mockFlight8);
        results.add(mockFlight9);

        return results;
    }

    private Date getDate(int lap) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        switch (lap) {
            case DespegarAPIService.TOMORROW :
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case DespegarAPIService.NEXT_WEEK :
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case DespegarAPIService.NEXT_MONTH :
                cal.add(Calendar.MONTH, 1);
                break;
        }
        return cal.getTime();
    }

    private String formatISODate (String isoDate) {
        System.out.println("DATE TO FORMAT:" + isoDate);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(isoDate);
        return  sdf.format(calendar.getTime());
    }

}