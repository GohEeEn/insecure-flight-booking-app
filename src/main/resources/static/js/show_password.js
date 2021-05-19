function showPassword() {

    const x = document.getElementById("password");
    const y = document.getElementById("passwordConfirm");

    if (x.type === "password") {
        x.type = "text";
    } else {
        x.type = "password";
        y.type = "password";
    }

    if(y != null) {
        if(y.type === "password") y.type = "text";
        else y.type = "password";
    }
}