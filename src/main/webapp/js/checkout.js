

function formatVND(amount) {

    const num = parseFloat(amount);

    if (isNaN(num)) {
        return amount;
    }

    return parseInt(num).toLocaleString("vi-VN") + " VND";
}


function formatExistingMoney() {

    document
            .querySelectorAll(".format-money, .format-money-js")
            .forEach(function (element) {

                const value =
                        parseFloat(
                                element.textContent.trim()
                                );

                if (!isNaN(value)) {
                    element.textContent =
                            formatVND(value);
                }

            });
}


document.addEventListener(
        "DOMContentLoaded",
        formatExistingMoney
        );

function addServiceItem() {
    // logic add service
}

function removeRow(button) {
    // logic remove service
}

