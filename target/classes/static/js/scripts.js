$(document).ready(function () {
    // Initialize the calendar
    $('#calendar').fullCalendar({
        // Add your calendar options here
        // For example:
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'month,agendaWeek,agendaDay'
        },
        defaultDate: '2023-04-18',
        navLinks: true, // can click day/week names to navigate views
        editable: true,
        eventLimit: true, // allow "more" link when too many events
    });
}); 