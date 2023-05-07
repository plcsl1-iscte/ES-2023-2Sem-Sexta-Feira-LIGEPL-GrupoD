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

// Add an event listener to the dropdown button
var dropdownBtn = document.getElementById("dropdownBtn");
dropdownBtn.addEventListener("click", function() {
  // Toggle the dropdown content's visibility
  var dropdownContent = document.getElementById("dropdownContent");
  dropdownContent.classList.toggle("show");
});

// Add an event listener to each dropdown item
var dropdownItems = document.querySelectorAll(".dropdown-item");
dropdownItems.forEach(function(item) {
  item.addEventListener("click", function() {
    // Update the selected value
    var selectedValue = item.textContent;
    var dropdownBtnText = document.getElementById("dropdownBtnText");
    dropdownBtnText.textContent = selectedValue;
    
    // Close the dropdown
    var dropdownContent = document.getElementById("dropdownContent");
    dropdownContent.classList.remove("show");
  });
});

document.getElementById('curso').addEventListener('change', function() {
    document.getElementById('cursoForm').submit();
  });

  
