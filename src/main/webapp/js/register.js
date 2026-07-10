
function nextStep() {

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;
    const confirm = document.getElementById("confirmPassword").value;

    if(username === ""){
        alert("Please enter username");
        return;
    }

    if(password.length < 6){
        alert("Password must contain at least 6 characters");
        return;
    }

    if(password !== confirm){
        alert("Passwords do not match");
        return;
    }

    document.getElementById("step1").style.display = "none";
    document.getElementById("step2").style.display = "block";
}

function backStep(){
    document.getElementById("step2").style.display = "none";
    document.getElementById("step1").style.display = "block";
}
