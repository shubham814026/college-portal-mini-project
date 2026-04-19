(function () {
  function pad2(n) {
    return String(n).padStart(2, '0');
  }

  function toMonthParam(date) {
    return date.getFullYear() + '-' + pad2(date.getMonth() + 1);
  }

  function toISODate(date) {
    return date.getFullYear() + '-' + pad2(date.getMonth() + 1) + '-' + pad2(date.getDate());
  }

  function sameDay(a, b) {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
  }

  function el(tag, className, text) {
    var node = document.createElement(tag);
    if (className) node.className = className;
    if (text != null) node.textContent = text;
    return node;
  }

  var root = document.getElementById('calendarRoot');
  if (!root) return;

  var ctx = window.APP_CTX || '';
  var label = document.getElementById('calLabel');
  var prevBtn = document.getElementById('calPrev');
  var nextBtn = document.getElementById('calNext');
  var dayLabel = document.getElementById('calDayLabel');
  var dayList = document.getElementById('calDayList');

  var activeMonth = new Date();
  activeMonth.setDate(1);
  activeMonth.setHours(0, 0, 0, 0);

  var selectedDay = new Date();
  selectedDay.setHours(0, 0, 0, 0);

  var eventsByDate = Object.create(null);

  function monthTitle(d) {
    return d.toLocaleString(undefined, { month: 'long', year: 'numeric' });
  }

  function clearNode(node) {
    while (node.firstChild) node.removeChild(node.firstChild);
  }

  function renderDayDetails(date) {
    var key = toISODate(date);
    var items = eventsByDate[key] || [];

    dayLabel.textContent = 'Events on ' + key;
    clearNode(dayList);

    if (!items.length) {
      dayList.appendChild(el('li', 'empty-state', 'No events for this day.'));
      return;
    }

    items.forEach(function (ev) {
      var li = el('li', 'calendar-event-item');
      var line = '';
      if (ev.startTime) {
        line += ev.startTime;
        if (ev.endTime) line += ' - ' + ev.endTime;
        line += ' · ';
      }
      if (ev.type) {
        line += ev.type + ' · ';
      }
      line += ev.title || 'Untitled';
      if (ev.location) {
        line += ' (' + ev.location + ')';
      }
      li.textContent = line;
      dayList.appendChild(li);
    });
  }

  function renderCalendarGrid() {
    label.textContent = monthTitle(activeMonth);
    clearNode(root);

    var grid = el('div', 'calendar-grid');

    var dows = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    dows.forEach(function (d) {
      grid.appendChild(el('div', 'calendar-dow', d));
    });

    var first = new Date(activeMonth);
    var startDow = first.getDay();

    var start = new Date(first);
    start.setDate(first.getDate() - startDow);

    for (var i = 0; i < 42; i++) {
      (function () {
        var cellDate = new Date(start);
        cellDate.setDate(start.getDate() + i);
        cellDate.setHours(0, 0, 0, 0);

        var inMonth = cellDate.getMonth() === activeMonth.getMonth();
        var cell = el('button', 'calendar-cell' + (inMonth ? '' : ' calendar-cell-muted'));
        cell.type = 'button';

        var top = el('div', 'calendar-cell-top');
        top.appendChild(el('span', 'calendar-daynum', String(cellDate.getDate())));

        var key = toISODate(cellDate);
        var count = (eventsByDate[key] || []).length;
        if (count) {
          top.appendChild(el('span', 'calendar-badge', String(count)));
        }

        cell.appendChild(top);

        if (sameDay(cellDate, selectedDay)) {
          cell.className += ' calendar-cell-selected';
        }

        cell.addEventListener('click', function () {
          selectedDay = new Date(cellDate);
          renderCalendarGrid();
          renderDayDetails(selectedDay);
        });

        grid.appendChild(cell);
      })();
    }

    root.appendChild(grid);
  }

  function indexEvents(events) {
    eventsByDate = Object.create(null);
    (events || []).forEach(function (ev) {
      if (!ev || !ev.date) return;
      if (!eventsByDate[ev.date]) eventsByDate[ev.date] = [];
      eventsByDate[ev.date].push(ev);
    });

    Object.keys(eventsByDate).forEach(function (k) {
      eventsByDate[k].sort(function (a, b) {
        var at = a.startTime || '';
        var bt = b.startTime || '';
        return at.localeCompare(bt);
      });
    });
  }

  function loadMonth() {
    var m = toMonthParam(activeMonth);
    var url = ctx + '/events?month=' + encodeURIComponent(m);

    return fetch(url, { credentials: 'same-origin' })
      .then(function (r) {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
      })
      .then(function (data) {
        indexEvents(data);
        renderCalendarGrid();

        var firstOfMonth = new Date(activeMonth);
        if (selectedDay.getMonth() !== activeMonth.getMonth() || selectedDay.getFullYear() !== activeMonth.getFullYear()) {
          selectedDay = firstOfMonth;
        }
        renderDayDetails(selectedDay);
      })
      .catch(function () {
        indexEvents([]);
        renderCalendarGrid();
        dayLabel.textContent = 'Could not load events.';
        clearNode(dayList);
      });
  }

  prevBtn.addEventListener('click', function () {
    activeMonth.setMonth(activeMonth.getMonth() - 1);
    activeMonth.setDate(1);
    loadMonth();
  });

  nextBtn.addEventListener('click', function () {
    activeMonth.setMonth(activeMonth.getMonth() + 1);
    activeMonth.setDate(1);
    loadMonth();
  });

  loadMonth();
})();
