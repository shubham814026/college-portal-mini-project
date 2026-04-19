package com.college.service;

import com.college.dao.EventDAO;
import com.college.models.Event;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;

public class EventService {
    private final EventDAO eventDAO = new EventDAO();

    public List<Event> getEventsForMonth(YearMonth month) throws SQLException {
        return eventDAO.getEventsForMonth(month);
    }
}
