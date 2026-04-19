package com.college.servlets;

import com.college.models.Event;
import com.college.service.EventService;
import com.college.utils.ServletResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/events")
public class EventServlet extends BaseServlet {
    private final EventService eventService = new EventService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String monthParam = req.getParameter("month");
        YearMonth month;

        try {
            month = (monthParam == null || monthParam.trim().isEmpty())
                    ? YearMonth.now()
                    : YearMonth.parse(monthParam.trim());
        } catch (DateTimeParseException e) {
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"error\":\"Invalid month format. Use yyyy-MM.\"}");
            return;
        }

        List<Event> events;
        try {
            events = eventService.getEventsForMonth(month);
        } catch (Exception e) {
            ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"error\":\"Could not load events.\"}");
            return;
        }

        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < events.size(); i++) {
            Event ev = events.get(i);
            if (i > 0) {
                json.append(",");
            }

            json.append("{");
            json.append("\"eventId\":").append(ev.getEventId()).append(",");
            json.append("\"title\":\"").append(ServletResponseUtil.escapeJson(ev.getTitle())).append("\",");
            json.append("\"type\":\"").append(ServletResponseUtil.escapeJson(ev.getEventType())).append("\",");
            json.append("\"date\":\"").append(ev.getEventDate() == null ? "" : ev.getEventDate().toString()).append("\",");
            json.append("\"startTime\":\"").append(ev.getStartTime() == null ? "" : ev.getStartTime().toString()).append("\",");
            json.append("\"endTime\":\"").append(ev.getEndTime() == null ? "" : ev.getEndTime().toString()).append("\",");
            json.append("\"location\":\"").append(ServletResponseUtil.escapeJson(ev.getLocation())).append("\",");
            json.append("\"description\":\"").append(ServletResponseUtil.escapeJson(ev.getDescription())).append("\"");
            json.append("}");
        }
        json.append("]");

        ServletResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, json.toString());
    }
}
