document.addEventListener("DOMContentLoaded", function () {

    const searchType = document.getElementById("searchType");
    const inputKeyword = document.getElementById("inputKeyword");
    const selectPosition = document.getElementById("selectPosition");
    const selectShift = document.getElementById("selectShift");
    const realKeyword = document.getElementById("realKeyword");
    const searchForm = document.getElementById("searchForm");


// Show the correct search input
    function updateSearchInput() {

        const type = searchType.value;

        // Hide all inputs first
        inputKeyword.classList.add("d-none");
        selectPosition.classList.add("d-none");
        selectShift.classList.add("d-none");


        // Search by Full Name
        if (type === "name") {

            inputKeyword.classList.remove("d-none");

            realKeyword.value = inputKeyword.value;
        }


        // Search by Position
        else if (type === "position") {

            selectPosition.classList.remove("d-none");

            realKeyword.value = selectPosition.value;
        }


        // Search by Work Shift
        else if (type === "shift") {

            selectShift.classList.remove("d-none");

            realKeyword.value = selectShift.value;
        }
    }


// Change search type
    searchType.addEventListener("change", function () {

        // Clear previous values
        inputKeyword.value = "";
        selectPosition.value = "";
        selectShift.value = "";
        realKeyword.value = "";

        updateSearchInput();
    });


// Full Name input
    inputKeyword.addEventListener("input", function () {

        if (searchType.value === "name") {
            realKeyword.value = inputKeyword.value;
        }

    });


// Position dropdown
    selectPosition.addEventListener("change", function () {

        if (searchType.value === "position") {
            realKeyword.value = selectPosition.value;
        }

    });


// Work Shift dropdown
    selectShift.addEventListener("change", function () {

        if (searchType.value === "shift") {
            realKeyword.value = selectShift.value;
        }

    });


// Make sure keyword is correct before submitting
    searchForm.addEventListener("submit", function () {

        if (searchType.value === "name") {

            realKeyword.value = inputKeyword.value;

        } else if (searchType.value === "position") {

            realKeyword.value = selectPosition.value;

        } else if (searchType.value === "shift") {

            realKeyword.value = selectShift.value;
        }

    });


// Initialize when page loads
    updateSearchInput();


});
