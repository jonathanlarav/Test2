package com.nearsoft.web.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Created by slopez on 2/28/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"classpath:mvc-dispatcher-servlet.xml", "classpath:applicationContext.xml"})
public class SiteControllerTest {

    SiteController siteController;
    private MockMvc mockmvc;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setUp() throws Exception {
        this.mockmvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() throws Exception {
        siteController = null;
    }

    //only method 'welcome' with path '/'
    @Test(expected = IllegalArgumentException.class)
    public void testWelcomeWithInvalidURL() throws Exception {
        this.mockmvc.perform(get("").accept(MediaType.parseMediaType("application/json")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWelcomeWithNullURL() throws Exception {
        this.mockmvc.perform(get(null)).andExpect(status().is4xxClientError());
    }

    @Test
    public void testWelcomeWithInvalidMediaType() throws Exception {
        this.mockmvc.perform(get("/").accept(MediaType.parseMediaType("text/plain")))
                .andExpect(status().isOk());
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void testWelcomehWithNullMediaType() throws Exception {
        this.mockmvc.perform(get("/").accept(MediaType.parseMediaType(null)));
    }

    @Test
    public void testWelcomeCorrectly() throws Exception {
        this.mockmvc.perform(get("/").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk()).andExpect(forwardedUrl("/WEB-INF/pages/index.jsp"));
    }


    //only method 'airports' with path '/airports'
    @Test
    public void testAirportshWithInvalidURL() throws Exception {
        this.mockmvc.perform(get("airports").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is4xxClientError());

        this.mockmvc.perform(get("dfisjfoidfj").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is4xxClientError());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAirportshWithNullURL() throws Exception {
        this.mockmvc.perform(get(null).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testAirportshWithInvalidMediaType() throws Exception {
        this.mockmvc.perform(get("/airports").accept(MediaType.parseMediaType("text/plain")))
                .andExpect(status().is4xxClientError());
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void testAirportshWithNullMediaType() throws Exception {
        this.mockmvc.perform(get("/airports").accept(MediaType.parseMediaType(null)));
    }

    @Test
    public void testAirportshCorrectly() throws Exception {
        //test everything is alright / correct url, correct media type to accept, correct content type
        this.mockmvc.perform(get("/airports").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
        //you cannot ask for an accurate answer because the results came from DB so it must
        //work for you to ask for the content-type
        //and tell us that whether the answer can be empty or not it is a JSON string anyway

        this.mockmvc.perform(get("/airports/").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }


    //only test for 'search' and path '/flights'
    @Test
    public void testSearchWithInvalidURL() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.mockmvc.perform(get("/flights?from=HMO&startDate=" + sdf.format(getDate(1)) + "&endDate=" +
                sdf.format(getDate(2)) + "&type=oneWay").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().is4xxClientError());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchWithNullURL() throws Exception {
        this.mockmvc.perform(get(null).accept(MediaType.parseMediaType("application/json")));
    }

    @Test
    public void testsearchWithInvalidMediaType() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.mockmvc.perform(get("/flights?from=HMO&to=MEX&startDate=" + sdf.format(getDate(1)) + "&endDate=" +
                sdf.format(getDate(2)) + "&type=oneWay").accept(MediaType.parseMediaType("text/plain")))
                .andExpect(status().isOk());
    }

    @Test(expected = InvalidMediaTypeException.class)
    public void testSearchhWithNullMediaType() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.mockmvc.perform(get("/flights?from=HMO&to=MEX&startDate=" + sdf.format(getDate(1)) + "&endDate=" +
                sdf.format(getDate(2)) + "&type=oneWay").accept(MediaType.parseMediaType(null)));
    }


    @Test
    public void testSearchCorrectly() throws Exception {
        //test everything is alright / correct url, correct media type to accept, correct content type
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String query = "/flights?from=HMO&to=MEX&startDate=" + sdf.format(getDate(1)) + "&endDate=" +
                sdf.format(getDate(2)) + "&type=oneWay";
        this.mockmvc.perform(get(query).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk());
        //you cannot ask for an accurate answer because the results came from API so it must
        //work for you to ask for the content-type
        //and tell us that whether the answer can be empty or not it is a JSON string anyway
    }

    /**
     * Gets a date given the lap
     * @param lap the lap of time to consider to create a date; 1 for a week, 2 for a day and a week
     * @return a date object with tha represents a date in the future
     */
    private Date getDate(int lap) {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        switch (lap) {
            case 1 :
                //in one week
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case 2 :
                //tomorrow plus 1 week
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                break;
        }
        return cal.getTime();
    }

}