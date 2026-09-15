
const createAccount =
        document.getElementById("createAccount");

const accountFields =
        document.getElementById("accountFields");


createAccount.addEventListener(
        "change",
        function () {

            accountFields.style.display =
                    this.checked
                    ? "block"
                    : "none";
        }
);

