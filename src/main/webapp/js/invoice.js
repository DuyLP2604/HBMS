// Opens the browser print dialog for the invoice.
document.addEventListener("DOMContentLoaded", function ()
{
    const printButton = document.getElementById("printInvoiceButton");
    if (!printButton)
    {
        return;
    }
    printButton.addEventListener("click", function ()
    {
        window.print();
    });
});