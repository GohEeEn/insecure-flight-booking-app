function showPassword() {

    const x = document.getElementById("password");
    const y = document.getElementById("passwordConfirm");

    if (x.type === "password") x.type = "text";
    else x.type = "password";

    if(y != null) {
        if(y.type === "password") y.type = "text";
        else y.type = "password";
    }
}

function showNewPassword() {

    const x = document.getElementById("newPassword");
    const y = document.getElementById("newPasswordDuplicate");

    if (x.type === "password" || y.type === "password") {
        x.type = "text";
        y.type = "text";
    } else {
        x.type = "password";
        y.type = "password";
    }
}