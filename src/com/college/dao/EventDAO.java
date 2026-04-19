package com.college.dao;

import com.college.models.Event;
import com.college.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public List<Event> getEventsForMonth(YearMonth month) throws SQLException {
        LocalDate start = month.atDay(1);
        LocalDate endExclusive = month.plusMonths(1).atDay(1);

        String sql = "SELECT event_id, title, description, event_type, event_date, start_time, end_time, location " +
                "FROM events WHERE event_date >= ? AND event_date < ? ORDER BY event_date ASC, start_time ASC";

        List<Event> events = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(endExclusive));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Event ev = new Event();
                    ev.setEventId(rs.getInt("event_id"));
                    ev.setTitle(rs.getString("title"));
                    ev.setDescription(rs.getString("description"));
                    ev.setEventType(rs.getString("event_type"));

                    Date eventDate = rs.getDate("event_date");
                    if (eventDate != null) {
                        ev.setEventDate(eventDate.toLocalDate());
                    }

                    Time startTime = rs.getTime("start_time");
                    if (startTime != null) {
                        ev.setStartTime(startTime.toLocalTime());
                    }

                    Time endTime = rs.getTime("end_time");
                    if (endTime != null) {
                        ev.setEndTime(endTime.toLocalTime());
                    }

                    ev.setLocation(rs.getString("location"));
                    events.add(ev);
                }
            }
        }

        return events;
    }
}
