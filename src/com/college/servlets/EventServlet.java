package com.college.servlets;

import com.college.models.Event;
import com.college.service.EventService;
import com.college.utils.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/events")
public class EventServlet extends BaseServlet {
    private final EventService eventService = new EventService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String monthParam = req.getParameter("month");
        YearMonth month;

        try {
            month = (monthParam == null || monthParam.trim().isEmpty())
                    ? YearMonth.now()
                    : YearMonth.parse(monthParam.trim());
        } catch (DateTimeParseException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid month format. Use yyyy-MM.");
            return;
        }

        try {
            List<Event> events = eventService.getEventsForMonth(month);
            List<String> items = new ArrayList<>();
            for (Event ev : events) {
                items.add(JsonUtil.object(
                        "eventId", JsonUtil.num(ev.getEventId()),
                        "title", JsonUtil.str(ev.getTitle()),
                        "type", JsonUtil.str(ev.getEventType()),
                        "date", JsonUtil.str(ev.getEventDate() == null ? "" : ev.getEventDate().toString()),
                        "startTime", JsonUtil.str(ev.getStartTime() == null ? "" : ev.getStartTime().toString()),
                        "endTime", JsonUtil.str(ev.getEndTime() == null ? "" : ev.getEndTime().toString()),
                        "location", JsonUtil.str(ev.getLocation()),
                        "description", JsonUtil.str(ev.getDescription())
                ));
            }
            JsonUtil.sendSuccess(resp, JsonUtil.array(items));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not load events.");
        }
    }
}
