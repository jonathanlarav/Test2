package com.nearsoft.dao;

import com.nearsoft.bean.Airport;

import java.util.List;

public interface AirportDAO {

    Airport findById(Long id);
    List<Airport> autoComplete(String part);
    List<Airport> findAll();
}
